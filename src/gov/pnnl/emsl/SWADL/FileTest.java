package gov.pnnl.emsl.SWADL;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author dmlb2000
 *
 * Perform some tests to make sure strings go in and out of the class
 * properly.
 */
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
