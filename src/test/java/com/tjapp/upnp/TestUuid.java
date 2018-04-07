package com.tjapp.upnp;

import static org.junit.Assert.assertEquals;
import org.junit.*;

public class TestUuid {

	private static Logger logger = Logger.getLogger("TestUuid");

	@Test
	public void test_uuid() {
		logger.debug(String.format("Generate: '%s'", Uuid.generate(0, null)));
		logger.debug(String.format("Random: '%s'", Uuid.random()));
	}
}
