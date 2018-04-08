package com.tjapp.app;

import com.tjapp.upnp.*;
import java.util.*;
import java.io.*;


class ControlPoint {
	
	public static void main(String[] args) throws Exception {
		UPnPControlPoint cp = new UPnPControlPoint(9000);
		cp.run();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("> ");
			String line = reader.readLine();
			if (line.equals("q") || line.equals("quit")) {
				break;
			}
			if (line.startsWith("search")) {
				cp.msearch(line.substring("search ".length()), 5);
			}
			if (line.startsWith("ls")) {
			}
		}
		System.out.println("[quit]");
	}
}
