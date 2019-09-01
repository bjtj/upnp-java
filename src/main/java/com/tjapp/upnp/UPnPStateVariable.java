package com.tjapp.upnp;

import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;


public class UPnPStateVariable {
	
	private boolean sendEvents;
	private boolean multicast;
	private String name;
	private String dataType;
	private String defaultValue;
	private List<String> allowedValueList;
	private String allowedValueRangeMax;
	private String allowedValueRangeMin;
	private String allowedValueRangeStep;
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
        
        public boolean hasAllowedValueList() {
            return allowedValueList != null && allowedValueList.size() > 0;
        }

	public List<String> getAllowedValueList() {
		return allowedValueList;
	}

	public void setAllowedValueList(List<String> allowedValueList) {
		this.allowedValueList = allowedValueList;
	}

	public void setAllowedValueRangeMax(String max) {
		allowedValueRangeMax = max;
	}

	public String getAllowedValueRangeMax() {
		return allowedValueRangeMax;
	}
	
	public void setAllowedValueRangeMin(String min) {
		allowedValueRangeMin = min;
	}

	public String getAllowedValueRangeMin() {
		return allowedValueRangeMin;
	}

	public void setAllowedValueRangeStep(String step) {
		allowedValueRangeStep = step;
	}

	public String getAllowedValueRangeStep() {
		return allowedValueRangeStep;
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
			} else if (name.equals("allowedValueRange")) {
				NodeList allowedValueRangeNodeList = node.getChildNodes();
				for (int j = 0; j < allowedValueRangeNodeList.getLength(); j++) {
					Node rangeNode = allowedValueRangeNodeList.item(j);
					if (rangeNode.getNodeName().equals("minimum")) {
						stateVariable.setAllowedValueRangeMin(
							rangeNode.getFirstChild().getNodeValue());
					} else if (rangeNode.getNodeName().equals("maximum")) {
						stateVariable.setAllowedValueRangeMax(
							rangeNode.getFirstChild().getNodeValue());
					} else if (rangeNode.getNodeName().equals("step")) {
						stateVariable.setAllowedValueRangeStep(
							rangeNode.getFirstChild().getNodeValue());
					}
				}
			}
		}
		return stateVariable;
	}

	public String getDefaultValueXml() {
		if (StringUtil.isEmpty(defaultValue)) {
			return "";
		}
		return XmlTag.wrap("defaultValue", defaultValue);
	}

	public String getAllowedValueListXml() {
		if (allowedValueList == null || allowedValueList.size() == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (String allowedValue : allowedValueList) {
			sb.append(XmlTag.wrap("allowedValue", allowedValue));
		}
		return XmlTag.wrap("allowedValueList", sb.toString());
	}

	public String getAllowedValueRangeXml() {
		if (StringUtil.isEmpty(allowedValueRangeMax) ||
			StringUtil.isEmpty(allowedValueRangeMin)) {
			return "";
		}

		return XmlTag.wrap("allowedValueRange",
						   XmlTag.wrap("minimum", allowedValueRangeMin) +
						   XmlTag.wrap("maximum", allowedValueRangeMax) +
						   (StringUtil.isEmpty(allowedValueRangeStep) ? "" :
							XmlTag.wrap("step", allowedValueRangeStep)));
	}

	public String toXml() {
		XmlTag stateVariable = new XmlTag("stateVariable");
		stateVariable.setAttribute("sendEvents", StringUtil.yesNo(sendEvents));
		stateVariable.setAttribute("multicast", StringUtil.yesNo(multicast));
		return stateVariable.wrap(XmlTag.wrap("name", name) +
								  XmlTag.wrap("dataType", dataType) +
								  getDefaultValueXml() +
								  getAllowedValueListXml() +
								  getAllowedValueRangeXml());
	}
}
