package com.nncloudtv.lib.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnNetUtil;

public class VimeoLib implements StreamLib {
    
    protected static final Logger log = Logger.getLogger(VimeoLib.class.getName());
    
    public static final String REGEX_VIMEO_VIDEO_URL = "^https?:\\/\\/vimeo\\.com\\/([0-9]+)$";
    
    public String getDirectVideoUrl(String url) {
        
        if (url == null) { return null; }
        
        String dataConfigUrl = null;
        String videoUrl = null;
        //step 1, get <div.player data-config-url>
        try {
            Document doc = Jsoup.connect(url).get();
            Element element = doc.select("div.player").first();
            if (element != null) {
                dataConfigUrl = element.attr("data-config-url");
                log.info("vimeo data-config-url=" + dataConfigUrl);
            }
        } catch (IOException e) {
            log.info("vimeo div.player data-config-url not exisiting");
            NnLogUtil.logException(e);
            return null;
        }
        if (dataConfigUrl == null)
            return null;
        //step 2, get json data
        String jsonStr = NnNetUtil.urlGet(dataConfigUrl);
        JSONObject json = new JSONObject(jsonStr);
        try {
            videoUrl = json.getJSONObject("request").getJSONObject("files").getJSONObject("h264").getJSONObject("hd").get("url").toString();
        } catch (JSONException e) {
            log.info("vimeo hd failed");
            NnLogUtil.logException(e);
        }
        return videoUrl;
    }
    
    public boolean isUrlMatched(String url) {
        
        return (url == null) ? null : url.matches(REGEX_VIMEO_VIDEO_URL);
    }
    
    public InputStream getDirectVideoStream(String url) {
        
        // need implement
        
        return null;
    }
}
