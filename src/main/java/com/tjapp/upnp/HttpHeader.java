package com.tjapp.upnp;

import java.util.*;

class HttpHeader {

	private static Logger logger = Logger.getLogger("HttpHeader");

	/**
	 * http header fields
	 *
	 */
	public class HeaderFields extends LinkedHashMap<String, List<String>> {

		Map<String, String> keymap = new HashMap<>();
		
		@Override
		public List<String> put(String key, List<String> value) {
			keymap.put(key.toLowerCase(), key);
            return super.put(key, value);
		}
		public List<String> get(String key) {
            return super.get(keymap.get(key.toLowerCase()));
		}
		@Override
		public void clear() {
			super.clear();
			keymap.clear();
		}
	}

	private String firstLine;
	private HeaderFields headerFields = new HeaderFields();

	public void clear() {
		firstLine = null;
		headerFields.clear();
	}

	public String getFirstLine() {
		return firstLine;
	}

	public void setFirstLine(String firstLine) {
		this.firstLine = firstLine;
	}

	public String[] getFirstParts() {
		String[] ret = firstLine.split("\\s+");
		assert ret.length == 3;
		return ret;
	}

	public int getContentLength() {
		if (getHeader("Content-Length") == null) {
			return -1;
		}
		return Integer.parseInt(getHeader("Content-Length"));
	}

	public void setContentLength(int length) {
		setHeader("Content-Length", Integer.toString(length));
	}

	public String getContentType() {
		return getHeader("Content-Type");
	}

	public void setContentType(String contentType) {
		setHeader("Content-Type", contentType);
	}
	
	public String getHeader(String name) {
		List<String> values = headerFields.get(name);
		if (values != null && values.size() > 0) {
			return values.get(0);
		}
		return null;
	}
	public String[] getHeaders(String name) {
		List<String> values = headerFields.get(name);
		if (values != null) {
			return values.toArray(new String[values.size()]);
		}
		return null;
	}
	
	public void setHeader(String name, String value) {
		List<String> values = headerFields.get(name);
		if (values == null) {
			values = new ArrayList();
			values.add(value);
			headerFields.put(name, values);
		} else {
			values.clear();
			values.add(value);
		}
	}
	
	public void setHeaders(String name, String[] values) {
		headerFields.put(name, new ArrayList<>(Arrays.asList(values)));
	}

	public void setHeaders(String name, List<String> values) {
		headerFields.put(name, values);
	}
	
	public void appendHeader(String name, String value) {
		List<String> values = headerFields.get(name);
		if (values == null) {
			values = new ArrayList();
			values.add(value);
			headerFields.put(name, values);
		} else {
			values.add(value);
		}
	}
	
	public void removeHeader(String name) {
		headerFields.remove(name);
	}

	public HeaderFields getHeaderFields() {
		return headerFields;
	}

	public void setHeaderFields(Map<String, List<String>> headerFields) {
		this.headerFields.clear();
		Iterator<String> keys = headerFields.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key != null) {
				this.headerFields.put(key, new ArrayList<String>(headerFields.get(key)));
			}
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(firstLine + "\r\n");
		Iterator<String> iter = headerFields.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			List<String> values = headerFields.get(key);
			for (String value : values) {
				sb.append(key + ": " + value + "\r\n");
			}
		}
		sb.append("\r\n");
		return sb.toString();
	}

	public void copy(HttpHeader header) {
		firstLine = header.getFirstLine();
		headerFields = header.getHeaderFields();
	}

	public static String[] parseHeaderField(String line) {
		int idx = line.indexOf(":");
		if (idx >= 0) {
			String key = line.substring(0, idx);
			String value = line.substring(idx + 1).trim();
			return new String[]{key, value};
		}
		return new String[]{line, null};
	}

	public static HttpHeader fromString(String text) {
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
