package com.tjapp.upnp;


class UPnPDeviceSession {

	private UPnPDeviceSessionStatus status = UPnPDeviceSessionStatus.PENDING;
	private long registerTick;
	private long timeout;
	private UPnPDevice device;
	private String baseUrl;

	public String getDeviceType() {
		return "";
	}

	public UPnPActionResponse invokeAction(UPnPActionRequest request) {
		return null;
	}

	public UPnPDevice getDevice() {
		return device;
	}

	public UPnPService getService(String serviceType) {
		return null;
	}

	public boolean expired() {
		long dur = Clock.getTickMilli() - registerTick;
		return dur >= timeout;
	}

	public UPnPDeviceSessionStatus getStatus() {
		return status;
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
