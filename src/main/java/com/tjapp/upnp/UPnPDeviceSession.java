package com.tjapp.upnp;

import java.util.*;
import java.net.*;


public class UPnPDeviceSession {

	private UPnPDeviceSessionStatus status = UPnPDeviceSessionStatus.PENDING;
	private long registerTick;
	private long timeout;
	private UPnPDevice device;
	private String baseUrl;

	public UPnPDeviceSession () {
		registerTick = Clock.getTickMilli();
		timeout = 30 * 1000;	// default 30 seconds
	}

	public String getUdn() {
		return device.getUdn();
	}

	public String getFriendlyName() {
		return device.getFriendlyName();
	}

	public String getDeviceType() {
		return device.getDeviceType();
	}

	public List<UPnPService> getServiceList() {
		return device.getServiceList();
	}

	public UPnPActionResponse invokeAction(UPnPActionRequest request) throws Exception {
		return UPnPActionInvoke.invoke(new URL(baseUrl), request);
	}

	public UPnPDevice getDevice() {
		return device;
	}

	public void setDevice(UPnPDevice device) {
		this.device = device;
	}

	public UPnPService getService(String serviceType) {
		return device.getService(serviceType);
	}

	public long lifetime() {
		return (Clock.getTickMilli() - registerTick);
	}

	public boolean expired() {
		return (lifetime() >= timeout);
	}

	public UPnPDeviceSessionStatus getStatus() {
		return status;
	}

	public void setStatus(UPnPDeviceSessionStatus status) {
		this.status = status;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setRegisterTick(long registerTick) {
		this.registerTick = registerTick;
	}

	public long getRegisterTick() {
		return registerTick;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getTimeout() {
		return timeout;
	}
}
