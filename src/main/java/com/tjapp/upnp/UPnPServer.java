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
	private static Logger logger = Logger.getLogger("UPnPServer");


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
		sessions.put(session.getUdn(), session);
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
		httpServer.bind("/upnp/.*", httpRequestHandler);
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

	public String getLocationUrl(UPnPDevice device) throws IOException {
		String addr = NetworkManager.getIpv4().getHostAddress();
		logger.debug("location: " + addr);
		return "http://" + addr + ":" + httpServer.getPort()
			+ "/upnp/" + device.getUdn() + "/device.xml" ;
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

	private HttpServer.Handler httpRequestHandler = new HttpServer.Handler() {
			public HttpResponse handle(HttpRequest request) throws Exception {
				logger.debug(request.getPath());
				String[] tokens = request.getPath().split("/");
				logger.debug("tokens: " + StringUtil.join(tokens, ", "));
				String udn = tokens[2];
				String type = tokens[tokens.length - 1];
				logger.debug("http request for udn: " + udn + " and type: " + type);
				UPnPDeviceSession session = sessions.get(udn);
				if (session == null) {
					logger.debug("session not found");
					return new HttpResponse(404);
				}
				// device description
				if (type.equals("device.xml")) {
					logger.debug("serve device.xml");
					HttpResponse response = new HttpResponse(200);
					String xml = session.getDevice().toXml();
					logger.debug("[device description]");
					logger.debug(xml);
					response.setData(xml);
					return response;
				}
				// scpd
				if (type.equals("scpd.xml")) {
					logger.debug("serve scpd.xml");
					HttpResponse response = new HttpResponse(200);
					String serviceType = tokens[3];
					String xml = session.getService(serviceType).getScpd().toXml();
					logger.debug("[scpd]");
					logger.debug(xml);
					response.setData(xml);
					return response;
				}
				// control
				if (type.equals("control.xml")) {
					logger.debug("serve control.xml");
					if (type.equals("control.xml")) {
						HttpResponse response = new HttpResponse(200);
						String serviceType = tokens[3];
						String xml = onActionRequest(UPnPActionRequest.fromXml(
														 request.text())).toXml();
						logger.debug("[action response]");
						logger.debug(xml);
						response.setData(xml);
						return response;
					}
				}
				// event subscribe
				if (type.equals("subscribe.xml")) {
					logger.debug("serve subscribe.xml");
					UPnPEventSubscription subscription = onEventSubsribe(request);
					HttpResponse response = new HttpResponse(200);
					response.setHeader("SID", subscription.getSid());
					response.setHeader("TIMEOUT", "Second-" + subscription.getTimeoutSec());
					return response;
				}
				logger.debug("not found - " + type);
				return new HttpResponse(404);
			}
		};

	public void addActionRequestHandler(UPnPActionRequestHandler handler) {
		actionHandlers.add(handler);
	}

	public void removeActionRequestHandler(UPnPActionRequestHandler handler) {
		actionHandlers.remove(handler);
	}

	public static void main(String[] args) throws Exception {
		UPnPServer server = new UPnPServer(8888);
		new Thread(server.getRunnable()).start();

		UPnPDevice device = UPnPDeviceBuilder.getInstance().buildResource("/light.xml");
		device.setUdn("uuid:" + Uuid.random().toString());
		device.setScpdUrl("/upnp/$udn/$serviceType/scpd.xml");
		device.setControlUrl("/upnp/$udn/$serviceType/control.xml");
		device.setEventSubUrl("/upnp/$udn/$serviceType/subscribe.xml");
		server.addDevice(device);
		server.addActionRequestHandler(new UPnPActionRequestHandler() {
				public UPnPActionResponse handle(UPnPActionRequest request) {
					UPnPActionResponse response = new UPnPActionResponse();
					response.setServiceType(request.getServiceType());
					response.setActionName(request.getActionName());
					if (request.getServiceType().equals("urn:schemas-upnp-org:service:SwitchPower:1")) {
						if (request.getActionName().equals("GetTarget")) {
							response.setParameter("RetTargetValue", "1");
						}
					}
					return response;
				}
			});
		server.notifyAlive(device);
		
		logger.debug("waiting for 60 seconds...");
		Thread.sleep(60 * 1000);

		server.notifyByebye(device);

		logger.debug("[stopping....]");
		server.stop();
		logger.debug("[upnp server] done");
	}
}
