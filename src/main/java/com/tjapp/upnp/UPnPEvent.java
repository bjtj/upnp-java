package com.tjapp.upnp;

import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;


public class UPnPEvent {

	private String sid;
	private Map<String, UPnPProperty> properties = new LinkedHashMap<>();


	public String getSid() {
		return sid;
	}
	
	public void setSid(String sid) {
		this.sid = sid;
	}

	public void setProperty(String name, String value) {
		properties.put(name, new UPnPProperty(name, value));
	}
	
	public String getProperty(String name) {
		return properties.get(name).getValue();
	}

	public List<UPnPProperty> getPropertyList() {
		List<UPnPProperty> list = new ArrayList<>();
		Iterator<String> keys = properties.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			list.add(properties.get(key));
		}
		return list;
	}

	public String toString() {
		XmlTag propertySetTag = new XmlTag("propertySet");
		propertySetTag.setNamespace("e");
		propertySetTag.setAttribute("xmlns:e", "urn:schemas-upnp-org:event-1-0");

		StringBuffer sb = new StringBuffer();
		Iterator<String> keys = properties.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			UPnPProperty property = properties.get(key);
			XmlTag propertyTag = new XmlTag("e", "property");
			sb.append(propertyTag.wrap(property.toXml()));
		}
		return XmlTag.docType(propertySetTag.wrap(sb.toString()));
	}

	public static UPnPEvent fromXml(String xml) throws Exception {
		UPnPEvent event = new UPnPEvent();
		Document dom = XmlParser.parse(xml);
		Element root = dom.getDocumentElement();
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().endsWith("propertyset")) {
				NodeList propsNodeList = node.getChildNodes();
				for (int j = 0; j < propsNodeList.getLength(); j++) {
					Node propNode = propsNodeList.item(j);
					if (propNode.getNodeName().endsWith("property")) {
						NodeList pnodeList = propNode.getChildNodes();
						for (int h = 0; h < pnodeList.getLength(); h++) {
							Node pnode = pnodeList.item(h);
							if (pnode.getNodeName().equals("#text") == false) {
								String name = pnode.getNodeName();
								String value = pnode.getFirstChild().getNodeValue();
								event.setProperty(name, value);
							}
						}
					}
				}
			}
		}
		return event;
	}
	
	public static void main(String args[]) {
	}
}
