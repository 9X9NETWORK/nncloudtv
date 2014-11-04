package com.nncloudtv.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DurationFormatUtils;

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
    protected final int  BUFSIZE = 294001;
    protected byte[]         buf = null;
    protected long     startTime = 0;
    protected int    timeoutMili = 0;
    
    public PipingTask(InputStream in, OutputStream out, int timeout) {
        
        super();
        this.in = in;
        this.out = out;
        this.keepGoing = true;
        this.buf = new byte[BUFSIZE];
        this.startTime = NnDateUtil.timestamp();
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
        long lastTime  = startTime;
        try {
            do {
                len = in.read(buf);
                if (len < 0) {
                    
                    break;
                    
                } else if (len > 0) {
                    
                    total += len;
                    out.write(buf, 0, len);
                    
                    // progress log
                    if (total % 19 == 0 ) {
                        
                        long timestamp = NnDateUtil.timestamp();
                        long deltaMiliSec = (timestamp - lastTime);
                        
                        if (deltaMiliSec > 2000) {
                            
                            long deltaLen = total - lastTotal;
                            long totalMiliSec = timestamp - startTime;
                            float pipingSpeed = ((float) deltaLen / deltaMiliSec) * 8;
                            float avarageSpeed = ((float) total / totalMiliSec) * 8;
                            
                            System.out.println(String.format("[pipe] total = %s, speed = %5.1f kbits/s, avarage = %5.1f kbits/s, last %s", FileUtils.byteCountToDisplaySize(total), pipingSpeed, avarageSpeed, DurationFormatUtils.formatDurationHMS(totalMiliSec)));
                            
                            lastTime = timestamp;
                            lastTotal = total;
                        }
                    }
                }
                
                yield();
                if (in.available() == 0) {
                    log.fine("sleep a while");
                    sleep(31);
                }
                
                if (timeoutMili > 0 && NnDateUtil.timestamp() - startTime > timeoutMili) {
                    
                    log.warning("streaming is too long, give up.");
                    break;
                }
                
            } while(keepGoing);
            
        } catch (IOException e) {
            log.info("pipe closed");
        } catch (InterruptedException e) {
            log.info("pipe interrupted");
        }
        log.info("... piping finished");
        log.info("total piped size = " + FileUtils.byteCountToDisplaySize(total) + ", keepGoing = " + keepGoing);
    }
}
