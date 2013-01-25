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
	OutputStream out;
	MyEMSLMetadata md;

	public MyEMSLFileCollection(OutputStream out, MyEMSLMetadata md) {
		this.out = out;
		this.md = md;
	}

	public void tarit() throws IOException, FileNotFoundException, NoSuchAlgorithmException {
		TarOutputStream out = new TarOutputStream( new BufferedOutputStream( this.out ) );
		MessageDigest cript = MessageDigest.getInstance("SHA-1");
		for(MyEMSLFileMD f:md.md.file) {
			cript.reset();
			File fd = new File(f.destinationDirectory + f.filename);
			out.putNextEntry(new TarEntry(fd, f.destinationDirectory + f.filename));
			BufferedInputStream origin = new BufferedInputStream(new FileInputStream( new File(f.localFilePath) ));
			int count;
			byte data[] = new byte[2048];
			while((count = origin.read(data)) != -1) {
				cript.update(data);
				out.write(data, 0, count);
			}
			f.sha1Hash = new String(Hex.encodeHex(cript.digest()));
			out.flush();
			origin.close();
		}
		byte jsonbytes[] = this.md.tojson().getBytes("UTF-8");
		out.putNextEntry(new TarEntry(TarHeader.createHeader("metadata.txt", jsonbytes.length, 0, false)));
		out.write(jsonbytes, 0, jsonbytes.length);
		out.flush();
		out.close();
	}
}
