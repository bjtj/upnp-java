package com.tjapp.upnp;

import java.util.*;


public class UPnPProperty {

	private static Logger logger = Logger.getLogger("UPnPProperty");

	private String name;
	private String value;

	private Map<String, String> attributes = new LinkedHashMap<>();

	public UPnPProperty () {
		
	}

	public UPnPProperty (String name, String value) {
		this.name = name;
		this.value = value;
	}

	public UPnPProperty (String name, String value, Map<String, String> attributes) {
		this.name = name;
		this.value = value;
		this.attributes = attributes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public String getAttribute(String key) {
		return attributes.get(key);
	}

	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public void removeAttribute(String key) {
		attributes.remove(key);
	}

	public String getAttributeString() {
		StringBuffer sb = new StringBuffer();
		Iterator<String> keys = attributes.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			String value = attributes.get(key);
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(String.format("%s=\"%s\"", key, value));
		}
		return sb.toString();
	}

	private String concat(String a, String b) {
		if (a.isEmpty()) {
			return a;
		}
		if (b.isEmpty()) {
			return a;
		}
		return a + " " + b;
	}

	public String toXml() {
		return String.format("<%s>%s</%s>", concat(name, getAttributeString()), value, name);
	}

	public String toString() {
		return toXml();
	}

	public static void main(String[] args) {
		UPnPProperty prop = new UPnPProperty("uuid", "uuid:xxxxx-xxxx-xxxx-xxxxxxxx");
		logger.debug(prop.toString());
		prop.setAttribute("extra", "attr1");
		logger.debug(prop.toString());
		prop.setAttribute("extra", "attr2");
		logger.debug(prop.toString());
		prop.setAttribute("extra2", "attr3");
		logger.debug(prop.toString());
		prop.removeAttribute("extra2");
		logger.debug(prop.toString());
		prop.removeAttribute("extra");
		logger.debug(prop.toString());
	}
}
