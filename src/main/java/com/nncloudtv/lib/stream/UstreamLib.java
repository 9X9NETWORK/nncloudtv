package com.nncloudtv.lib.stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.NnStringUtil;

public class UstreamLib implements StreamLib {
    
    protected static final Logger log = Logger.getLogger(UstreamLib.class.getName());
    
    public static final String REGEX_USTREAM_URL = "^https?:\\/\\/www\\.ustream\\.tv\\/(channel\\/)?(.+)$";
    
    public boolean isUrlMatched(String urlStr) {
        
        return (urlStr == null) ? null : urlStr.matches(REGEX_USTREAM_URL);
    }
    
    public String normalizeUrl(String urlStr) {
        
        if (urlStr == null || !isUrlMatched(urlStr)) { return null; }
        
        String ustreamId = getUstreamId(urlStr);
        if (ustreamId == null) {
            
            log.warning("fail to get ustreamId");
            return null;
        }
        
        String normalizedUrl = "http://www.ustream.tv/channel/" + ustreamId;
        try {
            
            // check 301 status
            URL url = new URL(normalizedUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            int code = conn.getResponseCode();
            log.info("status code = " + code);
            if (code == 301) {
                
                String location = conn.getHeaderField("Location");
                log.info("fetch redirection");
                if (location != null) {
                    
                    return location;
                }
            }
            
        } catch (MalformedURLException e) {
            
            log.info(e.getMessage());
            
        } catch (IOException e) {
            
            log.info(e.getMessage());
            
        }
        
        return normalizedUrl;
    }
    
    public static String getUstreamId(String urlStr) {
        
        if (urlStr == null) { return null; }
        
        try {
            
            Document doc = Jsoup.connect(urlStr).get();
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
    
    public String getHtml5DirectVideoUrl(String urlStr) {
        
        if (urlStr == null) { return null; }
        
        String firstUrl = getDirectVideoUrl(urlStr);
        if (firstUrl == null) {
            
            log.info("fail to get first step url");
            return null;
        }
        
        try {
            
            URL url = new URL(firstUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), NnStringUtil.UTF8));
                String buf = null, last = null;
                
                while ((buf = reader.readLine()) != null) {
                    
                    System.out.println(buf);
                    
                    if (buf.isEmpty() || buf.startsWith("#")) {
                        
                        continue;
                    }
                    
                    last = buf;
                }
                
                if (last != null) {
                    
                    return last;
                }
            }
            
        } catch (MalformedURLException e) {
            
            log.warning(e.getMessage());
            
        } catch (IOException e) {
            
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
        }
        
        return firstUrl;
    }
    
    public String getDirectVideoUrl(String urlStr) {
        
        if (urlStr == null) { return null; }
        
        try {
            
            String idStr = getUstreamId(urlStr);
            if (idStr == null) {
                return null;
            }
            String jsonUrl = "http://api.ustream.tv/channels/" + idStr + ".json";
            log.info("json url = " + jsonUrl);
            String jsonStr = NnNetUtil.urlGet(jsonUrl);
            if (jsonStr == null) {
                
                log.warning("fail to get json data");
                return null;
            }
            
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
    
    public InputStream getDirectVideoStream(String url) {
        
        // need implement
        
        return null;
    }
}
