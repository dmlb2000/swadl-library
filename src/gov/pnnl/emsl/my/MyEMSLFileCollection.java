package gov.pnnl.emsl.my;

import org.kamranzafar.jtar.TarOutputStream;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarHeader;
import gov.pnnl.emsl.my.MyEMSLMetadata;
import gov.pnnl.emsl.my.MyEMSLFileMD;
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

public class MyEMSLFileCollection {
	MyEMSLMetadata md;

	public MyEMSLFileCollection(MyEMSLMetadata md) {
		this.md = md;
	}

	public void tarit(OutputStream out) throws IOException, FileNotFoundException, NoSuchAlgorithmException {
		TarOutputStream tarout = new TarOutputStream( new BufferedOutputStream( out ) );
		MessageDigest cript = MessageDigest.getInstance("SHA-1");
		for(MyEMSLFileMD f:md.md.file) {
			cript.reset();
			File fd = null;
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
