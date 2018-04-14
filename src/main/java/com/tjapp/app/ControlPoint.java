package com.tjapp.app;

import com.tjapp.upnp.*;
import java.util.*;
import java.io.*;
import java.util.regex.*;

class ControlPoint {

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
			sb.append(" * Device: " + session.getFriendlyName() +
					  " (" + session.getUdn() + ")");
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

	public static void main(String[] args) throws Exception {

		Selection selection = new Selection();
		
		UPnPControlPoint cp = new UPnPControlPoint(9000);
		new Thread(cp.getRunnable()).start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			print("> ");
			String line = reader.readLine();
			if (line.equals("q") || line.equals("quit")) {
				break;
			} else if (line.startsWith("search")) {
				println("searching...");
				cp.msearch(line.substring("search ".length()), 5);
				println("done");
			} else if (line.startsWith("ls")) {
				List<UPnPDeviceSession> sessions = cp.candidates();
				println("Device list (count: " + sessions.size() + ")");
				println("==========================");
				for (int i = 0; i < sessions.size(); i++) {
					UPnPDeviceSession session = sessions.get(i);
					println("[" + i + "] " + session.getFriendlyName());
					List<UPnPService> services = session.getServiceList();
					for (UPnPService service : services) {
						println("    + " + service.getServiceType());
					}
				}
			} else if (line.startsWith("action")) {
				String action = line.substring("action ".length());
				selection.setActionName(action);
				println("action name is set - " + action);
			} else if (line.startsWith("service")) {
				String service = line.substring("service ".length());
				selection.setServiceType(service);
				println("service type is set - " + service);
			} else if (line.equals("invoke")) {
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
			} else if (line.matches("[0-9]+")) {
				int idx = Integer.parseInt(line);
				println("Select index: " + idx);
				UPnPDeviceSession session = cp.candidates().get(idx);
				selection.setSession(session);
				println("Selected device - " + session.getFriendlyName());
			} else if (StringUtil.isEmpty(line)) {
				println(selection.toString());
			}
		}
		println("[quit]");
	}
}
