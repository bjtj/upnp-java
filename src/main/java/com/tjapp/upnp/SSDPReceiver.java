package com.tjapp.upnp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

class SSDPServer {
	
	private int port;
	private MulticastSocket sock;
	private boolean done;
	private static Logger logger = Logger.getLogger("SSDPServer");

	public SSDPServer (int port) {
		this.port = port;
	}
	
	public void run() {
		try {
			sock = new MulticastSocket(port);
			sock.setReuseAddress(true);
			sock.joinGroup(InetAddress.getByName(SSDP.MCAST_GROUP));
			sock.setTimeToLive(64);
			byte[] data = new byte[4096];
			DatagramPacket pack = new DatagramPacket(data, data.length);
			while (!done) {
				pack.setLength(data.length);
				sock.receive(pack);
				if (pack.getLength() <= 0) {
					continue;
				}
				handleSSDPPacket(pack);
			}
		}
		catch (Exception e) {
		}
	}

	public void handleSSDPPacket(DatagramPacket packet) {
		HttpHeaderParser parser = new HttpHeaderParser();
		String text = new String(packet.getData(), 0, packet.getLength());
		HttpHeader header = parser.parse(text);
		logger.debug(header.toString());
	}
}
