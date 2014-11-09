package gov.pnnl.emsl.SWADL;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author dmlb2000
 *
 * Test the group class to make sure string go in and out
 * properly.
 */
public class GroupTest {

	@Test
	public void test() {
		String key = "FOO";
		String value = "BAR";
		Group g = new Group(key, value);
		assertEquals(g.getKey(), key);
		assertEquals(g.getValue(), value);
	}

}
