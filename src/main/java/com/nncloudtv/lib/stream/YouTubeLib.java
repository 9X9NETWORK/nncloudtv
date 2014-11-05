package com.nncloudtv.lib.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.IOUtils;
import com.google.api.client.util.Key;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.PlaylistFeed;
import com.google.gdata.util.ServiceException;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.task.PipingTask;
import com.nncloudtv.web.api.NnStatusCode;

public class YouTubeLib  implements StreamLib {
    
    protected static final Logger log = Logger.getLogger(YouTubeLib.class.getName());
    
    public static final String REGEX_YOUTUBE_PLAYLIST  = "^https?:\\/\\/www\\.youtube\\.com\\/playlist\\?list=([^&]+).*$";
    public static final String REGEX_VIDEO_URL         = "^https?:\\/\\/www\\.youtube\\.com\\/watch\\?v=([^&]+).*$";
    public static final String REGEX_VIDEO_ID_STR      = "v=([^&]+)";
    public static final String YOUTUBE_CHANNEL_PREFIX  = "http://www.youtube.com/user/";
    public static final String YOUTUBE_PLAYLIST_PREFIX = "http://www.youtube.com/view_play_list?p=";
        
    /** 
     * 1. remove those invalid keywords we already know.
     * 2. merge the following youtube channel formats to one, http://www.youtube.com/user/<userid>
     *    http://www.youtube.com/<usrid>
     *    http://www.youtube.com/user/<usrid>
     *    http://www.youtube.com/profile?user=<usrid>
     * 3. merge the following youtube playlist formats to one, http://www.youtube.com/view_play_list?p=<pid>
     *    http://www.youtube.com/view_play_list?p=<pid>
     *    http://www.youtube.com/playlist?list=PL03D59E2ECDDA66DF
     *    http://www.youtube.com/user/UCBerkeley#p/c/<pid>
     *    http://www.youtube.com/user/UCBerkeley#g/c/<pid>
     *    http://www.youtube.com/user/UCBerkeley#p/c/<pid>/0/-dQltKG3NlI
     *    http://www.youtube.com/profile?user=UCBerkeley#grid/user/<pid>
     *    http://www.youtube.com/watch?v=-dQltKG3NlI&p=<pid>
     *    http://www.youtube.com/watch?v=-dQltKG3NlI&playnext=1&list=<pid>
     * 4. youtube api call (disabled for now)
     * Example1: they should all become http://www.youtube.com/user/davidbrucehughes    
     *    http://www.youtube.com/profile?user=davidbrucehughes#g/u
     *    http://www.youtube.com/davidbrucehughes#g/a
     *    http://www.youtube.com/user/davidbrucehughes#g/p
     * Example2: they should all become http://www.youtube.com/user/view_play_list?p=03D59E2ECDDA66DF
     *    http://www.youtube.com/playlist?list=PL03D59E2ECDDA66DF
     *    http://www.youtube.com/view_play_list?p=03D59E2ECDDA66DF
     *    http://www.youtube.com/user/UCBerkeley#p/c/03D59E2ECDDA66DF
     *    http://www.youtube.com/user/UCBerkeley#g/c/095393D5B42B2266
     *    http://www.youtube.com/user/UCBerkeley#p/c/03D59E2ECDDA66DF/0/-dQltKG3NlI
     *    http://www.youtube.com/profile?user=UCBerkeley#grid/user/03D59E2ECDDA66DF
     *    http://www.youtube.com/watch?v=-dQltKG3NlI&p=03D59E2ECDDA66DF
     *    http://www.youtube.com/watch?v=-dQltKG3NlI&playnext=1&list=PL03D59E2ECDDA66DF
     *    http://www.youtube.com/watch?v=-dQltKG3NlI&playnext=1&list=PL03D59E2ECDDA66DF&feature=list_related
     */        
    public static String formatCheck(String urlStr) {
        if (urlStr == null) {return null;}
        String[] invalid = {"index", "videos",
                            "entertainment", "music", "news", "movies",
                            "comedy", "gaming", "sports", "education",
                            "shows", 
                            "store", "channels", "contests_main"};
        HashSet<String> dic = new HashSet<String>();
        for (int i=0; i<invalid.length; i++) {
            dic.add(invalid[i]);
        }
        //youtube channel
        String url = null;
        String reg = "^(http|https)://?(www.)?youtube.com/(user/|profile\\?user=)?(\\w+)";
        Pattern pattern = Pattern.compile(reg);
        Matcher m = pattern.matcher(urlStr);
        while (m.find()) {
            if (dic.contains(m.group(4))) {return null;}
            url = "http://www.youtube.com/user/" + m.group(4);
        }
        //youtube playlist
        reg = "^(http|https)://?(www.)?youtube.com/(user/|profile\\?user=)?(.+)(#(p/c|g/c|grid/user)/(\\w+))";
        pattern = Pattern.compile(reg);
        m = pattern.matcher(urlStr);
        while (m.find()) {
            url = "http://www.youtube.com/view_play_list?p=" + m.group(7);
        }
        reg = "^(http|https)://?(www.)?youtube.com/view_play_list\\?p=(\\w+)";
        pattern = Pattern.compile(reg);
        m = pattern.matcher(urlStr);        
        while (m.find()) {
            //log.info("match:view_play_list\\?p=(\\w+)");
            url = "http://www.youtube.com/view_play_list?p=" + m.group(3);
        }        
        //http://www.youtube.com/playlist?list=PLJ2QT-PhqTiI-BWE0Efr4rhbfVO-Qg_4q
        reg = "^(http|https)://?(www.)?youtube.com/(.+)?(p|list)=(PL)?([\\w|_|-]+)";
        pattern = Pattern.compile(reg);
        m = pattern.matcher(urlStr);
        while (m.find()) {
            //log.info("match:(p|list)=(PL)?(\\w+)");
            url = "http://www.youtube.com/view_play_list?p=" + m.group(6);
        }
        
        // http://www.youtube.com/playlist?list=03D59E2ECDDA66DF
        // http://www.youtube.com/playlist?list=PL03D59E2ECDDA66DF               
        reg = "^(http|https)://?(www.)?youtube.com/playlist?list=(PL)?(\\w+)";
        pattern = Pattern.compile(reg);
        m = pattern.matcher(urlStr);
        while (m.find()) {
            //log.info("match playlist?list=(PL)?(\\w+)");
            url = "http://www.youtube.com/view_play_list?p=" + m.group(5);
        }
        
        if (url != null) { 
            //url = url.toLowerCase();
            if (url.equals("http://www.youtube.com/user/watch")) {
                url = null;
            }
        }
        log.info("original url:" + urlStr + ";result=" + url);        
        //if (!youTubeCheck(result)) {return null;} //till the function is fixed        
        return url;        
    }
    
     public static class YouTubeUrl extends GenericUrl {
        @Key public final String alt = "jsonc";
        @Key public String author;
        @Key public String q;
        @Key("max-results") public Integer maxResults;
        public YouTubeUrl(String url) {
          super(url);
        }
    }
     
    public static class VideoFeed {
       @Key public String title;
       @Key public String subtitle;
       @Key public String logo;
       @Key public String description;
       @Key public String author;
       @Key public int totalItems;
       @Key public List<Video> items;
    }
    
    public static class Author {
        @Key public String name;
    }
    
    public static class MyFeed {
       @Key public List<Video> items;
    }
    
    public static class Video {
        @Key public String id;
        @Key public String title;
        @Key public String description;
        @Key public Thumbnail thumbnail;
        @Key public Player player;
        @Key public Video video;
    }
    
    public static class Thumbnail {
        @Key public String sqDefault;
    }
    
    public static class ProfileFeed {
        @Key public List<String> items;
    }
        
    public static class Player {
        @Key("default") public String defaultUrl;
    }
    
    public static HttpRequestFactory getFactory() {
        HttpTransport transport = new NetHttpTransport();
        HttpRequestFactory factory = transport.createRequestFactory();
        return factory;
    }
    
    public static Map<String, String> getYouTubeVideo(String videoId) {
        Map<String, String> results = new HashMap<String, String>();
        HttpRequestFactory factory = YouTubeLib.getFactory();        
        HttpRequest request;
        MyFeed feed;
        try {
            //https://gdata.youtube.com/feeds/api/videos/nIbzpk8FjbU?v=2&alt=jsonc            
            YouTubeUrl videoUrl = new YouTubeUrl("https://gdata.youtube.com/feeds/api/videos");
            videoUrl.q = videoId;
            videoUrl.maxResults = 1;
            request = factory.buildGetRequest(videoUrl);
            feed = request.execute().parseAs(MyFeed.class);
            if (feed.items != null) {                
                Video video = feed.items.get(0);                
                results.put("title", video.title);
                results.put("description", video.description);
                results.put("imageUrl", video.thumbnail.sqDefault);
            }
        } catch (Exception e) {
            NnLogUtil.logException(e);
        }
        return results;
    }
    
    //return key "status", "title", "thumbnail", "description"
    //TODO boolean channel removed, can easily tell by format
    public static Map<String, String> getYouTubeEntry(String userIdStr, boolean channel) {        
        Map<String, String> results = new HashMap<String, String>();
        results.put("status", String.valueOf(NnStatusCode.SUCCESS));
        String url = "";
        //https://gdata.youtube.com/feeds/api/playlists/nSXHekhWES_OhBZcFPWQ1f5q-BKHXx-O?v=2&alt=json
        //http://gdata.youtube.com/feeds/api/users/crashcourse?alt=json&v=2    
        if (channel)
           url = "http://gdata.youtube.com/feeds/api/users/" + userIdStr;
        else
           url = "https://gdata.youtube.com/feeds/api/playlists/" + userIdStr;

        url = url + "?v=2&alt=json";
        log.info("url:" + url);
        String jsonStr = NnNetUtil.urlGet(url);
        JSONObject json = new JSONObject(jsonStr);
        String title, description, thumbnail, author, total;
        title = description = thumbnail = author = total = "";
        try {
           if (channel) {
               title = json.getJSONObject("entry").getJSONObject("title").get("$t").toString();
               description = json.getJSONObject("entry").getJSONObject("summary").get("$t").toString();
               thumbnail = json.getJSONObject("entry").getJSONObject("media$thumbnail").get("url").toString();
               author = json.getJSONObject("entry").getJSONArray("author").getJSONObject(0).getJSONObject("name").get("$t").toString();
           } else {
               title = json.getJSONObject("feed").getJSONObject("title").get("$t").toString();
               description = json.getJSONObject("feed").getJSONObject("media$group").getJSONObject("media$description").get("$t").toString();
               thumbnail = json.getJSONObject("feed").getJSONObject("media$group").getJSONArray("media$thumbnail").getJSONObject(0).get("url").toString();
               author = json.getJSONObject("feed").getJSONArray("author").getJSONObject(0).getJSONObject("name").get("$t").toString();
               total = json.getJSONObject("feed").getJSONObject("openSearch$totalResults").get("$t").toString();
               results.put("totalItems", total);
           }
           results.put("title", title);
           results.put("description", description);
           results.put("thumbnail", thumbnail);
           results.put("author", author);
           results.put("total", total);
        } catch (JSONException e){
           e.printStackTrace();               
        }
        return results;        
    }
            
    public static String getYouTubeChannelName(String urlStr) {
        String channelUrl = "http://www.youtube.com/user/";
        String playListUrl = "http://www.youtube.com/view_play_list?p=";
        String name = urlStr.substring(channelUrl.length(), urlStr.length());        
        if (urlStr.contains("view_play_list")) {
            name = urlStr.substring(playListUrl.length(), urlStr.length()); 
        }
        return name;
    }

    /**
     * YouTube API request format, http://gdata.youtube.com/feeds/api/users/androidcentral
     * This function currently checks only if the query status is not 200.
     * 
     * @@@ IMPORTANT: This code will be blocked by YouTube, need to add user's IP, indicating you are on behalf of the user.
     * 
     * @param urlStr support only format of http://www.youtube.com/user/android  
     */
    public static boolean youTubeCheck(String urlStr) {        
        String[] splits = urlStr.split("/");
        String apiReq = "http://gdata.youtube.com/feeds/api/users/" + splits[splits.length-1];
        URL url;
        try {
            //HTTP GET
            url = new URL(apiReq);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                log.info("yutube GET response not ok with url:" + urlStr + "; status code = " + connection.getResponseCode());
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    public String normalizeUrl(String urlStr) {
        
        if (urlStr == null) { return null; }
        
        Matcher matcher = Pattern.compile(REGEX_VIDEO_URL).matcher(urlStr);
        if (matcher.find()) {
            
            return "https://www.youtube.com/watch?v=" + matcher.group(1);
        }
        
        return null;
    }
    
    public boolean isUrlMatched(String urlStr) {
        
        if (urlStr == null) { return false; }
        
        return urlStr.matches(REGEX_VIDEO_URL);
    }
    
    public String getVideoId(String urlStr) {
        
        if (urlStr == null || urlStr.length() == 0) {
            return urlStr;
        }
        
        if (!isUrlMatched(urlStr)) {
            return null;
        }
        
        Pattern pattern = Pattern.compile(REGEX_VIDEO_ID_STR);
        Matcher matcher = pattern.matcher(urlStr);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    static YouTubeService getYTService() {
        
        return new YouTubeService("FLIPr.tv");
    }
    
    public static PlaylistFeed getPlaylistFeed(String playlistId) throws MalformedURLException, IOException, ServiceException {
        
        if (playlistId == null) { return null; }
        YouTubeService service = getYTService();
        
        return service.getFeed(new URL("https://gdata.youtube.com/feeds/api/playlists/" + playlistId), PlaylistFeed.class);
    }
    
    public String getHtml5DirectVideoUrl(String urlStr) {
        
        return getDirectVideoUrl(urlStr);
    }
    
    public String getDirectVideoUrl(String urlStr) {
        
        if (urlStr == null) { return null; }
        
        String cmd = "/usr/bin/youtube-dl -g -v --no-cache-dir "
                   + NnStringUtil.escapeURLInShellArg(urlStr);
        log.info("[exec] " + cmd);
        
        try {
            
            Process process = Runtime.getRuntime().exec(cmd);
            // piping error message to stdout
            PipingTask pipingTask = new PipingTask(process.getErrorStream(), System.out, 0);
            pipingTask.start();
            
            InputStream dataIn = process.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(dataIn, baos);
            
            if (baos.size() > 0) {
                
                String rawUrl = baos.toString(NnStringUtil.UTF8);
                log.info(rawUrl);
                if (rawUrl != null && !rawUrl.isEmpty()) {
                    
                    return rawUrl.trim();
                }
            }
            
        } catch (IOException e) {
            
            log.warning(e.getMessage());
            return null;
        }
        
        return null;
    }
    
    public static InputStream youtubeDL(String urlStr) {
        
        if (urlStr == null) { return null; }
        
        String cmd = "/usr/bin/youtube-dl -v --no-cache-dir -o - "
                   + NnStringUtil.escapeURLInShellArg(urlStr);
        log.info("[exec] " + cmd);
        
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            // piping error message to stdout
            PipingTask pipingTask = new PipingTask(process.getErrorStream(), System.out, 0);
            pipingTask.start();
            
            return process.getInputStream();
            
        } catch (IOException e) {
            
            log.warning(e.getMessage());
            return null;
        }
    }
    
    public InputStream getDirectVideoStream(String urlStr) {
        
        return youtubeDL(urlStr);
    }
//        public static Map<String, String> getYouTubeEntry(String userIdStr, boolean channel) {        

    //https://gdata.youtube.com/feeds/api/playlists/nSXHekhWES_OhBZcFPWQ1f5q-BKHXx-O?v=2&alt=json
    //http://gdata.youtube.com/feeds/api/users/crashcourse?alt=json&v=2
    public static Map<String, String> test(String userIdStr, boolean channel) {       
        Map<String, String> results = new HashMap<String, String>();
        results.put("status", String.valueOf(NnStatusCode.SUCCESS));
        String url = "";
        if (channel)
           url = "http://gdata.youtube.com/feeds/api/users/" + userIdStr;
        else
           url = "https://gdata.youtube.com/feeds/api/playlists/" + userIdStr;

        url = url + "?v=2&alt=json";
        log.info("url:" + url);
        String jsonStr = NnNetUtil.urlGet(url);
        if (jsonStr == null) {
           results.put("status", String.valueOf(NnStatusCode.SERVER_ERROR));
           return results;
        }
        JSONObject json = new JSONObject(jsonStr);
        String title, description, thumbnail, author, total;
        title = description = thumbnail = author = total = "";
        try {
           if (channel) {
               title = json.getJSONObject("entry").getJSONObject("title").get("$t").toString();
               description = json.getJSONObject("entry").getJSONObject("summary").get("$t").toString();
               thumbnail = json.getJSONObject("entry").getJSONObject("media$thumbnail").get("url").toString();
               author = json.getJSONObject("entry").getJSONArray("author").getJSONObject(0).getJSONObject("name").get("$t").toString();
           } else {
               title = json.getJSONObject("feed").getJSONObject("title").get("$t").toString();
               description = json.getJSONObject("feed").getJSONObject("media$group").getJSONObject("media$description").get("$t").toString();
               thumbnail = json.getJSONObject("feed").getJSONObject("media$group").getJSONArray("media$thumbnail").getJSONObject(0).get("url").toString();
               author = json.getJSONObject("feed").getJSONArray("author").getJSONObject(0).getJSONObject("name").get("$t").toString();
               total = json.getJSONObject("feed").getJSONObject("openSearch$totalResults").get("$t").toString();
               results.put("totalItems", total);
           }
           results.put("title", title);
           results.put("description", description);
           results.put("thumbnail", thumbnail);
           results.put("author", author);
           results.put("total", total);
        } catch (JSONException e){
           e.printStackTrace();               
        }
        return results;        
    }
          
}

