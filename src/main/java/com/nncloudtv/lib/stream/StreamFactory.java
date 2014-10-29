package com.nncloudtv.lib.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.task.FeedingAvconvTask;
import com.nncloudtv.task.PipingTask;

/**
 * Stream Factory
 * 
 * @author louis
 *
 */
public class StreamFactory {
    
    protected final static Logger log = Logger.getLogger(StreamFactory.class.getName());
    
    public static StreamLib getStreamLib(String url) {
        
        if (url == null) { return null; }
        
        StreamLib[] StreamLibs = {
                
                new YouTubeLib(),
                new VimeoLib(),
                new UstreamLib(),
                new LiveStreamLib(),
                new DailyMotionLib(),
                new S3Helper(),
                new HttpsHelper(),
        };
        
        for (StreamLib lib : StreamLibs) {
            
            if (lib.isUrlMatched(url)) {
                
                log.info(lib.getClass() + " matched");
                
                return lib;
            }
        }
        
        log.info("no streamlib matched");
        
        return null;
    }
    
    public static void streamTo(String videoUrl, HttpServletResponse resp) {
        
        log.info("streamTo " + videoUrl);
        if (videoUrl == null || resp == null) { return; }
        
        StreamLib streamLib = getStreamLib(videoUrl);
        if (streamLib != null) {
            
            String directVideoUrl = streamLib.getDirectVideoUrl(videoUrl);
            if (directVideoUrl != null) {
                
                videoUrl = directVideoUrl;
            }
        } else {
            
            log.info("direct link");
        }
        
        try {
            
            NnNetUtil.proxyTo(videoUrl, resp);
            log.info("streamTo finished");
            
        } catch (IOException e) {
            // maybe player closed
            log.info("streamTo stopped");
        }
    }
    
    public static void streaming(String videoUrl, OutputStream videoOut) throws MalformedURLException {
        
        log.info("streamming " + videoUrl);
        if (videoUrl == null || videoOut == null) { return; }
        
        InputStream videoIn = null;
        StreamLib streamLib = getStreamLib(videoUrl);
        if (streamLib != null) {
            
            String directVideoUrl = streamLib.getDirectVideoUrl(videoUrl);
            if (directVideoUrl != null && !directVideoUrl.startsWith("https")) {
                
                videoUrl = directVideoUrl;
                
            } else {
                
                InputStream directVideoStream = streamLib.getDirectVideoStream(videoUrl);
                if (directVideoStream != null) {
                    
                    log.info("direct video stream");
                    videoIn = directVideoStream;
                }
            }
        }
        
        FeedingAvconvTask feedingAvconvTask = null;
        PipingTask pipingTask = null;
        
        try {
            String cmd = "/usr/bin/avconv -v debug -i "
                       + ((videoIn == null) ? NnStringUtil.escapeURLInShellArg(videoUrl) : "/dev/stdin")
                       + " -vcodec mpeg2video -acodec ac3 -ab 128k -f mpegts -y pipe:1";
            
            log.info("[exec] " + cmd);
            
            Process process = Runtime.getRuntime().exec(cmd);
            
            pipingTask = new PipingTask(process.getInputStream(), videoOut, 0);
            pipingTask.start();
            
            if (videoIn != null) {
                feedingAvconvTask = new FeedingAvconvTask(videoIn, process, 0);
                feedingAvconvTask.start();
            }
            
            pipingTask.join();
            log.info("streaming done");
            
        } catch (InterruptedException e) {
            log.warning(e.getMessage());
            return;
        } catch (IOException e) {
            // maybe player closed
            log.info("streaming stopped");
            log.info(e.getMessage());
        } finally {
            if (feedingAvconvTask != null) {
                feedingAvconvTask.stopCopying();
            }
            if (pipingTask != null) {
                pipingTask.stopCopying();
            }
        }
    }
}
