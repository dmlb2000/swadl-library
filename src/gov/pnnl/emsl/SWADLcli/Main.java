package gov.pnnl.emsl.SWADLcli;

import gov.pnnl.emsl.PacificaLibrary.Connect;
import gov.pnnl.emsl.PacificaLibrary.LibraryConfiguration;
import gov.pnnl.emsl.SWADL.Group;
import gov.pnnl.emsl.SWADL.SWADL;
import gov.pnnl.emsl.SWADL.UploadHandle;

import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

/**
 * This is the main class to have a main method.
 * 
 * This should be command line driven main function and take arguments that
 * span all aspects of the library. However, the main function is to either
 * upload or download data.
 * 
 * @author David ML Brown Jr. <dmlb2000@gmail.com>
 */
public class Main {
    
    int debug_level;
    
    /**
     * Main method does mostly option parsing and setting up some initial
     * variables to pass onto the upload or download methods.
     * 
     * @param args Array of strings containing the command line arguments.
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws URISyntaxException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws NoSuchAlgorithmException
     * @throws XPathExpressionException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws Exception {
        String username;
        String server = "my.emsl.pnl.gov";
        String qserver = "my.emsl.pnl.gov";
        String backend = "myemsl";
        String zone = "myemsl";
        SWADL conn;

        File config;

        Options options = new Options();
        OptionBuilder.withLongOpt( "backend" );
        OptionBuilder.withDescription( "backend to talk to" );
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("BACKEND");
        options.addOption(OptionBuilder.create('b'));
        OptionBuilder.withLongOpt( "zone" );
        OptionBuilder.withDescription( "irods zone" );
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("ZONE");
        options.addOption(OptionBuilder.create('z'));
        OptionBuilder.withLongOpt( "file" );
        OptionBuilder.withDescription( "filename to include" );
        OptionBuilder.hasOptionalArgs();
        OptionBuilder.withArgName("FILE");
        options.addOption(OptionBuilder.create('f'));
        OptionBuilder.withLongOpt( "group" );
        OptionBuilder.withDescription( "group name value pair" );
        OptionBuilder.hasOptionalArgs();
        OptionBuilder.withArgName("GROUP=NAME");
        options.addOption(OptionBuilder.create('g'));
        OptionBuilder.withLongOpt( "destination" );
        OptionBuilder.withDescription( "dir to put files into" );
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("DIRECTORY");
        options.addOption(OptionBuilder.create('d'));
        OptionBuilder.withLongOpt( "query-server" );
        OptionBuilder.withDescription( "server to query from" );
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("HOST");
        options.addOption(OptionBuilder.create('q'));
        OptionBuilder.withLongOpt( "server" );
        OptionBuilder.withDescription( "server to upload to or download from" );
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("HOST");
        options.addOption(OptionBuilder.create('s'));
        OptionBuilder.withLongOpt( "username" );
        OptionBuilder.withDescription( "username" );
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("USERNAME");
        options.addOption(OptionBuilder.create('u'));
        OptionBuilder.withLongOpt( "timeout" );
        OptionBuilder.withDescription( "timeout to wait for connections to fail." );
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("SECONDS");
        options.addOption(OptionBuilder.create('t'));
        OptionBuilder.withLongOpt( "help" );
        OptionBuilder.withDescription( "print help message" );
        options.addOption(OptionBuilder.create('h'));
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
        if( line.hasOption("b")) {
        	backend = line.getOptionValue("b");
        }
        else {
        	backend = "myemsl";
        }
        if (line.hasOption('z')) {
        	zone = line.getOptionValue('z');
        }
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

	/*
        Console console = System.console();
        if (console == null) {
            System.err.println("can't prompt for passwd unable to obtain console");
            return;
        }
        String password = new String (console.readPassword ("Enter password: "));
	*/
        /* eclipse is lame */
        String password = "r00tm3";
        if( backend.equals("myemsl")) {
        	conn = new Connect(new LibraryConfiguration(config.getAbsolutePath()), username, password);
        }
        else if (backend.equals("irods")){
        	gov.pnnl.emsl.iRODS.LibraryConfiguration c = new gov.pnnl.emsl.iRODS.LibraryConfiguration();
        	c.setHost(server);
        	c.setZone(zone);
        	c.setPort(1247);
        	conn = new gov.pnnl.emsl.iRODS.Connect(c);
        	conn.login(username, password);
        }
        else {
        	System.err.println("Need to specify backend to use [myemsl|irods].");
        	return;
        }
        if ( line.hasOption("f") ) {
            doUpload(conn, line);
        }
        else if ( line.hasOption("d") ) {
            doDownload(conn, line);
        }
    }

    private static void doUpload(SWADL conn, CommandLine line) throws Exception {
    	List<gov.pnnl.emsl.SWADL.File> files = new ArrayList<gov.pnnl.emsl.SWADL.File>();
    	
        for(String s:line.getOptionValues("file")) {
            gov.pnnl.emsl.SWADL.File afmd = new gov.pnnl.emsl.SWADL.File();
            afmd.setLocalName(s);
            afmd.setName(s);
            List<Group> groups = new ArrayList<Group>();
            for(String g:line.getOptionValues("group")) {
                groups.add(new Group(g.split("=")[0], g.split("=")[1]));
            }
            afmd.setGroups(groups);
            files.add(afmd);
        }

        Integer timeout = 15;
        if ( line.hasOption("timeout") ) {
            timeout = new Integer(line.getOptionValue("timeout"));
        }

        UploadHandle h = conn.uploadAsync(files);
        h.setTimeout(timeout);
        conn.uploadWait(h);
        conn.logout();
    }

    private static void doDownload(SWADL conn, CommandLine line) throws Exception {
        String destdir = line.getOptionValue("d");
        File ofile;
        File odir;
        List<Group> qset = new ArrayList<Group>();

        for(String g:line.getOptionValues("group")) {
            qset.add(new Group(g.split("=")[0], g.split("=")[1]));
        }
        List<gov.pnnl.emsl.SWADL.File> items = conn.query(qset);
        for(gov.pnnl.emsl.SWADL.File i: items) {
            System.out.println(destdir+i.getName());
            ofile = new File(destdir+i.getName());
            odir = new File(ofile.getParent());
            odir.mkdirs();
            BufferedWriter bwout = new BufferedWriter(new FileWriter(ofile));
            conn.getFile(bwout, i);
            bwout.close();
        }
        conn.logout();
    }

    /**
     * Default constructor with initializations.
     */
    public Main() {
        this.debug_level = 0;
    }
}
