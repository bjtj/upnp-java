package com.tjapp.upnp;

import java.net.*;

class UPnPActionInvoke {

	public static UPnPActionResponse invoke(URL baseUrl, UPnPActionRequest request) throws Exception {
		URL url = new URL(baseUrl, request.getScpdUrl());
		HttpClient client = new HttpClient();
		byte[] response = client.doPost(url, request.toSoap().getBytes());
		return UPnPActionResponse.fromXml(new String(response));
	}
}
