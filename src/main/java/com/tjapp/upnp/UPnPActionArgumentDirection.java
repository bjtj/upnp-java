package com.tjapp.upnp;

public enum UPnPActionArgumentDirection {
	IN("in"), OUT("out");

	String str;

	UPnPActionArgumentDirection(String str) {
		this.str = str;
	}

	public String toString() {
		return str;
	}
}
