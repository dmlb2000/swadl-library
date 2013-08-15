package gov.pnnl.emsl.my;


import java.lang.String;
import java.io.File;
import java.io.IOException;
import java.lang.System;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.GeneralSecurityException;
import java.net.URISyntaxException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import javax.xml.xpath.XPathExpressionException;
import java.lang.InterruptedException;
import org.javatuples.Triplet;
import java.util.ArrayList;
import org.junit.Ignore;
import org.junit.Test;

public class MyEMSLConnectTest extends junit.framework.TestCase {

    public MyEMSLConnectTest() { }

        @Ignore
        @Test
    public void testconnect() throws IOException, GeneralSecurityException, URISyntaxException,  ParserConfigurationException {
        MyEMSLConnect test;
        File temp;
        temp = File.createTempFile("temp",".ini");
        temp.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
        writer.write("[client]\nproto=https\nserver=192.168.122.128\nservices=myemsl/services\n");
        writer.close();
        test = new MyEMSLConnect(new MyEMSLConfig(temp.getAbsolutePath()), "dmlb2000", "dmlb336");
        assert test.get_myemsl_session() != null;
        test.logout();
    }

        @Ignore
        @Test
    public void testupload() throws IOException, GeneralSecurityException, URISyntaxException, ParserConfigurationException, SAXException, XPathExpressionException, InterruptedException {
        MyEMSLFileCollection col;
        MyEMSLMetadata md;
        MyEMSLConnect test;
        File temp;

                temp = File.createTempFile("temp",".ini");
                temp.deleteOnExit();
                BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
                writer.write("[client]\nproto=https\nserver=192.168.122.128\nservices=myemsl/services\n");
                writer.close();
                test = new MyEMSLConnect(new MyEMSLConfig(temp.getAbsolutePath()), "dmlb2000", "dmlb336");

        MyEMSLFileMD afmd = new MyEMSLFileMD("test"+File.separator+"a", "test"+File.separator+"a", "hashforfilea");
        MyEMSLFileMD bfmd = new MyEMSLFileMD("test"+File.separator+"b", "test"+File.separator+"b", "hashforfilea");

        afmd.groups.add(new MyEMSLGroupMD("45796", "proposal"));
        afmd.groups.add(new MyEMSLGroupMD("abc_1234", "JGI.ID"));
        bfmd.groups.add(new MyEMSLGroupMD("45796", "proposal"));
        bfmd.groups.add(new MyEMSLGroupMD("abc_1235", "JGI.ID"));

        md = new MyEMSLMetadata();
        md.md.file.add(afmd);
        md.md.file.add(bfmd);

        col = new MyEMSLFileCollection(md);
        test.status_wait(test.upload(col), 15, 5);
        test.logout();
    }

        @Ignore
        @Test
    public void testquery() throws IOException, GeneralSecurityException, URISyntaxException, ParserConfigurationException, SAXException, XPathExpressionException {
        MyEMSLConnect test;
        File temp, output;

                temp = File.createTempFile("temp",".ini");
                temp.deleteOnExit();
                BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
                writer.write("[client]\nproto=https\nquery_server=192.168.122.128\nserver=192.168.122.128\nservices=myemsl/services\n");
                writer.close();
                test = new MyEMSLConnect(new MyEMSLConfig(temp.getAbsolutePath()), "dmlb2000", "dmlb336");

        ArrayList<MyEMSLGroupMD> qset = new ArrayList<MyEMSLGroupMD>();
        qset.add(new MyEMSLGroupMD("45796", "proposal"));
        qset.add(new MyEMSLGroupMD("abc_1234", "JGI.ID"));

        /*
         * should be an array of (itemid, path, authtoken).
         */
        ArrayList<Triplet<Integer,String,String>> items = test.query(qset);
        for(Triplet<Integer,String,String> i: items) {
            System.out.println(i.getValue1());
            output = File.createTempFile("output", ".txt");
            //output.deleteOnExit();
            BufferedWriter bwout = new BufferedWriter(new FileWriter(output));
            test.getitem(bwout, i);
            bwout.close();
        }
    }
}

