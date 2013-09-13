package gov.pnnl.emsl;

import org.kamranzafar.jtar.TarOutputStream;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarHeader;
import java.io.OutputStream;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

/**
 * This class should maintain an object describing the metadata for the files
 * and generate a tar file of that files including metadata.
 * 
 * @author dmlb2000
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
    public void tarit(OutputStream out) throws IOException, FileNotFoundException, NoSuchAlgorithmException {
        TarOutputStream tarout = new TarOutputStream( new BufferedOutputStream( out ) );
        MessageDigest cript = MessageDigest.getInstance("SHA-1");
        for(FileMetaData f:md.md.file) {
            cript.reset();
            File fd;
            if(!f.destinationDirectory.equals("")) {
                fd = new File(f.localFilePath);
                tarout.putNextEntry(new TarEntry(fd, f.destinationDirectory + "/" + f.fileName));
            } else {
                fd = new File(f.localFilePath);
                tarout.putNextEntry(new TarEntry(fd, f.fileName));
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
