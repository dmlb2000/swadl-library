package gov.pnnl.emsl.my;

import gov.pnnl.emsl.my.MyEMSLConnect;
import gov.pnnl.emsl.my.MyEMSLConfig;

import java.lang.String;
import java.io.File;
import java.io.IOException;
import java.lang.System;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class MyEMSLConnectTest extends junit.framework.TestCase {
	MyEMSLConnect test;
	File temp;

	public MyEMSLConfigTest() throws IOException {
		temp = File.createTempFile("temp",".ini");
		temp.deleteOnExit();
		BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
		writer.write("[client]\nproto=https\nserver=a4.my.emsl.pnl.gov\nservices=myemsl/services\n");
		writer.close();
		test = new MyEMSLConnect(new MyEMSLConfig(temp.getAbsolutePath()));
	}
	
	public void testfinalize() throws IOException {
		if(test != null) {
			test.finalize();
		}
	}
}

