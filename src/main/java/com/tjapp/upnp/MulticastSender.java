package com.tjapp.upnp;

import java.io.*;
import java.net.*;


public class MulticastSender {

	private DatagramSocket socket;
	private SocketAddress targetAddr;
	

	public MulticastSender() throws IOException {
		this.targetAddr = new InetSocketAddress(SSDP.MCAST_GROUP, SSDP.MCAST_PORT);
		socket = new DatagramSocket();
	}

	public MulticastSender(SocketAddress targetAddr) throws IOException {
		this.targetAddr = targetAddr;
		socket = new DatagramSocket();
	}

	public void send(String data) throws IOException {
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), targetAddr);
		socket.send(packet);
	}

	public void close() {
		socket.close();
	}
	
	public static void main(String args[]) {
	}
}
