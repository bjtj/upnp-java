package com.tjapp.upnp;

class SSDPHeader extends HttpHeader {

	public SSDPHeader(HttpHeader header) {
		copy(header);
	}
	
	public boolean isNotify() {
		return getFirstParts()[0] == "NOTIFY";
	}

	public boolean isNotifyAlive() {
		return isNotify() && getHeader("NTS").equals("ssdp:alive");
	}

	public boolean isNotifyUpdate() {
		return isNotify() && getHeader("NTS").equals("ssdp:update");
	}

	public boolean isNotifyByebye() {
		return isNotify() && getHeader("NTS").equals("ssdp:byebye");
	}
	
	public boolean isMsearch() {
		return getFirstParts()[0] == "M-SEARCH";
	}
	
	public boolean isResponse() {
		return getFirstParts()[0].startsWith("HTTP");
	}
}
