package com.tjapp.upnp;

import java.io.*;
import java.util.*;
import java.net.*;

class HttpServer {

	private Map<String, Handler> binding = new LinkedHashMap<>();
	private int port;
	private boolean done;

	private static void logd(String msg) {
		System.out.println(msg);
	}

	private interface Handler {
		public Response handle(Request request);
	}

	public HttpServer () {
		this.port = 80;
	}

	public HttpServer (int port) {
		this.port = port;
	}
	
	public void bind(String pattern, Handler handler) {
		binding.put(pattern, handler);
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
				public Response handle(Request request) {
					Response response = new Response();
					response.setData("hello".getBytes());
					return response;
				}
			});
		server.run();
	}

	private static class Request {
		public HttpHeader header;
		public byte[] buffer;

		public String getPath() {
            return header.getFirstParts()[1];
		}

		public int getContentLength() {
			String len = header.getHeader("Content-Length");
			return (len == null ? -1 : Integer.parseInt(len));
		}

		public String getContentType() {
			return header.getHeader("Content-Type");
		}
	}

	private static class Response {
		public HttpHeader header;
		public byte[] data;
		
		public Response () {
			this.header = new HttpHeader();
			this.header.setFirstLine("HTTP/1.1 200 OK");
		}
		
		public Response (HttpHeader header, byte[] data) {
			this.header = header;
			this.data = data;
		}
		public int getContentLength() {
			String len = header.getHeader("Content-Length");
			return (len == null ? -1 : Integer.parseInt(len));
		}

		public String getContentType() {
			return header.getHeader("Content-Type");
		}

		public void setData(byte[] data) {
			this.data = data;
			header.setHeader("Content-Length", Integer.toString(data.length));
		}
	}

	enum ReadWriteState {
		READ_HEADER, READ_BODY, WRITE_HEADER, WRITE_BODY;
	}

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
				Request request = new Request();
				Response response = null;
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
							logd(sb.toString());
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
				logd("[connection close]");
				try {
					client.close();
				} catch (Exception e) {
				}
			}
		}

		public Response handle(Request request) {
			Handler handler = binding.get(request.getPath());
			if (handler != null) {
				return handler.handle(request);
			}
            return new Response();
		}

		public HttpHeader parseHeader(String headerString) {
            HttpHeaderParser parser = new HttpHeaderParser();
			return parser.parse(headerString);
		}
	}
}
