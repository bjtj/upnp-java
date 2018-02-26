package com.tjapp.upnp;

import java.util.*;

// https://tools.ietf.org/html/rfc4122.html
class Uuid {
	String uuid;

	public Uuid () {
	}

	public Uuid (String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public static Uuid generate(int seq, int[] nodes) {
		long time = new Date().getTime();
		time += 0x01B21DD213814000L; // Jan. 1, 1970 base => Oct. 15, 1582
		String str = String.format("%08x-%04x-%04x-%04x-",
								   (time & 0xffffffffL),
								   ((time >> 32) & 0xffff),
								   (((time >> 48) & 0x0fff) | 0x1fff),
								   ((seq & 0x3fff) | 0x8000));
		for (int i = 0; i < 6; i++) {
			if (nodes != null && i < nodes.length) {
				str += String.format("%02x", nodes[i]);
			} else {
				str += "00";
			}
		}
		return new Uuid(str);
	}

	public static Uuid random() {
		return new Uuid(UUID.randomUUID().toString());
	}

	public String toString() {
		return uuid;
	}

	public static void main(String[] args) {
		System.out.println("" + UUID.randomUUID());
	}
}
