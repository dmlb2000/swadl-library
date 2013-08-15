package gov.pnnl.emsl.my;

import gov.pnnl.emsl.my.MyEMSLConnect;
import gov.pnnl.emsl.my.MyEMSLConfig;

import java.lang.String;
import java.io.File;
import java.io.Console;
import java.io.IOException;
import java.lang.System;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

public class MyEMSLMain {
    public static void main(String[] args) throws IOException, GeneralSecurityException, URISyntaxException, ParserConfigurationException, SAXException, NoSuchAlgorithmException, XPathExpressionException, InterruptedException {
        String username = "NOT_A_USER";
        String server = "my.emsl.pnl.gov";
        String qserver = "my.emsl.pnl.gov";
        String destination = "NOT_A_DIRECTORY";
        MyEMSLConnect conn;

        File config;
        File tarfile;

        Options options = new Options();
        options.addOption( OptionBuilder
            .withLongOpt( "file" )
            .withDescription( "filename to include" )
            .hasOptionalArgs()
            .withArgName("FILE")
            .create('f')
        );
        options.addOption( OptionBuilder
            .withLongOpt( "group" )
            .withDescription( "group name value pair" )
            .hasOptionalArgs()
            .withArgName("GROUP=NAME")
            .create('g')
        );
        options.addOption( OptionBuilder
            .withLongOpt( "destination" )
            .withDescription( "dir to put files into" )
            .hasArg()
            .withArgName("DIRECTORY")
            .create('d')
        );
        options.addOption( OptionBuilder
            .withLongOpt( "query-server" )
            .withDescription( "server to query from" )
            .hasArg()
            .withArgName("HOST")
            .create('q')
        );
        options.addOption( OptionBuilder
            .withLongOpt( "server" )
            .withDescription( "server to upload to or download from" )
            .hasArg()
            .withArgName("HOST")
            .create('s')
        );
        options.addOption( OptionBuilder
            .withLongOpt( "username" )
            .withDescription( "username" )
            .hasArg()
            .withArgName("USERNAME")
            .create('u')
        );
        options.addOption( OptionBuilder
            .withLongOpt( "timeout" )
            .withDescription( "timeout to wait for connections to fail." )
            .hasArg()
            .withArgName("SECONDS")
            .create('t')
        );
        options.addOption( OptionBuilder
            .withLongOpt( "help" )
            .withDescription( "print help message" )
            .create('h')
        );
        CommandLineParser parser = new GnuParser();
        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse( options, args );
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }
        if( line == null) { return; }
        if( line.hasOption("h") ) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "MyEMSLClientCmd", options );
            return;
        }
        if ( line.hasOption("u") ) {
            username = line.getOptionValue("u");
        }
        else {
            username = System.getProperty("user.name");
        }
        if ( line.hasOption("q") ) {
            qserver = line.getOptionValue("q");
        }
        if ( line.hasOption("s") ) {
            server = line.getOptionValue("s");
        }
        if ( line.hasOption("d") ) {
            destination = line.getOptionValue("d");
        }
        if ( ! line.hasOption("group") ) {
            System.err.println("no groups failing stupidly");
            return;
        }
        if ( line.hasOption("file") && line.hasOption("d") ) {
            System.err.println("don't support upload and download in one command");
            return;
        }
        if ( ! line.hasOption("file") && ! line.hasOption("d") ) {
            System.err.println("you should specify either -f or -d but not both");
            return;
        }

        System.out.println("Connecting to https://"+username+"@"+server);

        config = File.createTempFile("temp",".ini");
        config.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(config));
        writer.write("[client]\nproto=https\nquery_server="+qserver+"\nserver="+server+"\nservices=myemsl/services\n");
        writer.close();

        Console console = System.console();
        if (console == null) {
            System.err.println("can't prompt for passwd unable to obtain console");
            return;
        }
        String password = new String (console.readPassword ("Enter password: "));
        conn = new MyEMSLConnect(new MyEMSLConfig(config.getAbsolutePath()), username, password);
        if ( line.hasOption("f") ) {
            doUpload(conn, line);
        }
        else if ( line.hasOption("d") ) {
            doDownload(conn, line);
        }
    }

    private static void doUpload(MyEMSLConnect conn, CommandLine line) throws IOException, SAXException, ParserConfigurationException, SAXException, NoSuchAlgorithmException, XPathExpressionException, InterruptedException {
        File tarfile;
        MyEMSLFileCollection col;
        MyEMSLMetadata md;

        md = new MyEMSLMetadata();
        for(String s:line.getOptionValues("file")) {
            MyEMSLFileMD afmd = new MyEMSLFileMD(s, s, "hashforfilea");
            for(String g:line.getOptionValues("group")) {
                afmd.groups.add(new MyEMSLGroupMD(g.split("=")[1], g.split("=")[0]));
            }
            md.md.file.add(afmd);
        }

        Integer timeout = 15;
        if ( line.hasOption("timeout") ) {
            timeout = new Integer(line.getOptionValue("timeout"));
        }

        col = new MyEMSLFileCollection(md);
        conn.status_wait(conn.upload(col), timeout, 5);
        conn.logout();
    }

    private static void doDownload(MyEMSLConnect conn, CommandLine line) throws IOException, SAXException, XPathExpressionException {
        String destdir = line.getOptionValue("d");
        File ofile;
        File odir;
        ArrayList<MyEMSLGroupMD> qset = new ArrayList<MyEMSLGroupMD>();

        for(String g:line.getOptionValues("group")) {
            qset.add(new MyEMSLGroupMD(g.split("=")[1], g.split("=")[0]));
        }
        ArrayList<Triplet<Integer,String,String>> items = conn.query(qset);
        for(Triplet<Integer,String,String> i: items) {
            System.out.println(destdir+i.getValue1());
            ofile = new File(destdir+i.getValue1());
            odir = new File(ofile.getParent());
            odir.mkdirs();
            BufferedWriter bwout = new BufferedWriter(new FileWriter(ofile));
            conn.getitem(bwout, i);
            bwout.close();
        }
        conn.logout();
    }
}
