package com.tjapp.upnp;

class UPnPStateVariable {
	
	private boolean sendEvents;
	private String name;
	private String dataType;
	private String defaultValue;

	public boolean getSendEvents() {
		return sendEvents;
	}

	public void setSendEvents(boolean sendEvents) {
		this.sendEvents = sendEvents;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	};

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
