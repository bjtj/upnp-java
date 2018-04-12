package com.tjapp.upnp;

import java.io.*;
import java.util.*;


class UPnPServer {

	private int port;
	private HttpServer httpServer;
	private SSDPReceiver ssdpReceiver;
	private Map<String, UPnPDeviceSession> sessions = new HashMap<>();
	private TimerThread timerThread;
	private Map<String, UPnPEventSubscription> subscriptions = new LinkedHashMap<>();
	private List<UPnPActionRequestHandler> actionHandlers = new ArrayList<>();
	private Logger logger = Logger.getLogger("UPnPServer");


	/**
	 * 
	 *
	 */
	private class TimerThread extends Thread {
		private boolean finishing;
		private UPnPServer server;
		private long tick;
		private long interval;
		public TimerThread(UPnPServer server, long interval) {
			this.server = server;
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
						server.onTimer();
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
	

	public UPnPServer (int port) {
		this.port = port;
	}

	public void onTimer() {
	}

	public void addDevice(UPnPDevice device) {
		UPnPDeviceSession session = new UPnPDeviceSession();
		session.setDevice(device);
	}

	public void removeDevice(UPnPDevice device) {
		sessions.remove(device.getUdn());
	}
	
	public void run() {
		timerThread = new TimerThread(this, 10 * 1000);
		timerThread.start();
		ssdpReceiver = new SSDPReceiver(SSDP.MCAST_PORT);
		new Thread(ssdpReceiver.getRunnable()).start();
		httpServer = new HttpServer(port);
		httpServer.bind("/upnp/.*", new HttpServer.Handler() {
				public HttpResponse handle(HttpRequest request) throws Exception {
					logger.debug(request.getPath());
					String[] tokens = request.getPath().split("/");
					String udn = tokens[1];
					String type = tokens[tokens.length - 1];
					UPnPDeviceSession session = sessions.get(udn);
					if (session == null) {
						return new HttpResponse(404);
					}
					// device description
					if (type.equals("device.xml")) {
						HttpResponse response = new HttpResponse(200);
						response.setData(session.getDevice().toXml());
						return response;
					}
					// scpd
					if (type.equals("scpd.xml")) {
						HttpResponse response = new HttpResponse(200);
						String serviceType = tokens[2];
						response.setData(session.getService(serviceType).getScpd().toXml());
						return response;
					}
					// control
					if (type.equals("control.xml")) {
						if (type.equals("scpd.xml")) {
						HttpResponse response = new HttpResponse(200);
						String serviceType = tokens[2];
						response.setData(
							onActionRequest(
								UPnPActionRequest.fromXml(request.text())).toXml());
						return response;
						}
					}
					// event subscribe
					if (type.equals("subscribe.xml")) {
						UPnPEventSubscription subscription = onEventSubsribe(request);
						HttpResponse response = new HttpResponse(200);
						response.setHeader("SID", subscription.getSid());
						response.setHeader("TIMEOUT", "Second-" + subscription.getTimeoutSec());
						return response;
					}
					
					return new HttpResponse(404);
				}
			});;
		httpServer.run();
	}

	public Runnable getRunnable() {
		return new Runnable() {
			public void run() {
				UPnPServer.this.run();
			}
		};
	}

	public UPnPActionResponse onActionRequest(UPnPActionRequest request) {
		for (UPnPActionRequestHandler handler : actionHandlers) {
			return handler.handle(request);
		}
		return null;
	}

	public UPnPEventSubscription onEventSubsribe(HttpRequest request) {
		request.getHeader("NT");
		String[] urls = StringUtil.unwrap(request.getHeader("CALLBACK"), "<", ">").split(" ");
		int timeout = Integer.parseInt(request.getHeader("TIMEOUT").substring("Second-".length()));
		UPnPEventSubscription subscription = new UPnPEventSubscription();
		String sid = Uuid.random().toString();
		subscription.setSid(sid);
		subscription.setCallbackUrls(new ArrayList<>(Arrays.asList(urls)));
		subscription.setTimeoutSec(timeout);
		subscriptions.put(sid, subscription);
		return subscription;
	}

	public void stop() {
		timerThread.finish();
		timerThread = null;
		httpServer.stop();
	}

	public String getLocationUrl(UPnPDevice device) {
		// String addr = NetworkManager.getIpv4().getHostAddress();
		String addr = httpServer.getInetAddress().getHostAddress();
		logger.debug("location: " + addr);
		return "http://" + addr + ":" + httpServer.getPort()
			+ "/" + device.getUdn() + "/device.xml" ;
	}

	public void notifyAlive(UPnPDevice device) throws IOException {
		MulticastSender sender = new MulticastSender();

		HttpHeader header = new HttpHeader();
		header.setFirstLine("NOTIFY * HTTP/1.1");
		header.setHeader("HOST", SSDP.MCAST_GROUP + ":" + SSDP.MCAST_PORT);
		header.setHeader("Cache-Control", "max-age=1800");
		header.setHeader("Location", getLocationUrl(device));
		header.setHeader("Server", Config.SERVER_NAME);
		header.setHeader("NTS", "ssdp:alive");
		
		// uuid only
		header.setHeader("NT", device.getUdn());
		header.setHeader("USN", device.getUdn());
		sender.send(header.toString());
		// uuid :: upnp:rootdevice
		header.setHeader("NT", "upnp:rootdevice");
		header.setHeader("USN", device.getUdn() + "::upnp:rootdevice");
		sender.send(header.toString());
		// each device
		header.setHeader("NT", device.getDeviceType());
		header.setHeader("USN", device.getUdn() + "::" + device.getDeviceType());
		sender.send(header.toString());
		// each services
		List<UPnPService> serviceList = device.getServiceList();
		for (UPnPService service : serviceList) {
			header.setHeader("NT", service.getServiceType());
			header.setHeader("USN", device.getUdn() + "::" + service.getServiceType());
			sender.send(header.toString());
		}

		sender.close();
	}

	public void notifyUpdate(UPnPDevice device) {
		// 
	}

	public void notifyByebye(UPnPDevice device) throws IOException {
		MulticastSender sender = new MulticastSender();

		HttpHeader header = new HttpHeader();
		header.setFirstLine("NOTIFY * HTTP/1.1");
		header.setHeader("HOST", SSDP.MCAST_GROUP + ":" + SSDP.MCAST_PORT);
		header.setHeader("NTS", "ssdp:byebye");
		
		// uuid only
		header.setHeader("NT", device.getUdn());
		header.setHeader("USN", device.getUdn());
		sender.send(header.toString());
		// uuid :: upnp:rootdevice
		header.setHeader("NT", "upnp:rootdevice");
		header.setHeader("USN", device.getUdn() + "::upnp:rootdevice");
		sender.send(header.toString());
		// each device
		header.setHeader("NT", device.getDeviceType());
		header.setHeader("USN", device.getUdn() + "::" + device.getDeviceType());
		sender.send(header.toString());
		// each services
		List<UPnPService> serviceList = device.getServiceList();
		for (UPnPService service : serviceList) {
			header.setHeader("NT", service.getServiceType());
			header.setHeader("USN", device.getUdn() + "::" + service.getServiceType());
			sender.send(header.toString());
		}

		sender.close();
	}

	public void addActionRequestHandler(UPnPActionRequestHandler handler) {
		actionHandlers.add(handler);
	}

	public void removeActionRequestHandler(UPnPActionRequestHandler handler) {
		actionHandlers.remove(handler);
	}

	public static void main(String[] args) {
		UPnPServer server = new UPnPServer(8888);
		new Thread(server.getRunnable()).start();
		// add device
		// notify alive
		server.stop();
	}
}
