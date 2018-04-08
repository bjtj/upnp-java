package com.tjapp.upnp;

import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;


class UPnPStateVariable {
	
	private boolean sendEvents;
	private boolean multicast;
	private String name;
	private String dataType;
	private String defaultValue;
	private List<String> allowedValueList;
	private static Logger logger = Logger.getLogger("UPnPStateVariable");

	public boolean getSendEvents() {
		return sendEvents;
	}

	public void setSendEvents(boolean sendEvents) {
		this.sendEvents = sendEvents;
	}

	public boolean getMulticast() {
		return multicast;
	}

	public void setMulticast(boolean multicast) {
		this.multicast = multicast;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	};

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public List<String> getAllowedValueList() {
		return allowedValueList;
	}

	public void setAllowedValueList(List<String> allowedValueList) {
		this.allowedValueList = allowedValueList;
	}

	public static UPnPStateVariable fromNode(Node stateVariableNode) {
		UPnPStateVariable stateVariable = new UPnPStateVariable();
		NodeList list = stateVariableNode.getChildNodes();
		stateVariable.setSendEvents(((Element)stateVariableNode).getAttribute("sendEvents").equals("yes"));
		stateVariable.setMulticast(((Element)stateVariableNode).getAttribute("multicast").equals("yes"));
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			String name = node.getNodeName();
			if (name.equals("name")) {
				stateVariable.setName(node.getFirstChild().getNodeValue());
			} else if (name.equals("dataType")) {
				stateVariable.setDataType(node.getFirstChild().getNodeValue());
			} else if (name.equals("defaultValue")) {
				stateVariable.setDefaultValue(node.getFirstChild().getNodeValue());
			} else if (name.equals("allowedValueList")) {
				NodeList allowedValueNodeList = node.getChildNodes();
				List<String> allowedValueList = new ArrayList<>();
				for (int j = 0; j < allowedValueNodeList.getLength(); j++) {
					Node allowedValueNode = allowedValueNodeList.item(j);
					if (allowedValueNode.getNodeName().equals("allowedValue")) {
						allowedValueList.add(allowedValueNode.getFirstChild().getNodeValue());
					}
				}
				stateVariable.setAllowedValueList(allowedValueList);
			}
		}
		return stateVariable;
	}
}
