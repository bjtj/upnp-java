package com.tjapp.upnp;

import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

class UPnPActionResponse {

	private static Logger logger = Logger.getLogger("UPnPActionResponse");
	private String serviceType;
	private String actionName;
	private Map<String, String> parameters = new LinkedHashMap<>();

	static {
		logger.setWriter(Logger.NULL_WRITER);
	}
	

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getActionName() {
		return actionName;
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

	public static UPnPActionResponse fromXml(String xml) throws Exception {
		UPnPActionResponse response = new UPnPActionResponse();
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
					if (responseNode.getNodeName().endsWith("Response")) {
						String actionName = responseNode.getNodeName();
						int idx = actionName.indexOf(":");
						if (idx > 0) {
							actionName = actionName.substring(idx + 1);
						}
						actionName = actionName.substring(0, actionName.length() - "Response".length());
						response.setActionName(actionName);
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
								response.setServiceType(attr.getNodeValue());
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
								response.setParameter(name, value);
							}
						}
					}
				}
			}
		}
		logger.debug("Service type: " + response.getServiceType());
		logger.debug("Action name: " + response.getActionName());
		Iterator<String> keys = response.getParameters().keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			String value = response.getParameter(key);
			logger.debug(" * " + key + ": '" + value + "'");
		}
		return response;
	}

	public String toSoap() {
		return UPnPSoap.toXml(this);
	}
}
