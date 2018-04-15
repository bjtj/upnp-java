package com.tjapp.upnp;

import java.util.regex.*;

public class StringUtil {

	// https://stackoverflow.com/a/15567045/5676460
	private final static Pattern LTRIM = Pattern.compile("^\\s+");

	public static String ltrim(String text) {
		return LTRIM.matcher(text).replaceAll("");
	}

	public static String yesNo(boolean b) {
		return b ? "yes" : "no";
	}

	public static String yesNo(int num) {
		return num == 0 ? "no" : "yes";
	}

	public static String quote(String text) {
		return "\"" + text.replaceAll("\"", "\\\"") + "\"";
	}

	public static String escape(String str) {
		return str.replaceAll("\"", "\\\"");
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static String wrap(String text, String start, String end) {
		return start + text + end;
	}

	public static String unwrap(String text, String start, String end) {
		if (text.startsWith(start)) {
			text = text.substring(start.length());
		}
		if (text.endsWith(end)) {
			text = text.substring(0, text.length() - end.length());
		}
		return text;
	}

	public static String join(String[] strArr, String glue) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < strArr.length; i++) {
			String str = strArr[i];
			if (i > 0) {
				sb.append(glue);
			}
			sb.append(str);
		}
		return sb.toString();
	}
}
