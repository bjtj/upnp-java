package com.tjapp.upnp;

import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

class UPnPDevice {

	private static Logger logger = Logger.getLogger("UPnPDevice");
	static {
		logger.setWriter(Logger.NULL_WRITER);
	}
	private Map<String, UPnPProperty> properties = new LinkedHashMap<>();
	private List<UPnPDevice> childDevices = new ArrayList<>();
	private List<UPnPService> services = new ArrayList<>();

	public UPnPDevice () {		
	}

	public String getUdn() {
		return properties.get("UDN").getValue();
	}

	public String getFriendlyName() {
		return properties.get("friendlyName").getValue();
	}

	public void setProperty(UPnPProperty property) {
		properties.put(property.getName(), property);
	}

	public void addChildDevice(UPnPDevice device) {
		childDevices.add(device);
	}

	public void removeChildDevice(UPnPDevice device) {
		childDevices.remove(device);
	}

	public List<UPnPDevice> getChildDevices() {
		return childDevices;
	}

	public void addService(UPnPService service) {
		services.add(service);
	}

	public void removeService(UPnPService service) {
		services.remove(service);
	}

	public List<UPnPService> getServiceList() {
		return services;
	}

	public static UPnPDevice fromNodeList(NodeList list) {
		UPnPDevice device = new UPnPDevice();
		for (int j = 0; j < list.getLength(); j++) {
			Node node = list.item(j);
			String name = node.getNodeName();
			if (name.equals("#text")) {
				continue;
			}
			Node first = node.getFirstChild();
			if (node.getChildNodes().getLength() == 1 && first.getNodeName().equals("#text")) {
				UPnPProperty property = new UPnPProperty(name, first.getNodeValue());
				logger.debug(" - prop: " + property);
				device.setProperty(property);
			} else if (name.equals("deviceList")) {
				NodeList deviceNodeList = node.getChildNodes();
				for (int i = 0; i < deviceNodeList.getLength(); i++) {
					if (deviceNodeList.item(i).getNodeName().equals("device")) {
						logger.debug(" - {enter device}");
						device.addChildDevice(fromNodeList(deviceNodeList.item(i).getChildNodes()));
						logger.debug(" - {leave device}");
					}
				}
			} else if (name.equals("serviceList")) {
				NodeList serviceNodeList = node.getChildNodes();
				for (int i = 0; i < serviceNodeList.getLength(); i++) {
					if (serviceNodeList.item(i).getNodeName().equals("service")) {
						UPnPService service = UPnPService.fromNodeList(serviceNodeList.item(i).getChildNodes());
						device.addService(service);
					}
				}
			} else {
				logger.debug(" - else: " + name);
			}
		}
		return device;
	}

	public static UPnPDevice fromXml(String xml) throws Exception {
		UPnPDevice device = null;
		Document doc = XmlParser.parse(xml);
		Element root = doc.getDocumentElement();
		NodeList list = root.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			String name = node.getNodeName();
			if (name.equals("#text")) {
				continue;
			}
			logger.debug("name: " + name);
			if (name.equals("device")) {
				device = fromNodeList(node.getChildNodes());
			} else {
				logger.debug(" - {skip}");
			}
		}
		return device;
	}
}
