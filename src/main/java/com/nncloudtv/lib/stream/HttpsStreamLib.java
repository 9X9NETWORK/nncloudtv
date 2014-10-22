package com.nncloudtv.lib.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

// helper to streaming HTTPS video
public class HttpsStreamLib implements StreamLib {
    
    protected final static Logger log = Logger.getLogger(HttpsStreamLib.class.getName());
    
    public String normalizeUrl(String urlStr) {
        
        // always return null
        return null;
    }
    
    public boolean isUrlMatched(String urlStr) {
        
        if (urlStr == null) { return false; }
        
        try {
            
            URL url = new URL(urlStr);
            return url.getProtocol().equals("https");
            
        } catch (MalformedURLException e) {
            
            return false;
        }
    }
    
    public String getHtml5DirectVideoUrl(String urlStr) {
        
        return getDirectVideoUrl(urlStr);
    }
    
    public String getDirectVideoUrl(String urlStr) {
        
        // always return null
        return null;
    }
    
    public InputStream getDirectVideoStream(String urlStr) {
        
        if (urlStr == null) { return null; }
        
        try {
            
            URL url = new URL(urlStr);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            
            return conn.getInputStream();
            
        } catch (MalformedURLException e) {
            
            return null;
            
        } catch (IOException e) {
            
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
            
            return null;
        }
    }
    
}
