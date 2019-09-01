package com.tjapp.upnp;

import java.net.*;

public class SSDPHeader extends HttpHeader {

	private SocketAddress remoteAddr;

	public SSDPHeader(HttpHeader header) {
		copy(header);
	}

	public void setRemoteAddress(SocketAddress remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public SocketAddress getRemoteAddress() {
		return remoteAddr;
	}
	
	public boolean isNotify() {
		return "NOTIFY".equals(getFirstParts()[0]);
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
		return "M-SEARCH".equals(getFirstParts()[0]);
	}
	
	public boolean isResponse() {
		return "HTTP".startsWith(getFirstParts()[0]);
	}
        
        public long getMaxAge() {
            String cacheControl = getHeader("cache-control");
            if (cacheControl == null || !cacheControl.startsWith("max-age=")) {
                return 0;
            }
            return Long.parseLong(cacheControl.split("=")[1]);
        }

	public static SSDPHeader fromString(String str) {
		return new SSDPHeader(HttpHeader.fromString(str));
	}
}
