package com.tjapp.upnp;

class HttpRequest {
	
	private HttpHeader header;
	private byte[] data;

	public void clear() {
		header.clear();
		data = null;
	}

	public String getMethod() {
		return header.getFirstParts()[0];
	}

	public String getPath() {
		return header.getFirstParts()[1];
	}

	public String getProtocol() {
		return header.getFirstParts()[2];
	}

	public int getContentLength() {
		String len = header.getHeader("Content-Length");
		return (len == null ? -1 : Integer.parseInt(len));
	}

	public String getContentType() {
		return header.getHeader("Content-Type");
	}

	public boolean keepConnect() {
		if (header.getHeader("Connection").equalsIgnoreCase("close")) {
			return false;
		}
		if (header.getFirstParts()[0].equals("HTTP/1.1") == false) {
			return false;
		}
		return true;
	}

	public void setHttpHeader(HttpHeader header) {
		this.header = header;
	}

	public  HttpHeader getHttpHeader() {
		return header;
	}

	public void setHeader(String name, String value) {
		header.setHeader(name, value);
	}

	public String getHeader(String name) {
		return header.getHeader(name);
	}

	public String[] getHeaders(String name) {
		return header.getHeaders(name);
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	public String text() {
		return new String(data);
	}
}
