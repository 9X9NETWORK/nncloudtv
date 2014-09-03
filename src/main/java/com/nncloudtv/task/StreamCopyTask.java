package com.nncloudtv.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Logger;

public class StreamCopyTask extends Thread {
    
    protected static Logger log = Logger.getLogger(StreamCopyTask.class.getName());
    
    InputStream  in = null;
    OutputStream out = null;
    BufferedReader err = null;
    boolean keepGoing = true;
    final int BUFSIZE = 76147;
    
    public StreamCopyTask(InputStream in, OutputStream out, InputStream err) {
        
        super();
        this.in = in;
        this.out = out;
        this.err = new BufferedReader(new InputStreamReader(err));
        this.keepGoing = true;
    }
    
    public void stopCopying() {
        
        this.keepGoing = false;
    }
    
    public void run() {
        
        log.info("start copy stream ...");
        
        if (in == null) {
            log.warning("null input stream, abort.");
        }
        if (out == null) {
            log.warning("null output stream, abort.");
        }
        
        try {
            byte[] buf = new byte[BUFSIZE];
            int len = 0, total = 0;
            do {
                String line = null;
                while ((line = err.readLine()) != null) {
                    
                    if (line != null) {
                        log.info("avconv: " + line);
                    }
                }
                
                len = in.read(buf);
                if (len < 0) {
                    
                    break;
                }
                total += len;
                log.info(total  + " copied");
                out.write(buf, 0, len);
                
            } while(keepGoing);
            
        } catch (IOException e) {
            log.info(e.getMessage());
        }
        log.info("copy finished - " + keepGoing);
    }
}
