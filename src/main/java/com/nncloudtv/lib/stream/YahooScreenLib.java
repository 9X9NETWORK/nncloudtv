package com.nncloudtv.lib.stream;

import java.io.InputStream;
import java.util.logging.Logger;

public class YahooScreenLib implements StreamLib {
    
    protected static final Logger log = Logger.getLogger(YahooScreenLib.class.getName());
    
    public static final String REGEX_YAHOO_SCREEN_URL = "^https?:\\/\\/(ww\\.)?screen\\.yahoo\\.com\\/(.+)$";
    
    @Override
    public boolean isUrlMatched(String urlStr) {
        
        return (urlStr == null) ? false : urlStr.matches(REGEX_YAHOO_SCREEN_URL);
    }
    
    @Override
    public String normalizeUrl(String urlStr) {
        
        return urlStr;
    }
    
    @Override
    public String getDirectVideoUrl(String urlStr) {
        
        return YouTubeLib.getYouTubeDLUrl(urlStr);
    }
    
    @Override
    public String getHtml5DirectVideoUrl(String urlStr) {
        
        return YouTubeLib.getYouTubeDLUrl(urlStr);
    }
    
    @Override
    public InputStream getDirectVideoStream(String urlStr) {
        
        return YouTubeLib.getYouTubeDLStream(urlStr);
    }
    
}
