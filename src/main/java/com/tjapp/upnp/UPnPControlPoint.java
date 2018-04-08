package com.tjapp.upnp;

import java.io.*;
import java.util.*;


public class UPnPControlPoint {

	private static Logger logger = Logger.getLogger("UPnPControlPoint");
	private int port;
	private Map<String, UPnPDeviceSession> sessions = new HashMap<>();
	
	public UPnPControlPoint (int port) {
		this.port = port;
	}
	
	public void run() {
	}

	public void subscribeEvent() {
	}

	public void unsubscribeEvent() {
	}

	public void msearch(String query, int mx) throws IOException {
		SSDPMsearchSender sender = new SSDPMsearchSender(query, mx);
		sender.send();
		while (sender.timeout() == false) {
			sender.pending(10);
		}
		sender.close();
		List<SSDPHeader> list = sender.getList();
		for (SSDPHeader header : list) {
			dispatch(header);
		}
	}

	private void dispatch(SSDPHeader header) {
		UPnPDeviceBuilder builder = UPnPDeviceBuilder.getInstance();
		try {
			Usn usn = Usn.fromString(header.getHeader("usn"));
			if (sessions.get(usn.getUuid()) != null) {
				return;
			}
			UPnPDeviceSession session = new UPnPDeviceSession(usn.getUuid());
			session.setDevice(builder.build(header.getHeader("location")));
			session.setStatus(UPnPDeviceSessionStatus.COMPLETE);
			sessions.put(usn.getUuid(), session);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<UPnPDeviceSession> candidates() {
		List<UPnPDeviceSession> list = new ArrayList<>();
		Iterator<String> keys = sessions.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			list.add(sessions.get(key));
		}
		return list;
	}

	public static void main(String[] args) throws Exception {
		// run
		// search
		// list devices
		// invoke actions
		// subscribe events
		// watching device up/down

		UPnPControlPoint cp = new UPnPControlPoint(9090);

		cp.msearch("ssdp:all", 5);

		List<UPnPDeviceSession> sessions = cp.candidates();
		for (UPnPDeviceSession session : sessions) {
			switch (session.getStatus()) {
			case PENDING:
				logger.debug("[pending] not ready yet - " + session.getDevice().getFriendlyName());
				break;
			case COMPLETE:
				logger.debug("device: " + session.getDevice().getFriendlyName() + " / " + session.getDeviceType());
				if (session.getDeviceType().equals("urn:schemas-upnp-org:device:BinaryLight:1")) {
					UPnPService service = session.getService("urn:schemas-upnp-org:service:SwitchPower:1");
					UPnPActionRequest request = new UPnPActionRequest(service.getAction("SetTarget"));
					request.setParameter("NewTargetValue", "1");
					UPnPActionResponse resp = session.invokeAction(request);
				} else if (session.getDeviceType().equals("urn:schemas-upnp-org:device:MediaServer:1")) {
					List<UPnPService> services = session.getDevice().getServiceList();
					UPnPService service = session.getService("urn:schemas-upnp-org:service:ContentDirectory:1");
					UPnPActionRequest request = new UPnPActionRequest(service.getAction("Browse"));
					request.setParameter("ObjectID", "0");
					request.setParameter("BrowseFlag", "BrowseDirectChildren");
					request.setParameter("Filter", "*");
					request.setParameter("StartingIndex", "0");
					request.setParameter("RequestedCount", "0");
					request.setParameter("SortCriteria", "");
					UPnPActionResponse resp = session.invokeAction(request);
					logger.debug("[invoke action]");
				} else {
					List<UPnPService> services = session.getDevice().getServiceList();
					for (UPnPService service : services) {
						logger.debug("- service type: " + service.getServiceType());
					}
				}
				break;
			default:
				break;
			}
		}
	}
}

