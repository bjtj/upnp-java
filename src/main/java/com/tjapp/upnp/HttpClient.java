package com.tjapp.upnp;

import java.io.*;
import java.net.*;


class HttpClient {

	private static Logger logger = Logger.getLogger("HttpClient");
	
	public static void main(String args[]) {
		// 
	}

	public byte[] doGet(URL url) throws IOException {
		InputStream in = url.openConnection().getInputStream();
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) > 0) {
			ba.write(buffer, 0, len);
		}
		return ba.toByteArray();
	}

	public byte[] doPost(URL url, byte[] data) throws IOException {
		OutputStream out = url.openConnection().getOutputStream();
		out.write(data);
		InputStream in = url.openConnection().getInputStream();
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) > 0) {
			ba.write(buffer, 0, len);
		}
		return ba.toByteArray();
	}
}
