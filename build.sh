#!/bin/bash

mkdir -p build
javac -d build src/com/tjapp/upnp/Main.java \
	  src/com/tjapp/upnp/SSDPServer.java
