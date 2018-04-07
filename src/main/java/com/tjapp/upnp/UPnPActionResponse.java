package com.tjapp.upnp;

import java.util.*;

class UPnPActionResponse {

	private Map<String, String> parameters = new LinkedHashMap<>();

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
}
