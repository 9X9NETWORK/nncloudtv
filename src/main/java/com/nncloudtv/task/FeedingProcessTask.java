package com.nncloudtv.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Logger;

public class FeedingProcessTask extends Thread {
    
    protected static Logger log = Logger.getLogger(FeedingProcessTask.class.getName());
    
    Process    process = null;
    InputStream     in = null;
    OutputStream   out = null;
    BufferedReader err = null;
    boolean  keepGoing = true;
    final int  BUFSIZE = 76147;
    
    public FeedingProcessTask(InputStream in, Process process) {
        
        super();
        this.process = process;
        this.in = in;
        this.out = process.getOutputStream();
        this.err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        this.keepGoing = true;
    }
    
    public void stopCopying() {
        
        this.keepGoing = false;
    }
    
    public void run() {
        
        log.info("start copy stream ...");
        byte[] buf = new byte[1024];
        
        if (in == null) {
            log.warning("null input stream, abort.");
        }
        if (out == null) {
            log.warning("null output stream, abort.");
        }
        
        try {
            int len = 0, total = 0;
            do {
                while (err.ready()) {
                    String line = err.readLine();
                    if (line != null) {
                        log.info("avconv: " + line);
                    }
                }
                
                len = in.read(buf);
                if (len < 0) {
                    
                    break;
                }
                total += len;
                log.info(total + " feeded");
                out.write(buf, 0, len);
                
                if (in.available() == 0) {
                    log.info("sleep a while");
                    sleep(100);
                }
            } while(keepGoing);
            
        } catch (IOException e) {
            log.info(e.getMessage());
        } catch (InterruptedException e) {
            log.info(e.getMessage());
        } finally {
            
            
            
            
            
            
            
        }
        log.info("copy finished - " + keepGoing);
    }
}
