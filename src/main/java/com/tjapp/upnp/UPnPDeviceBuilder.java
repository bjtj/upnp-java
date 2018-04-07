package com.tjapp.upnp;

import java.net.*;
import java.io.*;


class UPnPDeviceBuilder {

	private static UPnPDeviceBuilder builder;

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
		byte[] data = client.doGet(new URL(location));
		String deviceDescription = new String(data);
		UPnPDevice device = UPnPDevice.fromXml(deviceDescription);
		return device;
	}
}
