package com.tjapp.upnp;

import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;


public class UPnPActionRequest {

	private String serviceType;
	private String actionName;
	private String controlUrl;
	private Map<String, String> parameters = new LinkedHashMap<>();
	private static Logger logger = Logger.getLogger("UPnPActionRequest");

	public UPnPActionRequest() {
	}

	public UPnPActionRequest (String controlUrl, String serviceType, String actionName) {
		this.controlUrl = controlUrl;
		this.serviceType = serviceType;
		this.actionName = actionName;
		if (actionName == null) {
			throw new IllegalArgumentException("no action found - " + actionName);
		}
	}

	public String getControlUrl() {
		return controlUrl;
	}

	public void setControlUrl(String controlUrl) {
		this.controlUrl = controlUrl;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceType() {
		return serviceType;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public void setParameter(String key, String value) {
		parameters.put(key, value);
	}

	public String getParameter(String key) {
		return parameters.get(key);
	}

	public void removeParameter(String key) {
		parameters.remove(key);
	}

	public static UPnPActionRequest fromXml(String xml) throws Exception {
		UPnPActionRequest request = new UPnPActionRequest();
		Document doc = XmlParser.parse(xml);
		Element root = doc.getDocumentElement();
		NodeList list = root.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			logger.debug(node.getNodeName());
			if (node.getNodeName().endsWith("Body")) {
				NodeList bodyList = node.getChildNodes();
				for (int j = 0; j < bodyList.getLength(); j++) {
					Node responseNode = bodyList.item(j);
					logger.debug(responseNode.getNodeName());
					if (responseNode.getNodeName().equals("#text") == false) {
						String actionName = responseNode.getNodeName();
						int idx = actionName.indexOf(":");
						if (idx > 0) {
							actionName = actionName.substring(idx + 1);
						}
						request.setActionName(actionName);
						logger.debug("ns: " + responseNode.getAttributes());
						NamedNodeMap attrs = responseNode.getAttributes();
						for (int x = 0; x < attrs.getLength(); x++) {
							Node attr = attrs.item(x);
							logger.debug(" attr: " + attr);
							logger.debug(" node name: " + attr.getNodeName());
							logger.debug(" localname: " + attr.getLocalName());
							logger.debug(" prefix: " + attr.getPrefix());
							logger.debug(" value: " + attr.getNodeValue());

							if (attr.getNodeName().startsWith("xmlns")) {
								request.setServiceType(attr.getNodeValue());
							}
							
							// attr: xmlns:u="urn:schemas-upnp-org:service:ContentDirectory:1"
							// node name: xmlns:u
							// localname: null
							// prefix: null
							// value: urn:schemas-upnp-org:service:ContentDirectory:1
						}
						NodeList paramNodeList = responseNode.getChildNodes();
						for (int h = 0; h < paramNodeList.getLength(); h++) {
							Node paramNode = paramNodeList.item(h);
							if (paramNode.getNodeName().equals("#text") == false) {
								String name = paramNode.getNodeName();
								String value = paramNode.getFirstChild().getNodeValue();
								logger.debug("name: " + name + ", value: " + value);
								request.setParameter(name, value);
							}
						}
					}
				}
			}
		}
		logger.debug("Service type: " + request.getServiceType());
		logger.debug("Action name: " + request.getActionName());
		Iterator<String> keys = request.getParameters().keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			String value = request.getParameter(key);
			logger.debug(" * " + key + ": '" + value + "'");
		}
		return request;
	}

	public String toSoap() {
		return UPnPSoap.toXml(this);
	}
}
