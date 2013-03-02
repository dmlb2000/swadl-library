package gov.pnnl.emsl.my;

import gov.pnnl.emsl.my.MyEMSLConnect;
import gov.pnnl.emsl.my.MyEMSLConfig;

import java.lang.String;
import java.io.File;
import java.io.IOException;
import java.lang.System;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.security.GeneralSecurityException;
import java.net.URISyntaxException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import javax.xml.xpath.XPathExpressionException;
import java.lang.InterruptedException;
import org.javatuples.Triplet;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;

public class MyEMSLMain {
	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("g", true, "groups");
		CommandLineParser parser = new DefaultParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse( options, args );
		}
		catch( ParseException exp ) {
			// oops, something went wrong
			System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
		}
	}
}
