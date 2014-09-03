package com.nncloudtv.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

public class PipingTask extends Thread {
    
    protected static Logger log = Logger.getLogger(PipingTask.class.getName());
    
    protected InputStream     in = null;
    protected OutputStream   out = null;
    protected boolean  keepGoing = true;
    protected final int  BUFSIZE = 76147;
    
    public PipingTask(InputStream in, OutputStream out) {
        
        super();
        this.in = in;
        this.out = out;
        this.keepGoing = true;
    }
    
    public void stopCopying() {
        
        this.keepGoing = false;
    }
    
    public void run() {
        
        log.info("start piping ...");
        byte[] buf = new byte[BUFSIZE];
        
        if (in == null) {
            log.warning("null input stream, abort.");
        }
        if (out == null) {
            log.warning("null output stream, abort.");
        }
        
        try {
            int len = 0, total = 0;
            do {
                
                len = in.read(buf);
                if (len < 0) {
                    
                    break;
                }
                total += len;
                log.info(total + " piped");
                out.write(buf, 0, len);
                
                if (in.available() == 0) {
                    log.info("sleep a while");
                    yield();
                }
            } while(keepGoing);
            
        } catch (IOException e) {
            log.info(e.getMessage());
        }
        
        log.info("piping finished - " + keepGoing);
        notifyAll();
    }
}
