package com.tjapp.upnp;

public class Usn {
	
    private String uuid;
    private String urn;

    public Usn (String uuid, String urn) {
	this.uuid = uuid;
	this.urn = urn;
    }

    public void setUuid(String uuid) {
	this.uuid = uuid;
    }

    public String getUuid() {
	return uuid;
    }

    public void setUrn(String urn) {
	this.urn = urn;
    }

    public String getUrn() {
	return urn;
    }

    public static Usn fromString(String str) {
	String uuid = "";
	String urn = "";
	int idx = str.indexOf("::");
	if (idx > 0) {
	    uuid = str.substring(0, idx);
	    urn = str.substring(idx + 2);
	} else {
	    uuid = str;
	}
	return new Usn(uuid, urn);
    }
}
