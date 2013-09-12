package gov.pnnl.emsl;

import java.io.IOException;
import java.io.OutputStream;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * NullOutputStream is an OutputStream counter.
 * 
 * Just use this to write what you want and get the length of what
 * was written without creating copies.
 * 
 * @author dmlb2000
 */
public class NullOutputStream extends OutputStream {
    private long length;
    
    /**
     * Default constructor just set the length counter to 0
     */
    public NullOutputStream() {
        this.length = 0;
    }
    
    @Override
    public void write(int b) throws IOException { length += 1; }
    
    @Override
    public void write(byte[] b, int off, int len) {
        length += len;
    }
    
    @Override
    public void write(byte[] b) {
        length += b.length;
    }
    
    /**
     * Return the length of what was written.
     * 
     * @return Integer length
     */
    public long getLength() {
        return this.length;
    }
}
