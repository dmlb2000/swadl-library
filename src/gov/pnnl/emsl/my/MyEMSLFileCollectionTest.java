package gov.pnnl.emsl.my;

import gov.pnnl.emsl.my.MyEMSLFileCollection;
import gov.pnnl.emsl.my.MyEMSLMetadata;
import gov.pnnl.emsl.my.MyEMSLFileMD;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;

public class MyEMSLFileCollectionTest extends junit.framework.TestCase {
	public void testcol() throws IOException, FileNotFoundException, NoSuchAlgorithmException {
		MyEMSLFileCollection col;
		MyEMSLMetadata md;
		MyEMSLFileMD afmd = new MyEMSLFileMD("test/a", "test/a", "hashforfilea");
		MyEMSLFileMD bfmd = new MyEMSLFileMD("test/b", "test/b", "hashforfilea");

		afmd.groups.add(new MyEMSLGroupMD("45765", "proposal"));
		afmd.groups.add(new MyEMSLGroupMD("abc_1234", "JGI.ID"));
		bfmd.groups.add(new MyEMSLGroupMD("45765", "proposal"));
		bfmd.groups.add(new MyEMSLGroupMD("abc_1235", "JGI.ID"));

		md = new MyEMSLMetadata();
		md.md.file.add(afmd);
		md.md.file.add(bfmd);

		FileOutputStream dest = new FileOutputStream( "/tmp/test.tar" );
		col = new MyEMSLFileCollection(dest, md);
		col.tarit();
	}
}
