package gov.pnnl.emsl.PacificaLibrary;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarHeader;
import org.kamranzafar.jtar.TarOutputStream;

/**
 * This class should maintain an object describing the metadata for the files
 * and generate a tar file of that files including metadata.
 * 
 * @author David ML Brown Jr. <dmlb2000@gmail.com>
 */
public class FileCollection {
    Metadata md;

    /**
     * Constructor sets the internal metadata from arguments passed.
     * 
     * @param md metadata object
     */
    public FileCollection(Metadata md) {
        this.md = md;
    }

    /**
     * Create a tar file from the metadata and stream it to an output object.
     * 
     * This is a bit more involved since we need to create sha1sums of the files
     * as we stream the data to the output stream. Then finally send the json
     * of the metadata to the tar and close it.
     * 
     * @param out
     * @throws IOException
     * @throws FileNotFoundException
     * @throws NoSuchAlgorithmException
     */
    public void tarit(OutputStream out) throws Exception {
        TarOutputStream tarout = new TarOutputStream( new BufferedOutputStream( out ) );
        MessageDigest cript = MessageDigest.getInstance("SHA-1");
        for(gov.pnnl.emsl.SWADL.File sf:md.md.file) {
        	FileMetaData f = (FileMetaData) sf;
            cript.reset();
            File fd;
            if(!f.destinationDirectory.equals("")) {
                fd = new File(f.getLocalName());
                tarout.putNextEntry(new TarEntry(fd, f.destinationDirectory + "/" + f.getName()));
            } else {
                fd = new File(f.getLocalName());
                tarout.putNextEntry(new TarEntry(fd, f.getName()));
            }
            BufferedInputStream origin = new BufferedInputStream(new FileInputStream(fd));
            int count;
            byte data[] = new byte[2048];
            while((count = origin.read(data)) != -1) {
                cript.update(data);
                tarout.write(data, 0, count);
            }
            f.sha1Hash = new String(Hex.encodeHex(cript.digest()));
            tarout.flush();
            origin.close();
        }
        byte jsonbytes[] = this.md.tojson().getBytes("UTF-8");
        tarout.putNextEntry(new TarEntry(TarHeader.createHeader("metadata.txt", jsonbytes.length, System.currentTimeMillis()/1000-60, false)));
        tarout.write(jsonbytes, 0, jsonbytes.length);
        tarout.flush();
        tarout.close();
    }
}
