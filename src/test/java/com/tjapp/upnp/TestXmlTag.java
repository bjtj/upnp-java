package com.tjapp.upnp;

import static org.junit.Assert.assertEquals;
import org.junit.*;

public class TestXmlTag {

	@Test
	public void test_html_tag() {
		XmlTag tag = new XmlTag("body");
		assertEquals(tag.wrap("hello"), "<body>hello</body>");
		tag.setNamespace("u");
		assertEquals(tag.wrap("hello"), "<u:body>hello</u:body>");
		tag.setAttribute("x", "abc");
		assertEquals(tag.wrap("hello"), "<u:body x=\"abc\">hello</u:body>");
		tag.setAttribute("y", "dce");
		assertEquals(tag.wrap("hello"), "<u:body x=\"abc\" y=\"dce\">hello</u:body>");

		XmlTag rootTag = new XmlTag("root");
		assertEquals(rootTag.wrap(tag.wrap("hello")), "<root><u:body x=\"abc\" y=\"dce\">hello</u:body></root>");
	}
}
