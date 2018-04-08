package com.tjapp.upnp;

import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;


class UPnPAction {
	
	private String name;
	private Map<String, UPnPActionArgument> arguments = new LinkedHashMap<>();

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setArgument(UPnPActionArgument argument) {
		arguments.put(argument.getName(), argument);
	}

	public UPnPActionArgument getArgument(String name) {
		return arguments.get(name);
	}

	public static UPnPAction fromNodeList(NodeList list) {
		UPnPAction action = new UPnPAction();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			String name = node.getNodeName();
			if (name.equals("name")) {
				action.setName(name);
			} else if (name.equals("argumentList")) {
				NodeList argumentNodeList = node.getChildNodes();
				for (int j = 0; j < argumentNodeList.getLength(); j++) {
					Node argumentNode = argumentNodeList.item(j);
					if (argumentNode.getNodeName().equals("argument")) {
						UPnPActionArgument argument = UPnPActionArgument.fromNode(argumentNode);
						action.setArgument(argument);
					}
				}
			}
		}
		return action;
	}
}
