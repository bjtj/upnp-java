package com.tjapp.upnp;

// https://www.w3.org/TR/2007/REC-soap12-part0-20070427/

import java.util.*;


public class UPnPSoap {
	
	
	public static String toXml(UPnPActionRequest request) {

		String serviceType = request.getService().getServiceType();
		String actionName = request.getAction().getName();
		Map<String, String> parameters = request.getParameters();

		XmlTag env = new XmlTag("s", "Envelope");
		env.addAttribute("s:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");
		env.addAttribute("xmlns:s", "http://schemas.xmlsoap.org/soap/envelope/");

		XmlTag body = new XmlTag("s", "Body");

		XmlTag action = new XmlTag("u", actionName);
		action.addAttribute("xmlns:u", serviceType);

		StringBuffer sb = new StringBuffer();
		Iterator<String> keys = parameters.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			String value = parameters.get(key);
			sb.append(new XmlTag(key).wrap(value));
		}

		return env.wrap(body.wrap(action.wrap(sb.toString())));
	}
	

	public static String toXml(UPnPActionResponse response) {
		
		String serviceType = response.getServiceType();
		String actionName = response.getActionName();
		Map<String, String> parameters = response.getParameters();
	
		XmlTag env = new XmlTag("s", "Envelope");
		env.addAttribute("s:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");
		env.addAttribute("xmlns:s", "http://schemas.xmlsoap.org/soap/envelope/");

		XmlTag body = new XmlTag("s", "Body");

		XmlTag action = new XmlTag("u", actionName + "Response");
		action.addAttribute("xmlns:u", serviceType);

		StringBuffer sb = new StringBuffer();
		Iterator<String> keys = parameters.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			String value = parameters.get(key);
			sb.append(new XmlTag(key).wrap(value));
		}

		return env.wrap(body.wrap(action.wrap(sb.toString())));
	}
}
