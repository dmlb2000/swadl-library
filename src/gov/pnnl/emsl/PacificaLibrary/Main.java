package gov.pnnl.emsl.PacificaLibrary;


import java.io.File;
import java.io.Console;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.net.URISyntaxException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import javax.xml.xpath.XPathExpressionException;
import org.javatuples.Triplet;
import java.util.ArrayList;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;

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
    public static void main(String[] args) throws IOException, GeneralSecurityException, URISyntaxException, ParserConfigurationException, SAXException, NoSuchAlgorithmException, XPathExpressionException, InterruptedException {
        String username;
        String server = "my.emsl.pnl.gov";
        String qserver = "my.emsl.pnl.gov";
        Connect conn;

        File config;

        Options options = new Options();
        final Option tmpopt;
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
        options.addOption(OptionBuilder.create('g'));
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

        Console console = System.console();
        if (console == null) {
            System.err.println("can't prompt for passwd unable to obtain console");
            return;
        }
        String password = new String (console.readPassword ("Enter password: "));
        conn = new Connect(new LibraryConfiguration(config.getAbsolutePath()), username, password);
        if ( line.hasOption("f") ) {
            doUpload(conn, line);
        }
        else if ( line.hasOption("d") ) {
            doDownload(conn, line);
        }
    }

    private static void doUpload(Connect conn, CommandLine line) throws IOException, SAXException, ParserConfigurationException, SAXException, NoSuchAlgorithmException, XPathExpressionException, InterruptedException {
        FileCollection col;
        Metadata md;

        md = new Metadata();
        for(String s:line.getOptionValues("file")) {
            FileMetaData afmd = new FileMetaData(s, s, "hashforfilea");
            for(String g:line.getOptionValues("group")) {
                afmd.groups.add(new GroupMetaData(g.split("=")[1], g.split("=")[0]));
            }
            md.md.file.add(afmd);
        }

        Integer timeout = 15;
        if ( line.hasOption("timeout") ) {
            timeout = new Integer(line.getOptionValue("timeout"));
        }

        col = new FileCollection(md);
        conn.status_wait(conn.upload(col), timeout, 5);
        conn.logout();
    }

    private static void doDownload(Connect conn, CommandLine line) throws IOException, SAXException, XPathExpressionException {
        String destdir = line.getOptionValue("d");
        File ofile;
        File odir;
        ArrayList<GroupMetaData> qset = new ArrayList<GroupMetaData>();

        for(String g:line.getOptionValues("group")) {
            qset.add(new GroupMetaData(g.split("=")[1], g.split("=")[0]));
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
