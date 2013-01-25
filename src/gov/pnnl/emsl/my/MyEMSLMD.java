package gov.pnnl.emsl.my;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class MyEMSLMD {
	public List<MyEMSLFileMD> file;

	public MyEMSLMD() {
		file = new ArrayList<MyEMSLFileMD>();
	}
}

