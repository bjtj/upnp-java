package com.tjapp.upnp;

import java.net.*;
import java.io.*;
import java.util.*;


class UPnPDeviceBuilder {

	private static UPnPDeviceBuilder builder;
	private static Logger logger = Logger.getLogger("UPnPDeviceBuilder");
	static {
		logger.setWriter(Logger.NULL_WRITER);
	}

	public static UPnPDeviceBuilder getInstance() {
		if (builder == null) {
			builder = new UPnPDeviceBuilder();
		}
		return builder;
	}

	private UPnPDeviceBuilder () {		
	}

	public UPnPDevice build(String location) throws Exception {
		HttpClient client = new HttpClient();
		URL url = new URL(location);
		byte[] data = client.doGet(url);
		String deviceDescription = new String(data);
		UPnPDevice device = UPnPDevice.fromXml(deviceDescription);
		List<UPnPService> services = device.getServiceList();
		for (UPnPService service : services) {
			URL scpdUrl = new URL(url, service.getScpdUrl());
			logger.debug("scpd url: " + scpdUrl);
			String scpdXml = new String(client.doGet(scpdUrl));
			service.setScpd(UPnPScpd.fromXml(scpdXml));
		}
		return device;
	}
}
