package com.tjapp.upnp;

import java.io.*;

public interface OnSSDPHandler {
	public void handle(SSDPHeader ssdp) throws IOException;
}
