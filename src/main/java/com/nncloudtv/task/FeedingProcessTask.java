package com.nncloudtv.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

public class FeedingProcessTask extends Thread {
    
    protected static Logger log = Logger.getLogger(FeedingProcessTask.class.getName());
    
    Process    process = null;
    InputStream     in = null;
    OutputStream   out = null;
    BufferedReader err = null;
    boolean  keepGoing = true;
    final int  BUFSIZE = 1024;//76147;
    
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
        byte[] bytes = new byte[1024];
        
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
                
                len = in.read(bytes);
                if (len < 0) {
                    
                    break;
                }
                total += len;
                log.info(total + " feeded");
                out.write(bytes, 0, len);
                
                if (in.available() == 0) {
                    log.info("input stream is not available, sleep a while.");
                    sleep(1000);
                }
            } while(keepGoing);
            
        } catch (IOException e) {
            log.info(e.getMessage());
        } catch (InterruptedException e) {
            log.info(e.getMessage());
        }
        log.info("copy finished - " + keepGoing);
    }
}
