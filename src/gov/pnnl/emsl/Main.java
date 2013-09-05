package gov.pnnl.emsl;


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

/**
 * This is the main class to have a main method.
 * 
 * This should be command line driven main function and take arguments that
 * span all aspects of the library. However, the main function is to either
 * upload or download data.
 * 
 * @author dmlb2000
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
        conn = new Connect(new Configuration(config.getAbsolutePath()), username, password);
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
