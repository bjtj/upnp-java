package com.tjapp.upnp;


public class UPnPService {

	private UPnPScpd scpd;
	private String serviceType;

    public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public UPnPAction getAction(String name) {
		return scpd.getAction(name);
	}

	public UPnPScpd getScpd() {
		return scpd;
	}

	public void setScpd(UPnPScpd scpd) {
		this.scpd = scpd;
	}
}
