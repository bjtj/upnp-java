package com.tjapp.upnp;

import java.io.*;
import java.util.*;
import java.net.*;

class HttpServer {

	private Map<String, Handler> binder = new LinkedHashMap<>();
	private int port;
	private boolean done;
	private static Logger logger = Logger.getLogger("HttpServer");
	

	/**
	 * 
	 *
	 */
	public interface Handler {
		public HttpResponse handle(HttpRequest request);
	}

	/**
	 * 
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
		try {
			ServerSocket server = new ServerSocket(port);
			while (done == false) {
				Socket client = server.accept();
				onAccept(client);
			}
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onAccept(Socket client) {
		HttpClientThread t = new HttpClientThread(client);
		t.start();
	}
	
	public static void main(String[] args) {
		HttpServer server = new HttpServer(8080);
		server.bind("/", new Handler() {
				public HttpResponse handle(HttpRequest request) {
					HttpResponse response = new HttpResponse();
					response.setData("hello".getBytes());
					return response;
				}
			});
		server.run();
	}

	
	/**
	 * 
	 *
	 */
	enum ReadWriteState {
		READ_HEADER, READ_BODY, WRITE_HEADER, WRITE_BODY;
	}

	/**
	 * 
	 *
	 */
	private class HttpClientThread extends Thread {
		
		private Socket client;
		
		public HttpClientThread (Socket client) {
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
				int write_len = 0;
				int c = -1;
				while (done == false) {
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
							request.header = parseHeader(sb.toString());
							length = request.getContentLength();
							if (length > 0) {
								request.buffer = new byte[length];
								write_len = 0;
								readWriteState = ReadWriteState.READ_BODY;
							} else {
								response = handle(request);
								readWriteState = ReadWriteState.WRITE_HEADER;
							}
						}
						break;
					case READ_BODY:
						request.buffer[write_len++] = (byte)c;
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
						done = true;
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
			}
		}

		/**
		 * 
		 *
		 */
		public HttpResponse handle(HttpRequest request) {
			Handler handler = binder.get(request.getPath());
			if (handler != null) {
				return handler.handle(request);
			}
            return new HttpResponse();
		}

		/**
		 * 
		 *
		 */
		public HttpHeader parseHeader(String headerString) {
            HttpHeaderParser parser = new HttpHeaderParser();
			return parser.parse(headerString);
		}
	}
}
