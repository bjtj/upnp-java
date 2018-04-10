package com.tjapp.upnp;

import java.io.*;
import java.net.*;
import java.util.*;

class SSDPReceiver {
	
	private int port;
	private MulticastSocket sock;
	private boolean done;
	private List<OnSSDPHandler> handlers = new ArrayList<>();
	private static Logger logger = Logger.getLogger("SSDPReceiver");
	

	public SSDPReceiver (int port) {
		this.port = port;
	}
	
	public void run() {
		try {
			sock = new MulticastSocket(port);
			sock.setReuseAddress(true);
			sock.joinGroup(InetAddress.getByName(SSDP.MCAST_GROUP));
			sock.setTimeToLive(64);
			byte[] data = new byte[8 * 1024];
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
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Runnable getRunnable() {
		return new Runnable() {
			public void run() {
				SSDPReceiver.this.run();
			}
		};
	}

	public void handleSSDPPacket(DatagramPacket packet) {
		HttpHeaderParser parser = new HttpHeaderParser();
		String text = new String(packet.getData(), 0, packet.getLength());
		HttpHeader header = parser.parse(text);
		SSDPHeader ssdp = new SSDPHeader(header);
		for (OnSSDPHandler handler : handlers) {
			handler.handle(ssdp);
		}
	}

	public void addHandler(OnSSDPHandler handler) {
		handlers.add(handler);
	}

	public void removeHandler(OnSSDPHandler handler) {
		handlers.remove(handler);
	}

	public static void main(String[] args) {
		SSDPReceiver receiver = new SSDPReceiver(SSDP.MCAST_PORT);
		receiver.addHandler(new OnSSDPHandler() {
				public void handle(SSDPHeader ssdp) {
					logger.debug(ssdp.toString());
				}
			});
		receiver.run();
	}
}
