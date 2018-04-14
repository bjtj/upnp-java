package com.tjapp.upnp;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class UPnPActionArgument {
	
	private String name;
	private UPnPActionArgumentDirection direction;
	private String relatedStateVariable;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRelatedStateVariable() {
		return relatedStateVariable;
	}

	public void setRelatedStateVariable(String relatedStateVariable) {
		this.relatedStateVariable = relatedStateVariable;
	}

	public UPnPActionArgumentDirection getDirection() {
		return direction;
	}

	public void setDirection(UPnPActionArgumentDirection direction) {
		this.direction = direction;
	}

	public static UPnPActionArgument fromNode(Node actionArgumentNode) {
		UPnPActionArgument argument = new UPnPActionArgument();
		NodeList list = actionArgumentNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			String name = node.getNodeName();
			if (name.equals("name")) {
				argument.setName(node.getFirstChild().getNodeValue());
			} else if (name.equals("direction")) {
				argument.setDirection(node.getFirstChild().getNodeValue().equals("in") ? UPnPActionArgumentDirection.IN : UPnPActionArgumentDirection.OUT);
			} else if (name.equals("relatedStateVariable")) {
				argument.setRelatedStateVariable(node.getFirstChild().getNodeValue());
			}
		}
		return argument;
	}

	public String toXml() {
		XmlTag argument = new XmlTag("argument");
		return argument.wrap(XmlTag.wrap("name", name) +
							 XmlTag.wrap("direction", direction.toString()) +
							 XmlTag.wrap("relatedStateVariable", relatedStateVariable));
	}
}
