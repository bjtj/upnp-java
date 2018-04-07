package com.tjapp.upnp;


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
	public int getContentLength() {
		String len = header.getHeader("Content-Length");
		return (len == null ? -1 : Integer.parseInt(len));
	}

	public String getContentType() {
		return header.getHeader("Content-Type");
	}

	public void setData(byte[] data) {
		this.data = data;
		header.setHeader("Content-Length", Integer.toString(data.length));
	}
}
