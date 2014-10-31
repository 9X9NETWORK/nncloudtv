package com.nncloudtv.lib.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.nncloudtv.lib.NnNetUtil;

public class VimeoLib implements StreamLib {
    
    protected static final Logger log = Logger.getLogger(VimeoLib.class.getName());
    
    public static final String REGEX_VIMEO_VIDEO_URL = "^https?:\\/\\/vimeo\\.com\\/(channels\\/.+\\/)?([0-9]+)$";
    
    public String normalizeUrl(String urlStr) {
        
        if (urlStr == null) { return null; }
        
        Matcher matcher = Pattern.compile(REGEX_VIMEO_VIDEO_URL).matcher(urlStr);
        if (matcher.find()) {
            
            return "https://vimeo.com/" + matcher.group(2);
        }
        
        return null;
    }
    
    public String getHtml5DirectVideoUrl(String urlStr) {
        
        return getDirectVideoUrl(urlStr);
    }
    
    public String getDirectVideoUrl(String urlStr) {
        
        if (urlStr == null) { return null; }
        
        String dataConfigUrl = null;
        String videoUrl = null;
        //step 1, get <div.player data-config-url>
        try {
            
            Document doc = Jsoup.connect(urlStr).get();
            Element element = doc.select("div.player").first();
            if (element != null) {
                
                dataConfigUrl = element.attr("data-config-url");
                log.info("vimeo data-config-url = " + dataConfigUrl);
            }
            
        } catch (IOException e) {
            
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
            return null;
        }
        if (dataConfigUrl == null) {
            
            log.info("vimeo div.player data-config-url not exisiting");
            return null;
        }
        //step 2, get json data
        String jsonStr = NnNetUtil.urlGet(dataConfigUrl);
        if (jsonStr == null) {
            
            log.warning("failed to get json data");
            return null;
        }
        JSONObject json = new JSONObject(jsonStr);
        try {
            
            JSONObject h264Json = json.getJSONObject("request").getJSONObject("files").getJSONObject("h264");
            if (h264Json.isNull("hd")) {
                
                log.info("fallback to sd resolution");
                videoUrl = h264Json.getJSONObject("sd").getString("url");
                
            } else {
                
                videoUrl = h264Json.getJSONObject("hd").getString("url");
            }
            
        } catch (JSONException e) {
            
            log.warning("vimeo json parsing failed");
            log.warning(e.getMessage());
        }
        
        log.info("vimeo direct url = " + videoUrl);
        
        return videoUrl;
    }
    
    public boolean isUrlMatched(String urlStr) {
        
        return (urlStr == null) ? false : urlStr.matches(REGEX_VIMEO_VIDEO_URL);
    }
    
    public InputStream getDirectVideoStream(String urlStr) {
        
        return YouTubeLib.youtubeDL(urlStr);
    }
}
