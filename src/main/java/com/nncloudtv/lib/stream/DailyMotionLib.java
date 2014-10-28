package com.nncloudtv.lib.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.nncloudtv.lib.NnNetUtil;

public class DailyMotionLib implements StreamLib {
    
    protected final static Logger log = Logger.getLogger(DailyMotionLib.class.getName());
    
    public static final String REGEX_DAILYMOTION_VIDEO_URL = "^https?:\\/\\/www\\.dailymotion\\.com\\/(embed\\/)?video\\/([^_]+)(_.*)?$";
    
    public boolean isUrlMatched(String urlStr) {
        
        if (urlStr == null) { return false; }
        
        if (urlStr.matches(REGEX_DAILYMOTION_VIDEO_URL)) {
            
            return true;
        }
        
        return false;
    }
    
    public String getEmbedUrl(String urlStr) {
        
        if (urlStr == null) { return null; }
        
        Matcher matcher = Pattern.compile(REGEX_DAILYMOTION_VIDEO_URL).matcher(urlStr);
        if (matcher.find()) {
            
            return "http://www.dailymotion.com/embed/video/" + matcher.group(2);
        }
        
        return null;
    }
    
    public String normalizeUrl(String urlStr) {
        
        if (urlStr == null) { return null; }
        
        Matcher matcher = Pattern.compile(REGEX_DAILYMOTION_VIDEO_URL).matcher(urlStr);
        if (matcher.find()) {
            
            return "http://www.dailymotion.com/video/" + matcher.group(2);
        }
        
        return null;
    }
    
    public String getDirectVideoUrl(String urlStr) {
        
        
        if (urlStr == null) { return null; }
        
        String embedUrl = getEmbedUrl(urlStr);
        if (embedUrl == null) {
            
            log.warning("fail to get embed url");
            return null;
        }
        log.info("embed url = " + embedUrl);
        String regex = " var info = (\\{.+\\}),\\s";
        String content = NnNetUtil.urlGet(embedUrl);
        if (content == null) {
            
            log.warning("fail to get embed content");
            return null;
        }
        log.info("embed content size = " + content.length());
        Matcher matcher = Pattern.compile(regex).matcher(content);
        if (!matcher.find()) {
            
            log.warning("fail to find pattern");
            return null;
        }
        
        try {
            
            JSONObject json = new JSONObject(matcher.group(1));
            System.out.println(json.toString());
            String h264 = null;
            String[] qualities = { "stream_hls_url", "stream_h264_hd_url", "stream_h264_hq_url", "stream_h264_url" };
            for (String quality : qualities) {
                if (json.isNull(quality) == false) {
                    
                    log.info(quality);
                    h264 = json.getString(quality);
                    break;
                }
            }
            log.info("h264 = " + h264);
            if (h264 == null) {
                
                log.warning("fail to get h264 url");
                return null;
            }
            
            return h264;
            
        } catch (JSONException e) {
            
            log.warning(e.getMessage());
            return null;
        }
    }
    
    public String getHtml5DirectVideoUrl(String urlStr) {
        
        // DailyMotion is IP restricted
        return null;
    }
    
    public InputStream getDirectVideoStream(String urlStr) {
        
        if (urlStr == null) { return null; }
        
        String embedUrl = getDirectVideoUrl(urlStr);
        if (embedUrl == null) {
            
            log.warning("fail to get direct video url");
            return null;
        }
        
        try {
            
            return NnNetUtil.getConn(embedUrl).getInputStream();
            
        } catch (IOException e) {
            
            log.warning("failed to get input stream");
            return null;
        }
    }
}
