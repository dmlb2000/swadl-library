package gov.pnnl.emsl.SWADL;

import static org.junit.Assert.*;

import org.junit.Test;

public class FileTest {
	@Test
	public void test() throws Exception {
		File f;
		f = new File();
		String test = "DEADBEEF";
		f.setLocalName(test);
		assertEquals(f.getLocalName(), test);
		f.setName(test);
		assertEquals(f.getName(), test);
	}
}
