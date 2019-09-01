package com.tjapp.upnp;

import java.util.*;
import java.net.*;


public class UPnPDeviceSession {

    private static final long DEFAULT_TIMEOUT = 1800 * 1000;
    private UPnPDeviceSessionStatus status = UPnPDeviceSessionStatus.PENDING;
    private long updateTick;
    private long timeout;
    private UPnPDevice device;
    private URL baseUrl;

    public UPnPDeviceSession () {
	updateTick = Clock.getTickMilli();
	timeout = DEFAULT_TIMEOUT;
    }

    public UPnPDeviceSession (UPnPDevice device) {
	updateTick = Clock.getTickMilli();        
	timeout = DEFAULT_TIMEOUT;
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

    public UPnPService getServiceRecursive(String serviceType) {
	return device.getServiceRecursive(serviceType);
    }

    public long duration() {
	return (Clock.getTickMilli() - updateTick);
    }

    public boolean isExpired() {
	return (duration() > timeout);
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

    public void renewTick() {
	updateTick = Clock.getTickMilli();
    }

    public void setUpdateTick(long registerTick) {
	this.updateTick = registerTick;
    }

    public long getUpdateTick() {
	return updateTick;
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
    
    public static UPnPDeviceSession withDeviceAndTimeout(URL baseUrl, UPnPDevice device, long timeout) {
	UPnPDeviceSession session = new UPnPDeviceSession(device);
        session.setTimeout(timeout);
	session.setBaseUrl(baseUrl);
	session.setStatus(UPnPDeviceSessionStatus.COMPLETE);
	return session;
    }
}
