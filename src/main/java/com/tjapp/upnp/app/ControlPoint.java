package com.tjapp.upnp.app;

import com.tjapp.upnp.*;
import java.util.*;
import java.io.*;

public class ControlPoint {
    
    static class WrongFormatException extends Exception {        
    }

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
		
	UPnPControlPoint cp = new UPnPControlPoint(0);
	cp.addEventListener(new OnEventListener() {
                @Override
		public void onEvent(UPnPEvent event) {
		    println("Event SID: " + event.getSid());
		    List<UPnPProperty> list = event.getPropertyList();
		    for (UPnPProperty property : list) {
			println(" * " + property.getName() + ": " + property.getValue());
		    }
		}
	    });
	cp.addDeviceListener(new OnDeviceListener() {
                @Override
		public void onDeviceAdded(UPnPDevice device) {
		    println("* [ADDED] " + device.getFriendlyName() + " (" + device.getUdn() + ")");
		}
                @Override
		public void onDeviceRemoved(UPnPDevice device) {
		    println("* [REMOVED] " + device.getFriendlyName() + " (" + device.getUdn() + ")");
		}
	    });
        
	new Thread(cp.getRunnable()).start();
	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	println("Press 'h' or 'help' to display help");
	
	while (true) {
	    print("> ");
	    String line = reader.readLine();
	    if (line.equals("h") || line.equals("help")) {
		println("HELP");
		println("===============");
                println("* (Enter) -- Display selection");
		println("* h|help -- Display help");
		println("* q|quit -- Quit");
		println("* search <search type> -- Send msearch");
		println("* ls|list -- List devices");
		println("* [0-9]+ -- Show device of the index");
		println("* service <service name> -- Select service");
		println("* action <action name> -- Select action");
		println("* i|invoke -- Invoke action");
		println("* subscribe -- Subscribe");
		println("* unsubscribe -- Unsubscribe");
		println("");
	    } else if (line.equals("q") || line.equals("quit")) {
		break;
	    } else if (line.startsWith("search")) {
                try {
                    String type = getSearchType(line);
                    println("Start Searching... (" + type + ")");
                    cp.msearchAsync(type, 5);
                } catch (WrongFormatException e) {
                    println("ERR) expected 'search <search type>'");
                }
	    } else if (line.equals("ls") || line.equals("list")) {
		printDeviceList(cp);
	    } else if (line.startsWith("action")) {
                try {
                    String action = getActionName(line);
                    selection.setActionName(action);
                    println("Action name is set to '" + action + "'");
                } catch (WrongFormatException e) {
                    println("ERR) expected 'action <action name>'");
                }
	    } else if (line.startsWith("service")) {
                try {
                    String service = getServiceName(line);
                    selection.setServiceType(service);
                    println("Service type is set to '" + service + "'");
                } catch (WrongFormatException e) {
                    println("ERR) expected 'service <service name>'");
                }
	    } else if (line.equals("i") || line.equals("invoke")) {
		if (selection.session == null ||
		    selection.getServiceType() == null ||
		    selection.getActionName() == null) {
		    println("ERR) No selection");
		    continue;
		}
                try {
                    invokeAction(selection);
                } catch (Exception e) {
                    println("ERR) invoke action failed");
                    e.printStackTrace();
                }
	    } else if (line.equals("subscribe")) {
		UPnPDeviceSession session = selection.getSession();
		UPnPService service = session.getServiceRecursive(selection.getServiceType());
		cp.subscribeEvent(session, service);
	    } else if (line.equals("unsubscribe")) {
		UPnPDeviceSession session = selection.getSession();
		UPnPService service = session.getServiceRecursive(selection.getServiceType());
		cp.unsubscribeEvent(session, service);
	    } else if (line.matches("[0-9]+")) {
		int idx = Integer.parseInt(line);
		println("Select index: " + idx);
		List<UPnPDeviceSession> sessions = cp.candidates();
		if (idx < 0 || idx >= sessions.size() ) {
		    println("ERR) Index out of bound");
		    continue;
		}
		UPnPDeviceSession session = sessions.get(idx);
		selection.setSession(session);
		printDeviceSession("[Selected] ", session, true);
	    } else if (StringUtil.isEmpty(line)) {
                println("Selection");
		println("===============");
		println(selection.toString());
	    } else {
		println("ERR) Unknown command -- " + line);
	    }
	}

	cp.stop();
	println("Bye~");
    }
    
    private static void invokeAction(Selection selection) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        UPnPActionRequest request = new UPnPActionRequest();
        request.setServiceType(selection.getServiceType());
        request.setActionName(selection.getActionName());
        UPnPService service = selection.getSession().getServiceRecursive(selection.getServiceType());
        request.setControlUrl(service.getControlUrl());
        UPnPAction action = service.getAction(selection.getActionName());
        List<UPnPActionArgument> argumentList = action.getArgumentList();
        for (UPnPActionArgument argument : argumentList) {
            if (argument.getDirection() == UPnPActionArgumentDirection.IN) {
                if (argument.getRelatedStateVariable(service).hasAllowedValueList()) {
                    List<String> values = argument.getRelatedStateVariable(service).getAllowedValueList();
                    String lst = String.join("|", values);
                    print(" [in] " + argument.getName() + "(" + lst + "): ");
                } else {
                    print(" [in] " + argument.getName() + ": ");
                }
                String param = reader.readLine();
                request.setParameter(argument.getName(), param);
            }
        }
        println("Invoke action - '" + action.getName() + "'");
	long tick = Clock.getTickMilli();
        UPnPActionResponse response = selection.getSession().invokeAction(request);
	long dur = Clock.getTickMilli() - tick;
	println("Action Response - " + dur + " ms.");
        Map<String, String> params = response.getParameters();
        Iterator<String> keys = params.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            println(" * " + key + ": " + params.get(key));
        }
    }
    
    private static String getActionName(String line) throws WrongFormatException {
        String[] parts = line.split(" +");
        if (parts.length != 2) {
            throw new WrongFormatException();
        }
        return parts[1].trim();
    }
    
    private static String getSearchType(String line) throws WrongFormatException {
        String[] parts = line.split(" +");
        if (parts.length != 2) {
            throw new WrongFormatException();
        }
        return parts[1].trim();
    }
    
    private static String getServiceName(String line) throws WrongFormatException {
        String[] parts = line.split(" +");
        if (parts.length != 2) {
            throw new WrongFormatException();
        }
        return parts[1].trim();
    }

    private static void printDeviceList(UPnPControlPoint cp) {
	List<UPnPDeviceSession> sessions = cp.candidates();
	println("Device list (count: " + sessions.size() + ")");
	println("===============");
	for (int i = 0; i < sessions.size(); i++) {
	    UPnPDeviceSession session = sessions.get(i);
	    printDeviceSession("[" + i + "] ", session, false);
	}
    }
    
    private static void printDeviceSession(String prefix, UPnPDeviceSession session, boolean showActions) {
        UPnPDevice device = session.getDevice();
        printDevice(prefix, device, showActions);
    }

    private static void printDevice(String prefix, UPnPDevice device, boolean showActions) {
	println(prefix + device.getFriendlyName() + " (" + device.getUdn() + ")");
	List<UPnPService> services = device.getServiceList();
	for (UPnPService service : services) {
	    println("    + " + service.getServiceType());
	    UPnPScpd scpd = service.getScpd();
	    if (showActions && scpd != null) {
		List<UPnPAction> actions = scpd.getActions();
		for (UPnPAction action : actions) {
		    println("      * " + action.getName());
		}
	    }
	}
        for (UPnPDevice child : device.getChildDevices()) {
            printDevice(prefix, child, showActions);
        }
    }
}
