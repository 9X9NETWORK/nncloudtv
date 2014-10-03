package com.nncloudtv.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.nncloudtv.model.Mso;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.task.FeedingAvconvTask;
import com.nncloudtv.task.PipingTask;

public class UstreamLib {
    
    protected static final Logger log = Logger.getLogger(UstreamLib.class.getName());
    
    public static final String REGEX_USTREAM_URL = "^https?:\\/\\/www\\.ustream\\.tv\\/(channels\\/)?(.+)$";
    
    public static String getUstreamChannelId(String url) {
        
        if (url == null) { return null; }
        
        try {
            Document doc = Jsoup.connect(url).get();
            Element element = doc.select("meta[name=ustream:channel_id]").first();
            if (element == null) {
                log.warning("meta tag is not found");
                return null;
            }
            String idStr = element.attr("content");
            if (idStr == null) {
                log.warning("idStr is empty");
                return null;
            }
            log.info("ustream channel_id = " + idStr);
            
            return idStr;
            
        } catch (Exception e) {
            
            log.warning(e.getMessage());
        }
        
        return null;
    }
    
    public static void steaming(String videoUrl, OutputStream videoOut) throws MalformedURLException {
        
        URL url = null;
        HttpURLConnection conn = null;
        log.info("videoUrl = " + videoUrl);
        if (videoUrl == null || videoOut == null) { return; }
        
        try {
            url = new URL(videoUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            
        } catch (IOException e) {
            log.warning("can not open url");
            return;
        }
        
        InputStream videoIn = null;
        Matcher s3Matcher = Pattern.compile(AmazonLib.REGEX_S3_URL).matcher(videoUrl);
        Matcher vimeoMatcher = Pattern.compile(VimeoLib.REGEX_VIMEO_VIDEO_URL).matcher(videoUrl);
        Matcher ustreamMatcher = Pattern.compile(UstreamLib.REGEX_USTREAM_URL).matcher(videoUrl);
        
        if (ustreamMatcher.find()) {
            
            log.info("ustream url format");
            
            videoUrl = UstreamLib.getDirectVideoUrl(videoUrl);
            if (videoUrl == null) {
                log.warning("parsing ustream url failed");
                return;
            }
        } else if (vimeoMatcher.find()) {
            
            log.info("vimeo url format");
            
            videoUrl = VimeoLib.getDirectVideoUrl(videoUrl);
            if (videoUrl == null) {
                log.warning("parsing vimeo url failed");
                return;
            }
        } else if (s3Matcher.find()) {
            
            log.info("S3 url format");
            String bucket = s3Matcher.group(1);
            String filename = s3Matcher.group(2);
            Mso mso = NNF.getConfigMngr().findMsoByVideoBucket(bucket);
            AWSCredentials credentials = new BasicAWSCredentials(MsoConfigManager.getAWSId(mso),
                                                                 MsoConfigManager.getAWSKey(mso));
            AmazonS3 s3 = new AmazonS3Client(credentials);
            try {
                S3Object s3Object = s3.getObject(new GetObjectRequest(bucket, filename));
                videoIn = s3Object.getObjectContent();
            } catch (AmazonS3Exception e) {
                log.warning(e.getMessage());
                return;
            }
        } else if (videoUrl.matches(YouTubeLib.REGEX_VIDEO_URL)) {
            
            log.info("youtube url format");
            String cmd = "/usr/bin/youtube-dl -v --no-cache-dir -o - "
                       + NnStringUtil.escapeURLInShellArg(videoUrl);
            log.info("[exec] " + cmd);
            
            try {
                Process process = Runtime.getRuntime().exec(cmd);
                videoIn = process.getInputStream();
                // piping error message to stdout
                PipingTask pipingTask = new PipingTask(process.getErrorStream(), System.out, 0);
                pipingTask.start();
            } catch (IOException e) {
                log.warning(e.getMessage());
                return;
            }
        } else if (url.getProtocol().equals("https")) {
            
            log.info("https url format");
            try {
                videoIn = conn.getInputStream();
            } catch (IOException e) {
                log.warning(e.getMessage());
                return;
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
    
    public static String getDirectVideoUrl(String url) {
        
        if (url == null) { return null; }
        
        log.info("ustream url = " + url);
        
        try {
            
            String idStr = getUstreamChannelId(url);
            if (idStr == null) {
                return null;
            }
            String jsonUrl = "http://api.ustream.tv/channels/" + idStr+ ".json";
            log.info("json url = " + jsonUrl);
            String jsonStr = NnNetUtil.urlGet(jsonUrl);
            
            JSONObject jsonObj = new JSONObject(jsonStr);
            String m3u8 = jsonObj.getJSONObject("channel").getJSONObject("stream").get("hls").toString();
            log.info("m3u8 = " + m3u8);
            
            return m3u8;
            
        } catch (JSONException e) {
            
            log.warning(e.getMessage());
            
        } catch (NullPointerException e) {
            
            log.warning(e.getMessage());
            
        }
        
        return null;
    }
}
