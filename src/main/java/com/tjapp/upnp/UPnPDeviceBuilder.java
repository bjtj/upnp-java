package com.tjapp.upnp;

import java.net.*;
import java.io.*;
import java.util.*;


public class UPnPDeviceBuilder {

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

    public UPnPDevice build(URL url) throws Exception {
	HttpClient client = new HttpClient();
	HttpResponse response = client.doGet(url);
	String deviceDescription = response.text();
	UPnPDevice device = UPnPDevice.fromXml(deviceDescription);
	buildDevice(url, device);
	return device;
    }

    public void buildDevice(URL baseUrl, UPnPDevice device) {
	HttpClient client = new HttpClient();
	List<UPnPService> services = device.getServiceList();
	for (UPnPService service : services) {
	    try {
		URL scpdUrl = new URL(baseUrl, service.getScpdUrl());
		logger.debug("SCPD Url: " + scpdUrl);
		String scpdXml = client.doGet(scpdUrl).text();
		service.setScpd(UPnPScpd.fromXml(scpdXml));
	    } catch (Exception e) {
		logger.error("SCPD -- '" + service.getScpdUrl() + "'");
	    }
	}
	for (UPnPDevice childDevice : device.getChildDevices()) {
	    buildDevice(baseUrl, childDevice);
	}

    }

    public UPnPDevice buildResource(String path) throws Exception {
	logger.debug("build resource: " + path);
	byte[] data = IOUtil.dump(UPnPDeviceBuilder.class.getResourceAsStream(path));
	UPnPDevice device = UPnPDevice.fromXml(new String(data));
	List<UPnPService> services = device.getServiceList();
	for (UPnPService service : services) {
	    String scpdUrl = service.getScpdUrl();
	    logger.debug("scpd url: " + scpdUrl);
	    byte[] scpdData = IOUtil.dump(UPnPDeviceBuilder.class.getResourceAsStream(scpdUrl));
	    String scpdXml = new String(scpdData);
	    service.setScpd(UPnPScpd.fromXml(scpdXml));
	}
	return device;
    }
}
