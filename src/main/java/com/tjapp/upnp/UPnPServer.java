package com.tjapp.upnp;

import java.util.*;


class UPnPServer {

	private int port;
	private HttpServer httpServer;
	private SSDPReceiver ssdpReceiver;
	private Map<String, UPnPDeviceSession> sessions = new HashMap<>();
	private TimerThread timerThread;
	private Logger logger = Logger.getLogger("UPnPServer");

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
				public HttpResponse handle(HttpRequest request) {
					logger.debug(request.getPath());
					HttpResponse response = new HttpResponse(200);
					// device description
					// scpd
					// control
					// event subscribe
					return response;
				}
			});
		httpServer.run();
	}

	public void stop() {
		timerThread.finish();
		timerThread = null;
		httpServer.stop();
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
