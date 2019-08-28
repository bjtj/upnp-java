package com.tjapp.upnp;

import java.io.*;
import java.util.*;
import java.net.*;

public class NetworkManager {

	private static Logger logger = Logger.getLogger("NetworkManager");

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

	public static InetAddress getIpv4() throws IOException {
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		while (ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			Enumeration<InetAddress> addrs = iface.getInetAddresses();
			while (addrs.hasMoreElements()) {
				InetAddress addr = addrs.nextElement();
				boolean ipv4 = addr instanceof Inet4Address;
				if (ipv4 && addr.isLoopbackAddress() == false) {
					return addr;
				}
			}
		}
		return null;
	}

	public static void main(String[] args) throws Exception {

		logger.debug("local host: " + InetAddress.getLocalHost());
		logger.debug("loopback: " + InetAddress.getLoopbackAddress());
		
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		while (ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			logger.debug(iface.toString());
			Enumeration<InetAddress> addrs = iface.getInetAddresses();
			while (addrs.hasMoreElements()) {
				InetAddress addr = addrs.nextElement();
				boolean ipv4 = addr instanceof Inet4Address;
				logger.debug(" * [ipv4:" + ipv4 + "] " + addr.getHostAddress() + " / loopback: " + addr.isLoopbackAddress());
			}
		}
	}
}
