package gov.pnnl.emsl.iRODS;

import static org.junit.Assert.*;

import org.junit.Test;

public class FileTest {

	/**
	 * @throws Exception
	 * 
	 * make sure we get and set the attributes in the class
	 * to make sure they are the same.
	 */
	@Test
	public void test() throws Exception {
		String test = "DEADBEEF";
		File f = new File();
		f.setCollName(test);
		f.setDataName(test);
		f.setLocalName(test);
		f.setName(test);
		assertEquals(f.getCollName(), test);
		assertEquals(f.getDataName(), test);
		assertEquals(f.getLocalName(), test);
		assertEquals(f.getName(), test);
		
	}

}
