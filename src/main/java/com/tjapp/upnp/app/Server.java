package com.tjapp.upnp.app;

import com.tjapp.upnp.*;
import java.io.*;

class Server{
    public static void main(String args[]) throws Exception {

	boolean done = false;
	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
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

		
	while(!done) {
	    print("> ");
	    String line = reader.readLine();
	    if (line.equals("quit") || line.equals("q")) {
		break;
	    }
	}

	server.notifyByebye(device);

	println("[stopping....]");
	server.stop();
	println("[upnp server] done");

    }

    private static void println(String str) {
	System.out.println(str);
    }

    private static void print(String str) {
	System.out.print(str);
    }
}
