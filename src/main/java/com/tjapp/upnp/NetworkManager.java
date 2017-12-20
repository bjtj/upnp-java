package com.tjapp.upnp;

import java.util.*;
import java.net.*;

class NetworkManager {

	public static NetworkInterface iface(String ifaceName) throws Exception {
		return NetworkInterface.getByName(ifaceName);
	}
	
	public static String[] ifaceNames() throws Exception {
		List<String> names = new ArrayList<>();
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		while (ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			names.add(iface.getName());
		}
		return names.toArray(new String[]{});
	}

	public static byte[] getMacAddress(String ifaceName) throws Exception {
		NetworkInterface iface = NetworkInterface.getByName(ifaceName);
		return iface.getHardwareAddress();
	}

	public static Enumeration<InetAddress> inetAddresses(String ifaceName) throws Exception {
		NetworkInterface iface = NetworkInterface.getByName(ifaceName);
		return iface.getInetAddresses();
	}

	public static boolean isIpv6(InetAddress addr) {
		return (addr instanceof Inet6Address);
	}
}
