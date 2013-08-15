package gov.pnnl.emsl.my;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;


public class MyEMSLMetadata {

    Gson gson;
    public MyEMSLMD md;

    public MyEMSLMetadata() { this.gson = new Gson(); this.md = new MyEMSLMD(); }

    public String tojson() { return this.gson.toJson(this.md); }
}
