package com.nncloudtv.lib.video;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.task.FeedingAvconvTask;
import com.nncloudtv.task.PipingTask;

/**
 * Video Factory
 * 
 * @author louis
 *
 */
public class VideoFacroty {
    
    protected final static Logger log = Logger.getLogger(VideoFacroty.class.getName());
    
    public static VideoLib getVideoLib(String url) {
        
        if (url == null) { return null; }
        
        VideoLib[] videoLibs = {
                
                new YouTubeLib(),
                new VimeoLib(),
                new UstreamLib(),
                new LiveStreamLib(),
                new S3VideoLib(),
                new HttpsVideoLib(),
        };
        
        for (VideoLib lib : videoLibs) {
            
            if (lib.isUrlMatched(url)) {
                
                log.info(lib.getClass() + " matched");
                
                return lib;
            }
        }
        
        log.info("no videoLib matched");
        
        return null;
    }
    
    public static void streaming(String videoUrl, OutputStream videoOut) throws MalformedURLException {
        
        log.info("videoUrl = " + videoUrl);
        if (videoUrl == null || videoOut == null) { return; }
        
        // check url format
        try {
            URL url = new URL(videoUrl);
            url.openConnection();
        } catch (MalformedURLException e) {
            log.warning("invalid url format");
            return;
        } catch (IOException e) {
            log.warning("fail to open url");
            return;
        }
        
        InputStream videoIn = null;
        log.info("Q0");
        VideoLib videoLib = getVideoLib(videoUrl);
        log.info("Q1");
        String directVideoUrl = videoLib.getDirectVideoUrl(videoUrl);
        log.info("Q3");
        if (directVideoUrl != null) {
            
            log.info("direct video url = " + directVideoUrl);
            videoUrl = directVideoUrl;
            
        } else {
            
            InputStream directVideoStream = videoLib.getDirectVideoStream(videoUrl);
            if (directVideoStream != null) {
                
                log.info("direct video stream");
                videoIn = directVideoStream;
            }
        }
        log.info("Q4");
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
