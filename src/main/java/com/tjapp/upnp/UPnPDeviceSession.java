package com.tjapp.upnp;


class UPnPDeviceSession {

	private UPnPDeviceSessionStatus status = UPnPDeviceSessionStatus.PENDING;
	private long registerTick;
	private long timeout;
	private String uuid;
	private UPnPDevice device;
	private String baseUrl;

	public UPnPDeviceSession (String uuid) {
		this.uuid = uuid;
	}

	public String getDeviceType() {
		return "";
	}

	public UPnPActionResponse invokeAction(UPnPActionRequest request) {
		return null;
	}

	public UPnPDevice getDevice() {
		return device;
	}

	public void setDevice(UPnPDevice device) {
		this.device = device;
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

	public void setStatus(UPnPDeviceSessionStatus status) {
		this.status = status;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
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
