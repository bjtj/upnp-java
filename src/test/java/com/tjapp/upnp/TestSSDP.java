package com.tjapp.upnp;

import org.junit.*;

public class TestSSDP {
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
