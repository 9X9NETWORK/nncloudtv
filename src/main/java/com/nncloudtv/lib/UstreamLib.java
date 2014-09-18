package com.nncloudtv.lib;

import java.io.IOException;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class UstreamLib {
    
    protected static final Logger log = Logger.getLogger(UstreamLib.class.getName());
    
    public static String getDirectVideoUrl(String url) {
        
        if (url == null) { return null; }
        
        log.info("ustream url = " + url);
        
        try {
            Document doc = Jsoup.connect(url).get();
            Element element = doc.select("[name=\"ustream:channel_id\"]").first();
            if (element == null) {
                log.warning("meta tag is not found");
                return null;
            }
            String idStr = element.attr("content");
            if (idStr == null) {
                log.warning("idStr is empty");
                return null;
            }
            log.info("ustream channel ID = " + idStr);
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
            
        } catch (IOException e) {
            
            log.warning(e.getMessage());
        }
        
        return null;
    }
}
