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
	private List<UPnPService> serviceList = new ArrayList<>();

	public UPnPDevice () {
	}

	public String getUdn() {
		return properties.get("UDN").getValue();
	}

	public void setUdn(String udn) {
		logger.debug("set udn: " + udn);
		setProperty("UDN", udn);
		for (UPnPDevice childDevice : childDevices) {
			childDevice.setUdn(udn);
		}
	}

	public String getFriendlyName() {
		return properties.get("friendlyName").getValue();
	}

	public String getDeviceType() {
		return properties.get("deviceType").getValue();
	}

	public void setScpdUrl(String fmt) {
		fmt = fmt.replaceAll("\\$udn", getUdn());
		for (UPnPService service : serviceList) {
			service.setScpdUrl(fmt.replaceAll("\\$serviceType", service.getServiceType()));
		}
		for (UPnPDevice childDevice : childDevices) {
			childDevice.setScpdUrl(fmt);
		}
	}

	public void setControlUrl(String fmt) {
		fmt = fmt.replaceAll("\\$udn", getUdn());
		for (UPnPService service : serviceList) {
			service.setControlUrl(fmt.replaceAll("\\$serviceType", service.getServiceType()));
		}
		for (UPnPDevice childDevice : childDevices) {
			childDevice.setControlUrl(fmt);
		}
	}

	public void setEventSubUrl(String fmt) {
		fmt = fmt.replaceAll("\\$udn", getUdn());
		for (UPnPService service : serviceList) {
			service.setEventSubUrl(fmt.replaceAll("\\$serviceType", service.getServiceType()));
		}
		for (UPnPDevice childDevice : childDevices) {
			childDevice.setEventSubUrl(fmt);
		}
	}

	public void setProperty(String name, String value) {
		setProperty(new UPnPProperty(name, value));
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
		serviceList.add(service);
	}

	public void removeService(UPnPService service) {
		serviceList.remove(service);
	}

	public UPnPService getService(String serviceType) {
		for (UPnPService service : serviceList) {
			logger.debug("service type: " + service.getServiceType());
			if (service.getServiceType().equals(serviceType)) {
				return service;
			}
		}
		return null;
	}

	public List<UPnPService> getServiceList() {
		return serviceList;
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

	public String getPropertiesXml() {
		StringBuffer sb = new StringBuffer();
		Iterator<String> keys = properties.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			sb.append(properties.get(key).toXml());
		}
		return sb.toString();
	}

	public String getServiceListXml() {
		XmlTag serviceListTag = new XmlTag("serviceList");
		StringBuffer sb = new StringBuffer();
		for (UPnPService service : serviceList) {
			sb.append(service.toXml());
		}
		return serviceListTag.wrap(sb.toString());
	}

	public String getDeviceListXml() {
		if (childDevices.size() == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (UPnPDevice childDevice : childDevices) {
			sb.append(childDevice.toXml());
		}
		return XmlTag.wrap("deviceList", sb.toString());
	}

	public String toXml() {
		XmlTag root = new XmlTag("root");
		XmlTag specVersion = new XmlTag("specVersion");
		XmlTag major = new XmlTag("major");
		XmlTag minor = new XmlTag("minor");
		XmlTag device = new XmlTag("device");

		return XmlTag.docType(
			root.wrap(
				specVersion.wrap(major.wrap("1") +
								 minor.wrap("0"))
				+ device.wrap(getPropertiesXml() +
							  getServiceListXml() +
							  getDeviceListXml())));
	}
}
