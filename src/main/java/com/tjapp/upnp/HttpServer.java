package com.tjapp.upnp;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.*;

/**
 * http server
 *
 */
class HttpServer {

	private Binder binder = new Binder();
	private int port;
	private boolean finishing;
	private boolean running;
	private static Logger logger = Logger.getLogger("HttpServer");
	private ServerSocket server;
	private Map<Socket, HttpClientThread> clientHandlers = new HashMap<>();

	/**
	 * 
	 *
	 */
	private class Binder extends HashMap<String, Handler> {
		// https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
		private Map<String, Pattern> patterns = new HashMap<>();
		@Override
		public Handler put(String key, Handler handler) {
			patterns.put(key, Pattern.compile(key));
			return super.put(key, handler);
		}
		public Handler get(String query) {
			Iterator<String> keys = patterns.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				Matcher m = patterns.get(key).matcher(query);
				if (m.matches()) {
					return super.get(key);
				}
			}
			return null;
		}
		@Override
		public void clear() {
			super.clear();
			patterns.clear();
		}
	}
	

	/**
	 * handler
	 *
	 */
	public interface Handler {
		public HttpResponse handle(HttpRequest request) throws Exception;
	}

	/**
	 * Constructor
	 *
	 */
	public HttpServer () {
		this.port = 80;
	}

	public HttpServer (int port) {
		this.port = port;
	}
	
	public void bind(String pattern, Handler handler) {
		binder.put(pattern, handler);
	}
	
	public void run() {
		if (running) {
			throw new IllegalStateException("http server is already in running state");
		}
		try {
			running = true;
			server = new ServerSocket(port);
			while (Thread.interrupted() == false && finishing == false) {
				Socket client = server.accept();
				onAccept(client);
			}
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		closeAllClients();
		running = false;
		logger.debug("[http server] done");
	}

	public Runnable getRunnable() {
		return new Runnable() {
			public void run() {
				HttpServer.this.run();
			}
		};
	}

	public void closeAllClients() {
		Iterator<Socket> keys = clientHandlers.keySet().iterator();
		while (keys.hasNext()) {
			Socket client = keys.next();
			try {
				client.close();
			} catch (Exception e) {
				logger.warning("client close - " + e.getMessage());
			}
		}
		try {
			while (clientHandlers.size() > 0) {
				Thread.sleep(10);
			}
		}
		catch (InterruptedException e) {
			logger.warning("[ignore] wait all client handlers interrupted - " + e.getMessage());
		}
	}

	public void stop() {
		finishing = true;
		try {
			server.close();
		}
		catch (Exception e) {
			logger.warning("[ignore] server socket close exception - " + e.getMessage());
		}
		try {
			while (running) {
				Thread.sleep(10);
			}
		}
		catch (InterruptedException e) {
			logger.warning("[ignore] wait interrupted - " + e.getMessage());
		}
	}

	public void onAccept(Socket client) {
		HttpClientThread t = new HttpClientThread(this, client);
		t.start();
		clientHandlers.put(client, t);
	}

	public void onDisconnected(Socket client) {
		clientHandlers.remove(client);
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isFinishing() {
		return finishing;
	}

	public InetAddress getInetAddress() {
		return server.getInetAddress();
	}

	public int getPort() {
		return port;
	}
	
	
	/**
	 * read write state
	 *
	 */
	enum ReadWriteState {
		READ_HEADER, READ_BODY, WRITE_HEADER, WRITE_BODY;
	}

	/**
	 * http client thread
	 *
	 */
	private class HttpClientThread extends Thread {

		private HttpServer server;
		private Socket client;
		
		public HttpClientThread (HttpServer server, Socket client) {
			this.server = server;
			this.client = client;
		}
		
		public void run() {
			try {
				boolean done = false;
				StringBuffer sb = new StringBuffer();
				InputStream in = client.getInputStream();
				OutputStream out = client.getOutputStream();
				ReadWriteState readWriteState = ReadWriteState.READ_HEADER;
				int length = 0;
				HttpRequest request = new HttpRequest();
				HttpResponse response = null;
				boolean keepConnect = true;
				int write_len = 0;
				int c = -1;
				while (done == false && server.isFinishing() == false) {
					if (readWriteState == ReadWriteState.READ_HEADER ||
						readWriteState == ReadWriteState.READ_BODY) {
						if ((c = in.read()) <= 0) {
							done = true;
							break;
						}
					}

					switch (readWriteState) {
					case READ_HEADER:
						sb.append((char)c);
						if (sb.indexOf("\r\n\r\n") > 0) {
							logger.debug(sb.toString());
							request.setHttpHeader(HttpHeader.fromString(sb.toString()));
							length = request.getContentLength();
							if (length > 0) {
								request.setData(new byte[length]);
								write_len = 0;
								readWriteState = ReadWriteState.READ_BODY;
							} else {
								response = handle(request);
								readWriteState = ReadWriteState.WRITE_HEADER;
							}
						}
						break;
					case READ_BODY:
						request.getData()[write_len++] = (byte)c;
						if (write_len >= length) {
							response = handle(request);
							readWriteState = ReadWriteState.WRITE_HEADER;
						}
						break;
					case WRITE_HEADER:
						out.write(response.header.toString().getBytes());
						readWriteState = ReadWriteState.WRITE_BODY;
						break;
					case WRITE_BODY:
						if (response.data != null) {
							out.write(response.data);
						}
						if (keepConnect) {
							readWriteState = ReadWriteState.READ_HEADER;
							sb = new StringBuffer();
							length = 0;
							request.clear();
							response = null;
							write_len = 0;
						} else {
							done = true;
						}
						break;
					default:
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				logger.debug("[connection close]");
				try {
					client.close();
				} catch (Exception e) {
				}
				server.onDisconnected(client);
			}
		}

		/**
		 * 
		 *
		 */
		public HttpResponse handle(HttpRequest request) {
			Handler handler = binder.get(request.getPath());
			try {
				if (handler != null) {
					return handler.handle(request);
				}
			} catch (Exception e) {
				return new HttpResponse(500);
			}
            return new HttpResponse(404);
		}
	}

	/**
	 * main
	 *
	 */
	public static void main(String[] args) {
		HttpServer server = new HttpServer(8080);
		server.bind("/", new Handler() {
				public HttpResponse handle(HttpRequest request) {
					HttpResponse response = new HttpResponse(200);
					response.setData("hello".getBytes());
					return response;
				}
			});
		server.run();
	}
}
