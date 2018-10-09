package com.tjapp.upnp;

import java.io.*;
import java.util.*;
import java.net.*;

public class UPnPControlPoint {

	private static Logger logger = Logger.getLogger("UPnPControlPoint");
	private int port;
	private Map<String, UPnPDeviceSession> sessions = new HashMap<>();
	private Map<String, UPnPEventSubscription> subscriptions = new HashMap<>();
	private HttpServer httpServer;
	private boolean finishing;
	private TimerThread timerThread;
	private SSDPReceiver ssdpReceiver;
	private List<OnDeviceListener> deviceListenerList = new ArrayList<>();
	private List<OnEventListener> eventListenerList = new ArrayList<>();

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
					if (ssdp.isNotifyAlive()) {
						dispatch(ssdp);
					} else if (ssdp.isNotifyByebye()) {
						removeDevice(Usn.fromString(ssdp.getHeader("USN")).getUuid());
					}
				}
			});
		new Thread(ssdpReceiver.getRunnable()).start();
		httpServer = new HttpServer(port);
		httpServer.bind("/event", new HttpServer.Handler() {
				public HttpResponse handle(HttpRequest request) throws Exception {
					HttpResponse response = new HttpResponse(200);
					String sid = request.getHeader("SID");
					logger.debug("event - SID: " + sid);
					UPnPEvent event = UPnPEvent.fromXml(request.text());
					event.setSid(sid);
					for (OnEventListener listener : eventListenerList) {
						listener.onEvent(event);
					}
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
		if (session.isExpired()) {
		    for (OnDeviceListener listener : deviceListenerList) {
			listener.onDeviceRemoved(session.getDevice());
		    }
		    removeSubscription(session.getDevice());
		    keys.remove();
		}
	    }
	}

	public void removeSubscription(UPnPDevice device) {
		Iterator<String> sids = subscriptions.keySet().iterator();
		while (sids.hasNext()) {
			String sid = sids.next();
			if (device.contains(subscriptions.get(sid).getService())) {
				sids.remove();
			}
		}
	}

	public String getCallbackUrl() throws IOException {
		String addr = NetworkManager.getIpv4().getHostAddress();
		return "http://" + addr + ":" + httpServer.getPort() + "/event" ;
	}

	public UPnPEventSubscription subscribeEvent(UPnPDeviceSession session, UPnPService service) throws Exception {
		UPnPEventSubscription subscription = new UPnPEventSubscription(service);
		subscription.addCallbackUrl(getCallbackUrl());
		subscription.setTimeoutSec(30);
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("CALLBACK", subscription.getCallbackUrlsString());
		headers.put("NT", "upnp:event");
		headers.put("TIMEOUT", "Second-" + subscription.getTimeoutSec());
		HttpClient client = new HttpClient();
		URL url = new URL(session.getBaseUrl(), service.getEventSubUrl());
		HttpResponse response = client.doRequest(url, "SUBSCRIBE", headers, null);
		String sid = response.getHeader("SID");
		subscription.setSid(sid);
		subscriptions.put(sid, subscription);
		return subscription;
	}

	public void unsubscribeEvent(UPnPDeviceSession session, UPnPService service) throws IOException {
		unsubscribeEvent(session, getEventSubscription(service));
	}

	public void unsubscribeEvent(UPnPDeviceSession session, UPnPEventSubscription subscription) throws IOException {
		subscriptions.remove(subscription.getSid());
		HttpClient client = new HttpClient();
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("SID", subscription.getSid());
		URL url = new URL(session.getBaseUrl(), subscription.getService().getEventSubUrl());
		client.doRequest(url, "UNSUBSCRIBE", headers, null);
	}

	public UPnPEventSubscription getEventSubscription(UPnPService service) {
		Iterator<String> keys = subscriptions.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			UPnPEventSubscription subscription = subscriptions.get(key);
			if (subscription.getService() == service) {
				return subscription;
			}
		}
		return null;
	}

    public void msearchAsync(String query, int mx) {
	new Thread(new Runnable() {
		public void run() {
		    try {
			UPnPControlPoint.this.msearch(query, mx);
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}
	    }).start();
    }

	public void msearch(String query, int mx) throws IOException {
		SSDPMsearchSender sender = new SSDPMsearchSender(query, mx);
		sender.setOnSSDPHandler(new OnSSDPHandler() {
			public void handle(SSDPHeader header) {
			    UPnPControlPoint.this.dispatch(header);
			}
		    });
		sender.send();
		while (sender.timeout() == false) {
			sender.pending(10);
		}
		sender.close();
	}

	private void dispatch(SSDPHeader header) {
		UPnPDeviceBuilder builder = UPnPDeviceBuilder.getInstance();
		try {
			Usn usn = Usn.fromString(header.getHeader("usn"));
			UPnPDeviceSession session = sessions.get(usn.getUuid());
			if (session != null) {
			    session.renewTimeout();
				return;
			}
			URL location = new URL(header.getHeader("location"));
			addDevice(location, builder.build(location));
		} catch (FileNotFoundException e) {
			logger.error("file not found: " + e.getMessage());
		} catch (IOException e) {
			logger.error("io exception: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addDevice(URL baseUrl, UPnPDevice device) {
		sessions.put(device.getUdn(), UPnPDeviceSession.withDevice(baseUrl, device));
		for (OnDeviceListener listener : deviceListenerList) {
			listener.onDeviceAdded(device);
		}
	}

	public void removeDevice(String udn) {
		UPnPDeviceSession session = sessions.get(udn);
		if (session != null) {
			removeDevice(session.getDevice());
		}
	}

	public void removeDevice(UPnPDevice device) {
		for (OnDeviceListener listener : deviceListenerList) {
			listener.onDeviceRemoved(device);
		}
		removeSubscription(device);
		sessions.remove(device.getUdn());
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

	public void addDeviceListener(OnDeviceListener listener) {
		deviceListenerList.add(listener);
	}

	public void removeDeviceListener(OnDeviceListener listener) {
		deviceListenerList.remove(listener);
	}

	public void addEventListener(OnEventListener listener) {
		eventListenerList.add(listener);
	}

	public void removeEventListener(OnEventListener listener) {
		eventListenerList.remove(listener);
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
				if (session.getDeviceType().equals("urn:schemas-upnp-org:device:BinaryLight:1")) {
					UPnPService service = session.getService("urn:schemas-upnp-org:service:SwitchPower:1");
					UPnPActionRequest request = new UPnPActionRequest(service.getControlUrl(), service.getServiceType(), "SetTarget");
					request.setParameter("NewTargetValue", "1");
					UPnPActionResponse resp = session.invokeAction(request);
				} else if (session.getDeviceType().equals("urn:schemas-upnp-org:device:MediaServer:1")) {
					List<UPnPService> services = session.getDevice().getServiceList();
					UPnPService service = session.getService("urn:schemas-upnp-org:service:ContentDirectory:1");

					UPnPActionRequest request = new UPnPActionRequest(service.getControlUrl(), service.getServiceType(), "Browse");
					request.setParameter("ObjectID", "0");
					request.setParameter("BrowseFlag", "BrowseDirectChildren");
					request.setParameter("Filter", "*");
					request.setParameter("StartingIndex", "0");
					request.setParameter("RequestedCount", "0");
					request.setParameter("SortCriteria", "");
					UPnPActionResponse resp = session.invokeAction(request);
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

