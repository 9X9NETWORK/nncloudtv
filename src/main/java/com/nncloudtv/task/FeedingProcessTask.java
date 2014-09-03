package com.nncloudtv.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.logging.Logger;

import com.nncloudtv.lib.NnDateUtil;

public class FeedingProcessTask extends PipingTask {
    
    protected static Logger log = Logger.getLogger(FeedingProcessTask.class.getName());
    
    Process    process = null;
    BufferedReader err = null;
    Date     startTime = null;
    
    public FeedingProcessTask(InputStream in, Process process) {
        
        super(in, process.getOutputStream());
        this.process = process;
        this.err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        this.startTime = NnDateUtil.now();
    }
    
    public void run() {
        
        log.info("start feeding process ...");
        
        if (in == null) {
            log.warning("null input stream, abort.");
        }
        if (out == null) {
            log.warning("null output stream, abort.");
        }
        
        int total = 0, len = 0;
        try {
            do {
                while (err.ready()) {
                    String line = err.readLine();
                    if (line != null) {
                        log.info("[avconv] " + line);
                    }
                }
                
                len = in.read(buf);
                if (len < 0) {
                    
                    break;
                }
                total += len;
                log.fine(total + " feeded");
                out.write(buf, 0, len);
                
                yield();
                if (in.available() == 0) {
                    log.fine("sleep a while");
                    sleep(100);
                }
                
                if (NnDateUtil.now().getTime() - startTime.getTime() > 20000) { // 20 seconds
                    
                    log.warning("streaming is too long, give up.");
                    keepGoing = false;
                }
                
            } while(keepGoing);
            
        } catch (IOException e) {
            log.info(e.getMessage());
        } catch (InterruptedException e) {
            log.info(e.getMessage());
        }
        
        log.info("total feeded size = " + total);
        log.info("copy finished - " + keepGoing);
    }
}
