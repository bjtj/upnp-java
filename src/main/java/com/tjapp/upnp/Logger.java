package com.tjapp.upnp;

import java.util.*;
import java.text.*;

class Logger {

	private String tag;
	private SimpleDateFormat sdf = new SimpleDateFormat("Y-MM-dd HH:mm:ss.S z");
	
	public static void main(String args[]) {
	}

	private String format(String tag, String level, String msg) {
		return String.format("[%s] %s %s %s", sdf.format(new Date()), tag, level, msg);
	}

	public static Logger getLogger(String tag) {
		return new Logger(tag);
	}

	private Logger (String tag) {
		this.tag = tag;
	}

	public void debug(String msg) {
		System.out.println(format(tag, "D", msg));
	}

	public void verbose(String msg) {
		System.out.println(format(tag, "V", msg));
	}

	public void error(String msg) {
		System.out.println(format(tag, "E", msg));
	}

	public void info(String msg) {
		System.out.println(format(tag, "I", msg));
	}

	public void fatal(String msg) {
		System.out.println(format(tag, "F", msg));
	}

	public void trace(String msg) {
		System.out.println(format(tag, "T", msg));
	}
}
