package com.nncloudtv.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Piping from one input stream to another without blocking
 * 
 * @author louis
 *
 */
public class PipingTask extends Thread {
    
    protected static Logger log = Logger.getLogger(PipingTask.class.getName());
    
    protected InputStream     in = null;
    protected OutputStream   out = null;
    protected boolean  keepGoing = true;
    protected final int  BUFSIZE = 147457;
    protected byte[]         buf = null;
    
    public PipingTask(InputStream in, OutputStream out) {
        
        super();
        this.in = in;
        this.out = out;
        this.keepGoing = true;
        this.buf = new byte[BUFSIZE];
    }
    
    public void stopCopying() {
        
        this.keepGoing = false;
    }
    
    public void run() {
        
        log.info("start piping ...");
        
        if (in == null) {
            log.warning("null input stream, abort.");
        }
        if (out == null) {
            log.warning("null output stream, abort.");
        }
        
        int len = 0, total = 0;
        try {
            do {
                len = in.read(buf);
                if (len < 0) {
                    
                    break;
                }
                total += len;
                log.fine(total + " piped");
                out.write(buf, 0, len);
                
                yield();
                if (in.available() == 0) {
                    log.fine("sleep a while");
                    sleep(100);
                }
            } while(keepGoing);
            
            
        } catch (IOException e) {
            log.info(e.getMessage());
        } catch (InterruptedException e) {
            log.info(e.getMessage());
        }
        
        log.info("total piped size = " + total);
        log.info("piping finished with keepGoing = " + keepGoing);
    }
}
