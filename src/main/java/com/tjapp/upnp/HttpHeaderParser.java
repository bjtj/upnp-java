package com.tjapp.upnp;

class HttpHeaderParser {

	public static String[] parseHeaderField(String line) {
		int idx = line.indexOf(":");
		if (idx >= 0) {
			String key = line.substring(0, idx);
			String value = line.substring(idx + 1).trim();
			return new String[]{key, value};
		}
		return new String[]{line, null};
	}

	public static HttpHeader parse(String text) {
		HttpHeader header = new HttpHeader();
		String[] lines = text.split("\r\n");
		header.setFirstLine(lines[0]);
		for (int i = 1; i < lines.length; i++) {
			String[] tokens = parseHeaderField(lines[i]);
			header.appendHeader(tokens[0], tokens[1]);
		}
		return header;
	}
}
