package com.tjapp.upnp;

class HttpRequest {
	
	public HttpHeader header;
	public byte[] buffer;

	public String getPath() {
		return header.getFirstParts()[1];
	}

	public int getContentLength() {
		String len = header.getHeader("Content-Length");
		return (len == null ? -1 : Integer.parseInt(len));
	}

	public String getContentType() {
		return header.getHeader("Content-Type");
	}
}
