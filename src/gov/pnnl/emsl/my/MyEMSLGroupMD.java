package gov.pnnl.emsl.my;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class MyEMSLGroupMD {
	public String name;
	public String type;

	public MyEMSLGroupMD(String name, String type) {
		this.name = name;
		this.type = type;
	}
}

