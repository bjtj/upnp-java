package com.tjapp.upnp;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.tjapp.upnp.*;

public class TestHttpHeader {
	@Test
	public void test_header_string() {
		HttpHeader header = new HttpHeader();

		header.setFirstLine("HTTP/1.1 200 OK");
		header.setHeader("Content-Type", "text/plain");
		header.setHeader("Content-Length", "0");

		assertEquals(header.toString(), "HTTP/1.1 200 OK\r\n" +
					 "Content-Type: text/plain\r\n" + 
					 "Content-Length: 0\r\n\r\n");

		assertEquals(header.getHeader("Content-Type"), "text/plain");
		assertEquals(header.getHeader("CONTENT-TYPE"), "text/plain");
		assertEquals(header.getHeader("Content-Length"), "0");

		header.removeHeader("Content-Type");
		assertEquals(header.getHeader("Content-Type"), null);
		assertEquals(header.getHeader("Content-Length"), "0");
		assertEquals(header.getHeader("CONTENT-LENGTH"), "0");
	}

	@Test
	public void test_http_header_parser() {
		String text = "HTTP/1.1 200 OK\r\n" +
			"Content-Type: text/plain\r\n" + 
			"Content-Length: 0\r\n\r\n";

		HttpHeaderParser parser = new HttpHeaderParser();
		HttpHeader header = parser.parse(text);

		assertEquals(header.getHeader("Content-Type"), "text/plain");
		assertEquals(header.getHeader("Content-Length"), "0");
	}
}
