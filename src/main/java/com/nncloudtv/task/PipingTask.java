package com.nncloudtv.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Logger;

import com.nncloudtv.lib.NnDateUtil;

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
    protected Date     startTime = null;
    protected int    timeoutMili = 0;
    
    public PipingTask(InputStream in, OutputStream out, int timeout) {
        
        super();
        this.in = in;
        this.out = out;
        this.keepGoing = true;
        this.buf = new byte[BUFSIZE];
        this.startTime = NnDateUtil.now();
        this.timeoutMili = timeout * 1000;
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
        
        
        int  len       = 0;
        long total     = 0;
        long lastTotal = 0;
        Date lastTime  = startTime;
        try {
            do {
                len = in.read(buf);
                if (len < 0) {
                    
                    break;
                    
                } else if (len > 0) {
                    
                    total += len;
                    log.fine(total + " piped");
                    out.write(buf, 0, len);
                    
                    // progress log
                    if (total % 13 == 0 ) {
                        
                        Date now = NnDateUtil.now();
                        long deltaMiliSec = (now.getTime() - lastTime.getTime());
                        
                        if (deltaMiliSec > 1000) {
                            
                            long deltaLen = total - lastTotal;
                            long totalMiliSec = now.getTime() - startTime.getTime();
                            float pipingSpeed = ((float) deltaLen / deltaMiliSec) * 8;
                            float avarageSpeed = ((float) total / totalMiliSec) * 8;
                            float lastMinutes = (float) totalMiliSec / 60 / 1000;
                            
                            log.info(String.format("piping speed = %5.1f kbits/s, avarage = %5.1f kbits/s, last %3.1f minutes", pipingSpeed, avarageSpeed, lastMinutes));
                            
                            lastTime = now;
                            lastTotal = total;
                        }
                    }
                }
                
                yield();
                if (in.available() == 0) {
                    log.fine("sleep a while");
                    sleep(100);
                }
                
                if (timeoutMili > 0 && NnDateUtil.now().getTime() - startTime.getTime() > timeoutMili) {
                    
                    log.warning("streaming is too long, give up.");
                    break;
                }
                
            } while(keepGoing);
            
        } catch (IOException e) {
            log.info("IO");
        } catch (InterruptedException e) {
            log.info("Interrupted");
        }
        log.info("... piping finished");
        log.info("total piped size = " + total + ", keepGoing = " + keepGoing);
    }
}
