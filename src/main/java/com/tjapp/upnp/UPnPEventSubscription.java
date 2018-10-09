package com.tjapp.upnp;

import java.util.*;


class UPnPEventSubscription {

	private UPnPService service;
	private String sid;
	private long lastSeq;
	private List<String> callbackUrls = new ArrayList<>();
	private long timeoutSec;
	private long tick;

	public UPnPEventSubscription (UPnPService service) {
		this.service = service;
		tick = Clock.getTickMilli();
	}

	public UPnPService getService() {
		return service;
	}

	public void setService(UPnPService service) {
		this.service = service;
	}

	public void updateTick() {
		tick = Clock.getTickMilli();
	}

	public long lifetime() {
		return (Clock.getTickMilli() - tick);
	}

	public boolean isExpired() {
		return (lifetime() >= (timeoutSec * 1000));
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public long getLastSeq() {
		return lastSeq;
	}

	public void setLastSeq(long lastSeq) {
		this.lastSeq = lastSeq;
	}

	public List<String> getCallbackUrls() {
		return callbackUrls;
	}

	public void addCallbackUrl(String callbackUrl) {
		callbackUrls.add(callbackUrl);
	}

	public void addCallbackUrls(List<String> callbackUrls) {
		this.callbackUrls.addAll(callbackUrls);
	}

	public void setCallbackUrls(List<String> callbackUrls) {
		this.callbackUrls = callbackUrls;
	}

	public String getCallbackUrlsString() {
		StringBuffer sb = new StringBuffer();
		for (String url : callbackUrls) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(url);
		}
		return "<" + sb.toString() + ">";
	}

	public long getTimeoutSec() {
		return timeoutSec;
	}

	public void setTimeoutSec(long timeoutSec) {
		this.timeoutSec = timeoutSec;
	}
	
	public static void main(String args[]) {
		
	}
}
