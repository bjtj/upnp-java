package com.tjapp.upnp;

import static org.junit.Assert.assertEquals;
import org.junit.*;

public class TestNetwork {

	public String bytesToString(byte[] data) {
		if (data == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			if (i > 0) {
				sb.append(":");
			}
			sb.append(String.format("%02x", data[i]));
		}
		return sb.toString();
	}

	@Test
	public void test_ifaces() throws Exception {
		String[] ifaces = NetworkManager.ifaceNames();
		for (String iface : ifaces) {
			System.out.printf("[%s]\n", iface);
			System.out.printf("* MAC - '%s'\n", bytesToString(NetworkManager.getMacAddress(iface)));
		}
	}
}
