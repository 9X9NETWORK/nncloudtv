package com.nncloudtv.lib.video;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.api.client.util.IOUtils;
import com.nncloudtv.lib.NnStringUtil;

public class LiveStreamLib implements VideoLib {
    
    protected final static Logger log = Logger.getLogger(LiveStreamLib.class.getName());
    
    public static final String REGEX_LIVESTREAM_VIDEO_URL = "^https?:\\/\\/new\\.livestream\\.com\\/accounts\\/([0-9]+)\\/events\\/([0-9]+)\\/videos\\/([0-9]+)$";
    public static final String REGEX_LIVESTREAM_EVENT_URL = "^https?:\\/\\/new\\.livestream\\.com\\/accounts\\/([0-9]+)\\/events\\/([0-9]+)$";
    public static final String REGEX_LIVESTREAM_PAN_URL   = "^https?:\\/\\/new\\.livestream\\.com\\/(.+)(\\/(.+))+$";
    
    public boolean isUrlMatched(String url) {
        
        return (url == null) ? null : url.matches(REGEX_LIVESTREAM_PAN_URL);
    }
    
    public String getDirectVideoUrl(String urlStr) {
        
        if (urlStr == null) { return null; }
        
        if (urlStr.matches(REGEX_LIVESTREAM_VIDEO_URL)) {
            
            log.info("livestream video url format matched");
            
            urlStr = urlStr.replaceFirst("\\/\\/new\\.", "//api.new.");
            
            try {
                
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(conn.getInputStream(), baos);
                JSONObject videoJson = new JSONObject(new String(baos.toByteArray(), NnStringUtil.UTF8));
                
                String progressiveUrl = videoJson.getString("progressive_url");
                log.info("progressive_url = " + progressiveUrl);
                
                return progressiveUrl;
                
            } catch (MalformedURLException e) {
                
                log.warning(e.getClass().getName());
                log.warning(e.getMessage());
                
                return null;
                
            } catch (JSONException e) {
                
                log.warning(e.getClass().getName());
                log.warning(e.getMessage());
                
                return null;
                
            } catch (IOException e) {
                
                log.warning(e.getClass().getName());
                log.warning(e.getMessage());
                
                return null;
            }
            
        } else if (urlStr.matches(REGEX_LIVESTREAM_EVENT_URL)) {
            
            log.info("livestream event url format matched");
            
            urlStr = urlStr.replaceFirst("\\/\\/new\\.", "//api.new.");
            
            try {
                
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(conn.getInputStream(), baos);
                JSONObject respJson = new JSONObject(new String(baos.toByteArray(), NnStringUtil.UTF8));
                
                if (respJson.isNull("stream_info")) {
                    
                    JSONObject feedJson = respJson.getJSONObject("feed");
                    JSONArray dataJson = feedJson.getJSONArray("data");
                    if (dataJson.length() > 0) {
                        
                        String progressiveUrl = dataJson.getJSONObject(0).getString("progressive_url");
                        log.info("progressive_url = " + progressiveUrl);
                        
                        return progressiveUrl;
                    }
                    
                } else {
                    
                    JSONObject streamInfoJson = respJson.getJSONObject("stream_info");
                    
                    String m3u8Url = streamInfoJson.getString("m3u8_url");
                    log.info("m3u8_url = " + m3u8Url);
                    
                    return m3u8Url;
                }
                
            } catch (MalformedURLException e) {
                
                log.warning(e.getClass().getName());
                log.warning(e.getMessage());
                
                return null;
                
            } catch (JSONException e) {
                
                log.warning(e.getClass().getName());
                log.warning(e.getMessage());
                
                return null;
                
            } catch (IOException e) {
                
                log.warning(e.getClass().getName());
                log.warning(e.getMessage());
                
                return null;
            }
            
        } else if (urlStr.matches(REGEX_LIVESTREAM_PAN_URL)) {
            
            log.info("livestream pan url format matched");
            
            try {
                
                String metaTag = "meta[name=apple-itunes-app]";
                Document doc = Jsoup.connect(urlStr).get();
                Element element = doc.select(metaTag).first();
                if (element == null) {
                    log.warning("meta tag is not found " + metaTag);
                    return null;
                }
                
                String contentStr = element.attr("content");
                if (contentStr == null) {
                    log.warning("contentStr is null");
                    return null;
                }
                log.info(contentStr);
                String[] split = contentStr.split(",");
                for (String str : split) {
                    
                    str = str.trim();
                    String[] attr = str.split("=");
                    if (attr.length >= 2 && attr[0].trim().equals("app-argument")) {
                        
                        String appArgument = attr[1].trim();
                        log.info("app-argument = " + appArgument);
                        
                        if (appArgument != null && appArgument.matches(REGEX_LIVESTREAM_VIDEO_URL) || appArgument.matches(REGEX_LIVESTREAM_EVENT_URL)) {
                            
                            return getDirectVideoUrl(appArgument);
                        }
                    }
                }
                
                log.info("no proper app-argument found");
                
            } catch (IOException e) {
                
                log.warning(e.getClass().getName());
                log.warning(e.getMessage());
                
                return null;
            }
            
        }
        
        log.info("does not match any");
        
        return null;
    }
    
    public InputStream getDirectVideoStream(String url) {
        
        // need implement
        
        return null;
    }
    
}
