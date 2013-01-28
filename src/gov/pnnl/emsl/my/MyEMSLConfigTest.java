package gov.pnnl.emsl.my;

import gov.pnnl.emsl.my.MyEMSLConfig;

import java.lang.String;
import java.io.File;
import java.io.IOException;
import java.lang.System;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class MyEMSLConfigTest extends junit.framework.TestCase {
	MyEMSLConfig test;
	File temp;

	public MyEMSLConfigTest() throws IOException {
		temp = File.createTempFile("temp",".ini");
		temp.deleteOnExit();
		BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
		writer.write("[client]\nproto=https\nserver=a4.my.emsl.pnl.gov\nservices=myemsl/services\n");
		writer.close();
		test = new MyEMSLConfig(temp.getAbsolutePath());
	}

	public void testserver() throws IOException {
		assert test.server() == "a4.my.emsl.pnl.gov";
	}

	public void testbaseurl() throws IOException {
		assert test.baseurl() == "https://a4.my.emsl.pnl.gov";
	}

	public void testservices() throws IOException {
		assert test.services() == "https://a4.my.emsl.pnl.gov/myemsl/services";
	}

	public void testlogout() throws IOException {
		assert test.logouturl() == "https://a4.my.emsl.pnl.gov/myemsl/logout";
	}
	public void testlogin() throws IOException {
		assert test.loginurl() == "https://a4.my.emsl.pnl.gov/myemsl/auth";
	}
	public void testprealloc() throws IOException {
		assert test.preallocurl() == "https://a4.my.emsl.pnl.gov/myemsl/cgi-bin/preallocate";
	}
	public void testfinish() throws IOException {
		assert test.finishurl() == "https://a4.my.emsl.pnl.gov/myemsl/cgi-bin/finish";
	}

}
