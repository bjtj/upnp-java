package com.tjapp.upnp;

import java.util.*;


class UPnPEventSubscription {

	private String eventSubUrl;
	private String sid;
	private String udn;
	private String serviceType;
	private long lastSeq;
	private List<String> callbackUrls = new ArrayList<>();
	private long timeoutSec;
	private long tick;

	public UPnPEventSubscription () {
		tick = Clock.getTickMilli();
	}

	public void updateTick() {
		tick = Clock.getTickMilli();
	}

	public long lifetime() {
		return (Clock.getTickMilli() - tick);
	}

	public boolean expired() {
		return (lifetime() >= (timeoutSec * 1000));
	}

	public String getEventSubUrl() {
		return eventSubUrl;
	}
	public void setEventSubUrl(String eventSubUrl) {
		this.eventSubUrl = eventSubUrl;
	}
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public String getUdn() {
		return udn;
	}
	public void setUdn(String udn) {
		this.udn = udn;
	}
	public String getServiceType() {
		return serviceType;
	}
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
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

	public static UPnPEventSubscription create(List<String> callbackUrls, long timeoutSec) {
		UPnPEventSubscription subscription = new UPnPEventSubscription();
		subscription.setCallbackUrls(callbackUrls);
		subscription.setTimeoutSec(timeoutSec);
		return subscription;
	}
	
	public static void main(String args[]) {
		
	}
}
