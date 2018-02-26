package com.tjapp.upnp;

import javax.xml.parsers.*;

import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

class XmlParser {

	Document parse(String textXml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(textXml)));
		document.getDocumentElement().normalize();
		return document;
	}

	public static void main(String args[]) throws Exception {
		String textXml = "<root><person><name>TJ</name><age>35</age></person></root>";
		Document doc = new XmlParser().parse(textXml);
		System.out.println("Name: " + doc.getElementsByTagName("name").item(0).getChildNodes().item(0));
		System.out.println("Age: " + doc.getElementsByTagName("age").item(0).getChildNodes().item(0));
	}
}
