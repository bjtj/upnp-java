package com.tjapp.upnp;

import static org.junit.Assert.assertEquals;
import org.junit.*;

public class TestUuid {

	@Test
	public void test_uuid() {
		System.out.printf("Generate: '%s'\n", Uuid.generate(0, null));
		System.out.printf("Random: '%s'\n", Uuid.random());
	}
}
