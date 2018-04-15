package com.tjapp.upnp;

public interface OnDeviceListener {
	public void onDeviceAdded(UPnPDevice device);
	public void onDeviceRemoved(UPnPDevice device);
}
