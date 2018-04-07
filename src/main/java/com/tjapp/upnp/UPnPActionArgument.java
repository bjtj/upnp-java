package com.tjapp.upnp;


class UPnPActionArgument {
	
	private String name;
	private String stateVariable;
	private UPnPActionArgumentDirection direction;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStateVariable() {
		return stateVariable;
	}

	public void setStateVariable(String stateVariable) {
		this.stateVariable = stateVariable;
	}

	public UPnPActionArgumentDirection getDirection() {
		return direction;
	}

	public void setDirection(UPnPActionArgumentDirection direction) {
		this.direction = direction;
	}
}
