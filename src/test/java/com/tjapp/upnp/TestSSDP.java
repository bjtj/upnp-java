package com.tjapp.upnp;

import static org.junit.Assert.assertEquals;
import org.junit.*;

public class TestSSDP {

	@Test
	public void test_ssdp_header() {
		String[] tokens = "NOTIFY * HTTP/1.1".split("\\s+");
		assertEquals(tokens.length, 3);
		assertEquals(tokens[0], "NOTIFY");
		assertEquals(tokens[1], "*");
		assertEquals(tokens[2], "HTTP/1.1");

		tokens = "NOTIFY    * HTTP/1.1".split("\\s+");
		assertEquals(tokens.length, 3);
		assertEquals(tokens[0], "NOTIFY");
		assertEquals(tokens[1], "*");
		assertEquals(tokens[2], "HTTP/1.1");

		tokens = "NOTIFY   *\tHTTP/1.1".split("\\s+");
		assertEquals(tokens.length, 3);
		assertEquals(tokens[0], "NOTIFY");
		assertEquals(tokens[1], "*");
		assertEquals(tokens[2], "HTTP/1.1");
	}
	
	@Test
	public void test_ssdp_msearch() throws Exception {
		SSDPMsearchSender sender = new SSDPMsearchSender("ssdp:all", 5);
		sender.send();
		while (sender.timeout() == false) {
			sender.pending(10);
		}
		sender.close();
		System.out.printf("Received: %d\n", sender.getList().size());
	}
}
