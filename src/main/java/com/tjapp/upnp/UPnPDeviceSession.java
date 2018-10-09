package com.tjapp.upnp;

import java.util.*;
import java.net.*;


public class UPnPDeviceSession {

    private UPnPDeviceSessionStatus status = UPnPDeviceSessionStatus.PENDING;
    private long registerTick;
    private long timeout;
    private UPnPDevice device;
    private URL baseUrl;

    public UPnPDeviceSession () {
	registerTick = Clock.getTickMilli();
	timeout = 30 * 1000;	// default 30 seconds
    }

    public UPnPDeviceSession (UPnPDevice device) {
	registerTick = Clock.getTickMilli();
	timeout = 30 * 1000;	// default 30 seconds
	this.device = device;
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
	return UPnPActionInvoke.invoke(baseUrl, request);
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

    public boolean isExpired() {
	return (lifetime() >= timeout);
    }

    public UPnPDeviceSessionStatus getStatus() {
	return status;
    }

    public void setStatus(UPnPDeviceSessionStatus status) {
	this.status = status;
    }

    public URL getBaseUrl() {
	return baseUrl;
    }

    public void setBaseUrl(String baseUrl) throws MalformedURLException {
	this.baseUrl = new URL(baseUrl);
    }

    public void setBaseUrl(URL baseUrl) {
	this.baseUrl = baseUrl;
    }

    public void renewTimeout() {
	registerTick = Clock.getTickMilli();
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

    public static UPnPDeviceSession withDevice(URL baseUrl, UPnPDevice device) {
	UPnPDeviceSession session = new UPnPDeviceSession(device);
	session.setBaseUrl(baseUrl);
	session.setStatus(UPnPDeviceSessionStatus.COMPLETE);
	return session;
    }
}
