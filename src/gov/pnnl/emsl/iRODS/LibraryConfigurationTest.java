package gov.pnnl.emsl.iRODS;

import static org.junit.Assert.*;

import org.junit.Test;

public class LibraryConfigurationTest {

	@Test
	public void test() throws Exception {
		String test = "DEADBEEF";
		LibraryConfiguration c = new LibraryConfiguration();
		c.setHost(test);
		assertEquals(c.getHost(), test);
		c.setPort(1492);
		assertEquals(c.getPort(), new Integer(1492));
		c.setPrefix(test);
		assertEquals(c.getPrefix(), test);
		c.setResource(test);
		assertEquals(c.getResource(), test);
		c.setZone(test);
		assertEquals(c.getZone(), test);
	}
}
