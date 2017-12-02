package com.tjapp.upnp;

import java.text.*;
import java.util.*;
import java.util.regex.*;

class TextUtil {
	// https://stackoverflow.com/a/15567045/5676460
	private final static Pattern LTRIM = Pattern.compile("^\\s+");

	public static String ltrim(String text) {
		return LTRIM.matcher(text).replaceAll("");
	}
}
