package com.tjapp.upnp;

import java.util.*;


class UPnPServer {

	private int port;

	public UPnPServer (int port) {
		this.port = port;
	}

	public void registerDevice(UPnPDevice device, boolean notify) {
		
	}

	public void unregisterDevice(UPnPDevice device, boolean notify) {
		
	}
	
	public void run() {
		
	}

	public void sendNotification(Notification notification) {
		switch (notification) {
		case ALIVE:
			break;
		case UPDATE:
			break;
		case BYEBYE:
			break;
		default:
			break;
		}
	}

	public List<UPnPDevice> list() {
		return null;
	}
}
