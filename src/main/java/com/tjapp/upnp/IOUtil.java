package com.tjapp.upnp;

import java.io.*;
import java.net.*;


public class IOUtil {

	public static byte[] dump(InputStream in) throws IOException {
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) > 0) {
			ba.write(buffer, 0, len);
		}
		return ba.toByteArray();
	}
	
	public static void main(String args[]) {
	}
}
