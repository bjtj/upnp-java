package com.tjapp.upnp;

import java.util.*;


public class UPnPSoapResponse {
	public void response() {
		String actionName = "";
		String serviceType = "";
		List<Pair<String, String>> items = new ArrayList<>();
	
		XmlTag env = new XmlTag("s", "Envelope");
		env.addAttribute("s:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");
		env.addAttribute("xmlns:s", "http://schemas.xmlsoap.org/soap/envelope/");

		XmlTag body = new XmlTag("s", "Body");

		XmlTag action = new XmlTag("u", actionName + "Response");
		action.addAttribute("xmlns:u", serviceType);

		StringBuffer sb = new StringBuffer();
		for (Pair<String, String> item : items) {
			String key = item.getFirst();
			String value = item.getSecond();
			sb.append(new XmlTag(key).wrap(value));
		}

		env.wrap(body.wrap(action.wrap(sb.toString())));
	}
}
