package gov.pnnl.emsl.iRODS;

import static org.junit.Assert.*;

import org.junit.Test;

public class StatusHandlerTest {

	@Test
	public void test() {
		StatusHandler t = new StatusHandler();
		t.setTimeout(1492);
		assertEquals(t.getTimeout(), null);
	}

}
