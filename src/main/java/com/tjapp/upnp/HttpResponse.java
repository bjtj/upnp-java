package com.tjapp.upnp;

import java.io.*;
import java.net.*;

class HttpResponse {
	
	public HttpHeader header;
	public byte[] data;

	
	public HttpResponse () {
		this.header = new HttpHeader();
		this.header.setFirstLine("HTTP/1.1 200 OK");
	}
		
	public HttpResponse (HttpHeader header, byte[] data) {
		this.header = header;
		this.data = data;
	}

	public void setFirstLine(String firstLine) {
		header.setFirstLine(firstLine);
	}

	public String getFirstLine() {
		return header.getFirstLine();
	}
	
	public int getContentLength() {
		String len = header.getHeader("Content-Length");
		return (len == null ? -1 : Integer.parseInt(len));
	}

	public String getContentType() {
		return header.getHeader("Content-Type");
	}

	public void setHeader(HttpHeader header) {
		this.header = header;
	}

	public HttpHeader getHeader() {
		return header;
	}

	public void setData(byte[] data) {
		this.data = data;
		header.setHeader("Content-Length", Integer.toString(data.length));
	}

	public byte[] getData() {
		return data;
	}

	public String text() {
		return new String(data);
	}

	public static HttpResponse fromConnection(HttpURLConnection conn) throws IOException {
		HttpResponse response = new HttpResponse();
		response.setFirstLine(conn.getHeaderFields().get(null).get(0));
		response.getHeader().setHeaderFields(conn.getHeaderFields());
		byte[] data = IOUtil.dump(conn.getInputStream());
		response.setData(data);
		return response;
	}
}
