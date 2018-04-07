package com.tjapp.upnp;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.tjapp.upnp.*;
import java.io.*;
import java.net.*;

public class TestHttpServer {

	private static Logger logger = Logger.getLogger("TestHttpServer");
	
	@Test
	public void test_run() throws Exception {
		new Thread(new Runnable() {
				public void run() {
					HttpServer server = new HttpServer(9900);
					server.bind("/", new HttpServer.Handler() {
							public HttpResponse handle(HttpRequest request) {
								HttpResponse response = new HttpResponse();
								response.setData("hello".getBytes());
								return response;
							}
						});
					logger.debug("[start server]");
					server.run();
				}
			}).start();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		URL url = new URL("http://localhost:9900/");
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
		String line = reader.readLine();
		assertEquals(line, "hello");
	}
}
