package com.tjapp.upnp;

import java.net.*;
import java.util.*;


class UPnPActionInvoke {

	private static Logger logger = Logger.getLogger("UPnPActionInvoke");
	static {
		logger.setWriter(Logger.NULL_WRITER);
	}

	public static UPnPActionResponse invoke(URL baseUrl, UPnPActionRequest request) throws Exception {
		URL url = new URL(baseUrl, request.getControlUrl());
		HttpClient client = new HttpClient();
		String soap = request.toSoap();
		logger.debug("request: " + soap);
		Map<String, String> header = new LinkedHashMap<>();
		header.put("SOAPACTION", "\"" + request.getService().getServiceType() + "#" + request.getAction().getName() + "\"");
		HttpResponse response = client.doPost(url, header, soap.getBytes());
		logger.debug("response: " + response.text());
		return UPnPActionResponse.fromXml(response.text());
	}
}
