package com.tjapp.upnp;

import java.io.*;
import java.net.*;
import java.util.*;



class HttpClient {

	private static Logger logger = Logger.getLogger("HttpClient");

	private class Config {
		private boolean redirect;
		public void setRedirect(boolean redirect) {
			this.redirect = redirect;
		}
		public boolean getRedirect() {
			return redirect;
		}
	}

	private Config config = new Config();

	public Config getConfig() {
		return config;
	}

	private void setHeaderFields(HttpURLConnection conn, Map<String, String> headers) {
		if (headers == null) {
			return;
		}
		Iterator<String> keys = headers.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			conn.setRequestProperty(key, headers.get(key));
		}
	}

	public HttpResponse doGet(URL url) throws IOException {
		return doGet(url, null);
	}

	public HttpResponse doGet(URL url, Map<String, String> headers) throws IOException {
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setFollowRedirects(config.getRedirect());
		setHeaderFields(conn, headers);
		return HttpResponse.fromConnection(conn);
	}

	public HttpResponse doPost(URL url, byte[] data) throws IOException {
		return doPost(url, null, data);
	}

	public HttpResponse doPost(URL url, Map<String, String> headers, byte[] data) throws IOException {
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setDoOutput(true);
		conn.setFollowRedirects(config.getRedirect());
		setHeaderFields(conn, headers);
		OutputStream out = conn.getOutputStream();
		out.write(data);
		return HttpResponse.fromConnection(conn);
	}

	public static void main(String args[]) {
		// 
	}
}
