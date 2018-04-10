package com.tjapp.upnp;

import java.io.*;
import java.util.*;


public class UPnPControlPoint {

	private static Logger logger = Logger.getLogger("UPnPControlPoint");
	private int port;
	private Map<String, UPnPDeviceSession> sessions = new HashMap<>();
	private Map<String, UPnPEventSubscription> subscriptions = new HashMap<>();
	private HttpServer httpServer;
	private boolean finishing;
	private TimerThread timerThread;
	private SSDPReceiver ssdpReceiver;

	/**
	 * 
	 *
	 */
	private class TimerThread extends Thread {
		private UPnPControlPoint cp;
		private boolean paused;
		private long tick;
		private long interval;
		public TimerThread (UPnPControlPoint cp, long interval) {
			this.cp = cp;
			this.interval = interval;
		}
		public void finish() {
			this.interrupt();
			finishing = true;
		}
		public void run() {
			try {
				this.tick = Clock.getTickMilli();
				while (Thread.interrupted() == false && finishing == false) {
					if (Clock.getTickMilli() - tick >= interval) {
						cp.onTimer();
					} else {
						Thread.sleep(10);
					}
				}
			} catch (InterruptedException e) {
				// 
			}
			logger.debug("[timer thread] done");
		}
	}
	
	public UPnPControlPoint (int port) {
		this.port = port;
	}
	
	public void run() {
		timerThread = new TimerThread(this, 10 * 1000);
		timerThread.start();
		ssdpReceiver = new SSDPReceiver(SSDP.MCAST_PORT);
		ssdpReceiver.addHandler(new OnSSDPHandler() {
				public void handle(SSDPHeader ssdp) {
					if (ssdp.isNotify()) {
						logger.debug(ssdp.toString());
					}
				}
			});
		new Thread(ssdpReceiver.getRunnable()).start();
		httpServer = new HttpServer(port);
		httpServer.bind("/event", new HttpServer.Handler() {
				public HttpResponse handle(HttpRequest request) {
					HttpResponse response = new HttpResponse(200);
					return response;
				}
			});
		httpServer.run();
	}

	public void stop() {
		timerThread.finish();
		timerThread = null;
		ssdpReceiver.stop();
		httpServer.stop();
	}

	public Runnable getRunnable() {
		return new Runnable() {
			public void run() {
				UPnPControlPoint.this.run();
			}
		};
	}

	public void onTimer() {
		Iterator<String> keys = sessions.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			UPnPDeviceSession session = sessions.get(key);
			if (session.expired()) {
				onDeviceSessionRemoved(session);
				keys.remove();
			}
		}
	}

	public void onDeviceSessionRemoved(UPnPDeviceSession session) {
		String udn = session.getUdn();
		Iterator<String> sids = subscriptions.keySet().iterator();
		while (sids.hasNext()) {
			String sid = sids.next();
			if (subscriptions.get(sid).getUdn().equals(udn)) {
				sids.remove();
			}
		}
	}

	public void subscribeEvent(UPnPEventSubscription subscription) throws IOException {
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("CALLBACK", subscription.getCallbackUrlsString());
		headers.put("NT", "upnp:event");
		headers.put("TIMEOUT", "Second-" + subscription.getTimeoutSec());
		HttpClient client = new HttpClient();
		HttpResponse response = client.doRequest(subscription.getEventSubUrl(), "SUBSCRIBE", headers, null);
		String sid = response.getHeader("SID");
		subscription.setSid(sid);
		subscriptions.put(sid, subscription);
	}

	public void unsubscribeEvent(UPnPEventSubscription subscription) throws IOException {
		subscriptions.remove(subscription.getSid());
		HttpClient client = new HttpClient();
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("SID", subscription.getSid());
		client.doRequest(subscription.getEventSubUrl(), "UNSUBSCRIBE", headers, null);
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
			session.setBaseUrl(header.getHeader("location"));
			session.setDevice(builder.build(header.getHeader("location")));
			session.setStatus(UPnPDeviceSessionStatus.COMPLETE);
			sessions.put(usn.getUuid(), session);
		} catch (FileNotFoundException e) {
			logger.error("file not found: " + e.getMessage());
		} catch (IOException e) {
			logger.error("io exception: " + e.getMessage());
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

		new Thread(cp.getRunnable()).start();

		cp.msearch("ssdp:all", 3);

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
					UPnPActionRequest request = new UPnPActionRequest(service, "SetTarget");
					request.setParameter("NewTargetValue", "1");
					UPnPActionResponse resp = session.invokeAction(request);
				} else if (session.getDeviceType().equals("urn:schemas-upnp-org:device:MediaServer:1")) {
					List<UPnPService> services = session.getDevice().getServiceList();
					UPnPService service = session.getService("urn:schemas-upnp-org:service:ContentDirectory:1");

					UPnPActionRequest request = new UPnPActionRequest(service, "Browse");
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
		cp.stop();
	}
}

