package com.tjapp.upnp;

import java.util.*;


class UPnPScpd {

	private int majorVersion;
	private int minorVersion;
	private Map<String, UPnPAction> actions = new LinkedHashMap<>();
	private Map<String, UPnPStateVariable> stateVariables = new LinkedHashMap<>();
	
	public String getSpecVersion() {
		return String.format("%d.%d", majorVersion, minorVersion);
	}

	public UPnPAction getAction(String name) {
		return actions.get(name);
	}

	public Map<String, UPnPAction> getActions() {
		return actions;
	}

	public UPnPStateVariable getStateVariable(String name) {
		return stateVariables.get(name);
	}

	public Map<String, UPnPStateVariable> getStateVariables() {
		return stateVariables;
	}

	public static UPnPScpd fromXml(String xml) {
		return null;
	}

	public String toXml() {
		return "";
	}
}
