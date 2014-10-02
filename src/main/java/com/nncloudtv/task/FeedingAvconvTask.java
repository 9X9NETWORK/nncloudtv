package com.nncloudtv.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.logging.Logger;

import com.nncloudtv.lib.NnDateUtil;

public class FeedingAvconvTask extends PipingTask {
    
    protected static Logger log = Logger.getLogger(FeedingAvconvTask.class.getName());
    
    Process    process = null;
    BufferedReader err = null;
    Date     startTime = null;
    int    timeoutMili = 0;
    
    public FeedingAvconvTask(InputStream in, Process process, int timeout) {
        
        super(in, process.getOutputStream());
        this.process = process;
        this.err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        this.startTime = NnDateUtil.now();
        this.timeoutMili = timeout * 1000;
    }
    
    public void run() {
        
        log.info("start feeding avconv ...");
        
        if (in == null) {
            log.warning("null input stream, abort.");
        }
        if (out == null) {
            log.warning("null output stream, abort.");
        }
        
        int total = 0, len = 0;
        try {
            do {
                while (err.ready() && (total % 2) == 0) {
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
                    log.fine("sleep a little while");
                    sleep(100);
                }
                
                if (timeoutMili > 0 && NnDateUtil.now().getTime() - startTime.getTime() > timeoutMili) {
                    
                    log.warning("streaming is too long, give up.");
                    break;
                }
                
            } while(keepGoing);
            
        } catch (IOException e) {
            log.info(e.getMessage());
        } catch (InterruptedException e) {
            log.info(e.getMessage());
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
        }
        log.info("... feeding avconv finished");
        log.info("total feeded size = " + total + ", keepGoing = " + keepGoing);
    }
}
