package com.tjapp.upnp;

import java.util.*;
import java.text.*;

class Logger {

	public final static LogWriter CONSOLE_WRITER = new ConsoleWriter();
	public final static LogWriter NULL_WRITER = new NullWriter();

	private String tag;
	private SimpleDateFormat sdf = new SimpleDateFormat("Y-MM-dd HH:mm:ss.S z");
	private LogWriter writer = CONSOLE_WRITER;

	private String format(String tag, String level, String msg) {
		return String.format("[%s] %s %s | %s", sdf.format(new Date()), tag, level, msg);
	}

	public static Logger getLogger(String tag) {
		return new Logger(tag);
	}

	private Logger (String tag) {
		this.tag = tag;
	}

	public void setWriter(LogWriter writer) {
		this.writer = writer;
	}

	public void debug(String msg) {
		writer.print(format(tag, "D", msg));
	}

	public void verbose(String msg) {
		writer.print(format(tag, "V", msg));
	}

	public void error(String msg) {
		writer.print(format(tag, "E", msg));
	}

	public void info(String msg) {
		writer.print(format(tag, "I", msg));
	}

	public void fatal(String msg) {
		writer.print(format(tag, "F", msg));
	}

	public void trace(String msg) {
		writer.print(format(tag, "T", msg));
	}

	/**
	 * 
	 *
	 */
	public interface LogWriter {
		public void print(String text);
	}

	/**
	 * 
	 *
	 */
	public static class ConsoleWriter implements LogWriter {
		public void print(String text) {
			System.out.println(text);
		}
	}

	/**
	 * 
	 *
	 */
	public static class NullWriter implements LogWriter {
		public void print(String text) {
			// ignore
		}
	}

	public static void main(String args[]) {
	}
}
