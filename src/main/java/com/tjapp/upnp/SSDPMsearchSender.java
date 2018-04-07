package com.tjapp.upnp;

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

class SSDPMsearchSender {

	private static Logger logger = Logger.getLogger("SSDPMsearchSender");
	
	private DatagramChannel channel;
	private Selector selector;
	private String type;
	private int mx;
	private long tick;
	private ByteBuffer buffer = ByteBuffer.allocate(4096);
	private List<SSDPHeader> list = new ArrayList<>();

	
	public SSDPMsearchSender (String type, int mx) throws IOException {
		this(type, mx, null);
	}
	
	public SSDPMsearchSender (String type, int mx, NetworkInterface iface) throws IOException {
		this.type = type;
		this.mx = mx;
		channel = DatagramChannel.open(StandardProtocolFamily.INET);
		channel.bind(null);
		if (iface != null) {
			channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, iface);
		}
		channel.configureBlocking(false);
		selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ);
	}
	
	public void send() throws IOException {
		SocketAddress targetAddr = new InetSocketAddress(SSDP.MCAST_GROUP, SSDP.MCAST_PORT);
		HttpHeader header = new HttpHeader();
		header.setFirstLine("M-SEARCH * HTTP/1.1");
		header.setHeader("HOST", String.format("%s:%d",SSDP.MCAST_GROUP, SSDP.MCAST_PORT));
		header.setHeader("MAN", "\"ssdp:discover\"");
		header.setHeader("MX", Long.toString(mx));
        header.setHeader("ST", type);
		header.setHeader("USER-AGENT", "java/* UPnP/1.1 app/1.0");
		byte[] payload = header.toString().getBytes();
		channel.send(ByteBuffer.wrap(payload), targetAddr);
		tick = Clock.getTickMilli();
	}
	
	public void pending(long timeout) throws IOException{
		if (selector.select(timeout) > 0) {
			Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
			while (keyIter.hasNext()) {
				SelectionKey key = keyIter.next();
				if (key.isReadable()) {
					buffer.clear();
					DatagramChannel channel = (DatagramChannel)key.channel();
					SocketAddress remoteAddr = channel.receive(buffer);
					if (remoteAddr != null) {
						buffer.flip();
						handle(buffer.array(), buffer.remaining(), remoteAddr);
					}
				}
				keyIter.remove();
			}
		}
	}
	
	public void handle(byte[] data, int len, SocketAddress remoteAddr) {
		String text = new String(data, 0, len);
		SSDPHeader header = new SSDPHeader(HttpHeaderParser.parse(text));
		list.add(header);
	}
	
	public boolean timeout() {
		return ((Clock.getTickMilli() - tick) >= (mx * 1000));
	}
	
	public void close() {
		try {
			channel.close();
		} catch (IOException e) {
			// ignore
		}
	}
	
	public List<SSDPHeader> getList() {
		return list;
	}
	
	public static void main(String[] args) throws Exception {
		SSDPMsearchSender sender = new SSDPMsearchSender("ssdp:all", 5);
		sender.send();
		while (sender.timeout() == false) {
			sender.pending(100);
		}
		List<SSDPHeader> list = sender.getList();
		for (SSDPHeader item : list) {
			logger.debug(item.getHeader("usn"));
			logger.debug(" - " + item.getHeader("location"));
		}
		logger.debug("[done]");
	}
}
