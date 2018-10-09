package com.tjapp.app;

import com.tjapp.upnp.*;
import java.util.*;
import java.io.*;
import java.util.regex.*;

class ControlPoint {

    /**
     * 
     *
     */
    private static class Selection {
	private UPnPDeviceSession session;
	private String serviceType;
	private String actionName;

	public UPnPDeviceSession getSession() {
	    return session;
	}
	public String getServiceType() {
	    return serviceType;
	}
	public String getActionName() {
	    return actionName;
	}

	public void setSession(UPnPDeviceSession session) {
	    this.session = session;
	}
	public void setServiceType(String serviceType) {
	    this.serviceType = serviceType;
	}
	public void setActionName(String actionName) {
	    this.actionName = actionName;
	}
	public String toString() {
	    StringBuffer sb = new StringBuffer();
	    if (session == null) {
		sb.append(" * Device: no selection");
	    } else {
		sb.append(" * Device: " + session.getFriendlyName() + " (" + session.getUdn() + ")");
	    }
	    sb.append("\n");
	    sb.append(" * Service type: " + serviceType);
	    sb.append("\n");
	    sb.append(" * Action name: " + actionName);
	    sb.append("\n");
	    return sb.toString();
	}
    }

    public static void print(String str) {
	System.out.print(str);
    }

    public static void println(String str) {
	System.out.println(str);
    }

    /**
     * 
     *
     */
    public static void main(String[] args) throws Exception {

	Selection selection = new Selection();
		
	UPnPControlPoint cp = new UPnPControlPoint(9000);
	cp.addEventListener(new OnEventListener() {
		public void onEvent(UPnPEvent event) {
		    println("Event sid: " + event.getSid());
		    List<UPnPProperty> list = event.getPropertyList();
		    for (UPnPProperty property : list) {
			println(" * " + property.getName() + ": " + property.getValue());
		    }
		}
	    });
	cp.addDeviceListener(new OnDeviceListener() {
		public void onDeviceAdded(UPnPDevice device) {
		    println("* device added: " + device.getFriendlyName() + " (" + device.getUdn() + ")");
		}
		public void onDeviceRemoved(UPnPDevice device) {
		    println("* device removed: " + device.getFriendlyName() + " (" + device.getUdn() + ")");
		}
	    });
	new Thread(cp.getRunnable()).start();
	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	println("h|help to display help");
	
	while (true) {
	    print("> ");
	    String line = reader.readLine();
	    if (line.equals("h") || line.equals("help")) {
		println("HELP");
		println("====");
		println("h|help -- help");
		println("q|quit -- quit");
		println("search <search type> -- send msearch");
		println("ls -- list");
		println("[0-9]+ -- select device");
		println("service -- select service");
		println("action -- select action");
		println("i|invoke -- invoke action");
		println("subscribe -- subscribe");
		println("unsubscribe -- unsubscribe");
		println("");
	    } else if (line.equals("q") || line.equals("quit")) {
		break;
	    } else if (line.startsWith("search")) {
		println("searching...");
		cp.msearchAsync(line.substring("search ".length()), 5);
		println("done");
	    } else if (line.startsWith("ls")) {
		printDeviceList(cp);
	    } else if (line.startsWith("action")) {
		String action = line.substring("action ".length());
		selection.setActionName(action);
		println("action name is set - " + action);
	    } else if (line.startsWith("service")) {
		String service = line.substring("service ".length());
		selection.setServiceType(service);
		println("service type is set - " + service);
	    } else if (line.equals("i") || line.equals("invoke")) {
		if (selection.session == null ||
		    selection.getServiceType() == null ||
		    selection.getActionName() == null) {
		    println("not selected");
		    continue;
		}
		UPnPActionRequest request = new UPnPActionRequest();
		request.setServiceType(selection.getServiceType());
		request.setActionName(selection.getActionName());
		UPnPService service = selection.getSession().getService(selection.getServiceType());
		request.setControlUrl(service.getControlUrl());
		UPnPAction action = service.getAction(selection.getActionName());
		List<UPnPActionArgument> argumentList = action.getArgumentList();
		for (UPnPActionArgument argument : argumentList) {
		    if (argument.getDirection() == UPnPActionArgumentDirection.IN) {
			print(" [in] " + argument.getName() + ": ");
			String param = reader.readLine();
			request.setParameter(argument.getName(), param);
		    }
		}
		println("invoke action");
		UPnPActionResponse response = selection.getSession().invokeAction(request);
		Map<String, String> params = response.getParameters();
		Iterator<String> keys = params.keySet().iterator();
		while (keys.hasNext()) {
		    String key = keys.next();
		    println(" * " + key + ": " + params.get(key));
		}
	    } else if (line.equals("subscribe")) {
		UPnPDeviceSession session = selection.getSession();
		UPnPService service = session.getService(selection.getServiceType());
		cp.subscribeEvent(session, service);
	    } else if (line.equals("unsubscribe")) {
		UPnPDeviceSession session = selection.getSession();
		UPnPService service = session.getService(selection.getServiceType());
		cp.unsubscribeEvent(session, service);
	    } else if (line.matches("[0-9]+")) {
		int idx = Integer.parseInt(line);
		println("Select index: " + idx);
		List<UPnPDeviceSession> sessions = cp.candidates();
		if (idx < 0 || idx >= sessions.size() ) {
		    println("index out of bound");
		    continue;
		}
		UPnPDeviceSession session = sessions.get(idx);
		selection.setSession(session);
		printDevice("[selected] ", session);
	    } else if (StringUtil.isEmpty(line)) {
		println(selection.toString());
	    } else {
		println("unknown command -- " + line);
	    }
	}

	cp.stop();
	println("[quit]");
    }

    public static void printDeviceList(UPnPControlPoint cp) {
	List<UPnPDeviceSession> sessions = cp.candidates();
	println("Device list (count: " + sessions.size() + ")");
	println("==========================");
	for (int i = 0; i < sessions.size(); i++) {
	    UPnPDeviceSession session = sessions.get(i);
	    printDevice("[" + i + "] ", session);
	    // println("[" + i + "] " + session.getFriendlyName());
	    // List<UPnPService> services = session.getServiceList();
	    // for (UPnPService service : services) {
	    // 	println("    + " + service.getServiceType());
	    // 	UPnPScpd scpd = service.getScpd();
	    // 	if (scpd != null) {
	    // 	    List<UPnPAction> actions = scpd.getActions();
	    // 	    for (UPnPAction action : actions) {
	    // 		println("      * action -- " + action.getName());
	    // 	    }
	    // 	}
	    // }
	}
    }

    public static void printDevice(String prefix, UPnPDeviceSession session) {
	println(prefix + session.getFriendlyName());
	List<UPnPService> services = session.getServiceList();
	for (UPnPService service : services) {
	    println("    + " + service.getServiceType());
	    UPnPScpd scpd = service.getScpd();
	    if (scpd != null) {
		List<UPnPAction> actions = scpd.getActions();
		for (UPnPAction action : actions) {
		    println("      * action -- " + action.getName());
		}
	    }
	}
    }
}
