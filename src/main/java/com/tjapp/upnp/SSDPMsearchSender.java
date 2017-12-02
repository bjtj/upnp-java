package com.tjapp.upnp;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

class SSDPMsearchSender {
	private DatagramChannel channel;
	private Selector selector;
	private String type;
	private int mx;
	private long tick;
	
	public SSDPMsearchSender (String type, int mx) throws IOException {
		this.type = type;
		this.mx = mx;
		channel = DatagramChannel.open();
		channel.configureBlocking(false);
		selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ);
	}
	public void send() throws IOException {
		SocketAddress targetAddr = new InetSocketAddress(SSDP.MCAST_GROUP, SSDP.MCAST_PORT);
		HttpHeader header = new HttpHeader();
		byte[] payload = header.toString().getBytes();
		channel.send(ByteBuffer.wrap(payload), targetAddr);
		tick = Clock.getTickMilli();
	}
	public void pending(long timeout) throws IOException{
		if (selector.select(timeout) > 0) {
			
		}
	}
	public boolean timeout() {
		return ((Clock.getTickMilli() - tick) >= (mx * 1000));
	}
	public static void main(String[] args) throws Exception {
		SSDPMsearchSender sender = new SSDPMsearchSender("upnp:rootdevice", 3);
		sender.send();
		while (sender.timeout() == false) {
			sender.pending(100);
		}
	}
}
