package com.tjapp.upnp;

import java.io.*;
import java.net.*;
import java.util.*;



public class HttpClient {

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

	private void setHeaderFields(HttpHeader header, Map<String, String> headers) {
		if (headers == null) {
			return;
		}
		Iterator<String> keys = headers.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			header.setHeader(key, headers.get(key));
		}
	}

	public HttpResponse doGet(String url) throws IOException {
		return doGet(new URL(url), null);
	}

	public HttpResponse doGet(String url, Map<String, String> headers) throws IOException {
		return doGet(new URL(url), headers);
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

	public HttpResponse doPost(String url, byte[] data) throws IOException {
		return doPost(new URL(url), null, data);
	}

	public HttpResponse doPost(String url, Map<String, String> headers, byte[] data) throws IOException {
		return doPost(new URL(url), headers, data);
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

	public HttpResponse doRequest(String url, String method, Map<String, String> headers, byte[] data) throws IOException {
		return doRequest(new URL(url), method, headers, data);
	}

	public HttpResponse doRequest(URL url, String method, Map<String, String> headers, byte[] data) throws IOException {

		Socket socket = new Socket(url.getHost(), url.getPort());
		InputStream in = socket.getInputStream();
		OutputStream out = socket.getOutputStream();

		HttpHeader header = new HttpHeader();
		header.setFirstLine(method + " " + url.getPath() + " HTTP/1.1");
		setHeaderFields(header, headers);
		header.setContentLength(data == null ? 0 : data.length);
		out.write(header.toString().getBytes());
		if (data != null && data.length > 0) {
			out.write(data);
		}

		HttpResponse response = new HttpResponse();
		StringBuffer sb = new StringBuffer();
		while (sb.indexOf("\r\n\r\n") < 0) {
			sb.append((char)in.read());
		}
		response.setHttpHeader(HttpHeader.fromString(sb.toString()));
		int length = response.getContentLength();
		if (length > 0) {
			int cur = 0;
			byte[] responseData = new byte[length];
			while (cur < length) {
				int len = in.read(responseData, cur, length - cur);
				if (len <= 0) {
					throw new IOException("socket closed unexpectedly");
				}
				cur += len;
			}
			response.setData(responseData);
		}
		try {
			socket.close();
		} catch (Exception e) {
		}
		return response;
	}

	public static void main(String args[]) {
		// 
	}
}
