package com.tjapp.upnp;

import java.util.*;


class UPnPActionRequest {

	private UPnPService service;
	private UPnPAction action;
	private Map<String, String> parameters = new LinkedHashMap<>();

	public UPnPActionRequest (UPnPService service, String actionName) {
		this.service = service;
		this.action = service.getAction(actionName);
	}

	public String getScpdUrl() {
		return service.getScpdUrl();
	}

	public void setService(UPnPService service) {
		this.service = service;
	}

	public UPnPService getService() {
		return service;
	}

	public UPnPAction getAction() {
		return action;
	}

	public void setAction(UPnPAction action) {
		this.action = action;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public void setParameter(String key, String value) {
		parameters.put(key, value);
	}

	public String getParameter(String key) {
		return parameters.get(key);
	}

	public void removeParameter(String key) {
		parameters.remove(key);
	}

	public String toSoap() {
		return null;
	}
}
