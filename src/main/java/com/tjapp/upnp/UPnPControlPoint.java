package com.tjapp.upnp;

import java.io.*;
import java.util.*;


class UPnPControlPoint {

	private static Logger logger = Logger.getLogger("UPnPControlPoint");
	private int port;
	
	public UPnPControlPoint (int port) {
		this.port = port;
	}
	
	public void run() {
	}

	public void subscribeEvent() {
	}

	public void unsubscribeEvent() {
	}

	public void msearch(String query) throws IOException {
		SSDPMsearchSender sender = new SSDPMsearchSender(query, 5);
		sender.send();
		while (sender.timeout() == false) {
			sender.pending(10);
		}
		sender.close();
		logger.debug(String.format("Received: %d", sender.getList().size()));
	}

	public List<UPnPDeviceSession> candidates() {
		return null;
	}

	public static void main(String[] args) throws Exception {
		// run
		// search
		// list devices
		// invoke actions
		// subscribe events
		// watching device up/down

		UPnPControlPoint cp = new UPnPControlPoint(9090);

		cp.msearch("ssdp:all");

		List<UPnPDeviceSession> sessions = cp.candidates();
		for (UPnPDeviceSession session : sessions) {

			switch (session.getStatus()) {
			case PENDING:
				System.out.println("[pending] not ready yet");
				break;
			case COMPLETE:
				if (session.getDeviceType().equals("urn:schemas-upnp-org:device:BinaryLight:1")) {
					UPnPService service = session.getService("urn:schemas-upnp-org:service:SwitchPower:1");
					UPnPActionRequest request = new UPnPActionRequest(service.getAction("SetTarget"));
					request.setParameter("NewTargetValue", "1");
					UPnPActionResponse resp = session.invokeAction(request);
				}
				break;
			default:
				break;
			}
		}
	}
}
