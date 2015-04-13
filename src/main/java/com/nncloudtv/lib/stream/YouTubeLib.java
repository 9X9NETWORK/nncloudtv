package com.nncloudtv.lib.stream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelContentDetails;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistContentDetails;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.IOUtils;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.task.PipingTask;
import com.nncloudtv.web.api.NnStatusCode;

public class YouTubeLib  implements StreamLib {
    
    protected static final Logger log = Logger.getLogger(YouTubeLib.class.getName());
    
    public static final String REGEX_YOUTUBE_PLAYLIST  = "^https?:\\/\\/www\\.youtube\\.com\\/playlist\\?list=([^&]+).*$";
    public static final String REGEX_VIDEO_URL         = "^https?:\\/\\/www\\.youtube\\.com\\/watch\\?v=([^&]+).*$";
    public static final String REGEX_VIDEO_ID_STR      = "v=([^&]+)";
    public static final String YOUTUBE_CHANNEL_PREFIX  = "http://www.youtube.com/user/";
    public static final String YOUTUBE_PLAYLIST_PREFIX = "http://www.youtube.com/view_play_list?p=";
    
    public static final String SCOPE_READONLY = "https://www.googleapis.com/auth/youtube.readonly";
    
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
        return url;
    }
    
    public static Map<String, String> getYouTubeVideo(String videoId) {
        
        Map<String, String> results = new HashMap<String, String>();
        
        YouTube youtube;
        try {
            youtube = getYouTubeService();
            YouTube.Videos.List request = youtube.videos().list("snippet");
            VideoListResponse response = request.setId(videoId).execute();
            List<Video> items = response.getItems();
            if (items.size() > 0) {
                
                VideoSnippet snippet = items.get(0).getSnippet();
                results.put("title",       snippet.getTitle());
                results.put("description", snippet.getDescription());
                results.put("imageUrl",    snippet.getThumbnails().getStandard().getUrl());
            }
        } catch (GeneralSecurityException e) {
            log.warning(e.getMessage());
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
        
        return results;
    }
    
    //return key "status", "title", "thumbnail", "description"
    //TODO boolean channel removed, can easily tell by format
    public static Map<String, String> getYouTubeEntry(String userIdStr, boolean isChannel) {
        Map<String, String> results = new HashMap<String, String>();
        results.put("status", String.valueOf(NnStatusCode.SUCCESS));
        
        try {
            YouTube youtube = getYouTubeService();
            String playlistId = userIdStr;
            if (isChannel) {
                
                YouTube.Channels.List channelRequest = youtube.channels().list("contentDetails");
                if (userIdStr.startsWith("UC"))
                    channelRequest.setId(userIdStr);
                else
                    channelRequest.setForUsername(userIdStr);
                ChannelListResponse channelResponse = channelRequest.execute();
                List<Channel> items = channelResponse.getItems();
                if (items == null || items.isEmpty()) {
                    log.warning("no channel exists");
                    results.put("status", String.valueOf(NnStatusCode.SERVER_ERROR));
                    return results;
                }
                ChannelContentDetails contentDetails = items.get(0).getContentDetails();
                playlistId = contentDetails.getRelatedPlaylists().getUploads();
            }
            
            YouTube.Playlists.List request = youtube.playlists().list("contentDetails,snippet");
            log.info("playlistId = " + playlistId);
            PlaylistListResponse response = request.setId(playlistId).execute();
            List<Playlist> items = response.getItems();
            if (items == null || items.isEmpty()) {
                log.warning("no playlist exists");
                results.put("status", String.valueOf(NnStatusCode.SERVER_ERROR));
                return results;
            }
            PlaylistSnippet snippet = items.get(0).getSnippet();
            PlaylistContentDetails contentDetails = items.get(0).getContentDetails();
            results.put("title", snippet.getTitle());
            results.put("description", snippet.getDescription());
            results.put("thumbnail", snippet.getThumbnails().getStandard().getUrl());
            results.put("author", snippet.getChannelTitle());
            results.put("total", String.valueOf(contentDetails.getItemCount()));
            
        } catch (GeneralSecurityException e) {
            
            log.warning(e.getMessage());
            results.put("status", String.valueOf(NnStatusCode.SERVER_ERROR));
            
        } catch (IOException e) {
            
            log.warning(e.getMessage());
            results.put("status", String.valueOf(NnStatusCode.SERVER_ERROR));
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
    
    static YouTube getYouTubeService() throws GeneralSecurityException, IOException {
        
        GoogleCredential.Builder builder = new GoogleCredential.Builder();
        
        String path = MsoConfigManager.getYouTubeP12FilePath();
        log.info("read p12 file from " + path);
        File p12 = new File(path);
        if (!p12.canRead()) {
            log.severe("can not read p12 file from " + path);
            return null;
        }
        String accountEmail = MsoConfigManager.getYouTubeAccountEmail();
        log.info("youtube account email = " + accountEmail);
        
        builder = builder.setServiceAccountPrivateKeyFromP12File(p12);
        builder = builder.setTransport(GoogleNetHttpTransport.newTrustedTransport());
        builder = builder.setJsonFactory(JacksonFactory.getDefaultInstance());
        builder = builder.setServiceAccountId(accountEmail);
        builder = builder.setServiceAccountScopes(Collections.singleton(SCOPE_READONLY));
        
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                                   JacksonFactory.getDefaultInstance(),
                                   builder.build()).setApplicationName("YouTube Data API Service").build();
    }
    
    public static List<Video> getPlaylistVideos(String playlistId) throws GeneralSecurityException, IOException {
        
        List<String> videoIdList = new ArrayList<String>();
        YouTube youtube = getYouTubeService();
        YouTube.PlaylistItems.List playlistRequest = youtube.playlistItems().list("contentDetails");
        PlaylistItemListResponse playlistResponse = playlistRequest.setPlaylistId(playlistId).setMaxResults(50L).execute();
        List<PlaylistItem> PlaylistItems = playlistResponse.getItems();
        for (PlaylistItem item : PlaylistItems)
            videoIdList.add(item.getContentDetails().getVideoId());
        
        YouTube.Videos.List videoRequest = youtube.videos().list("contentDetails,snippet");
        VideoListResponse videoResponse = videoRequest.setId(StringUtils.join(videoIdList, ",")).execute();
        
        return videoResponse.getItems();
    }
    
    public static List<PlaylistItem> getPlaylistItems(String playlistId) throws GeneralSecurityException, IOException {
        
        YouTube youtube = getYouTubeService();
        YouTube.PlaylistItems.List request = youtube.playlistItems().list("snippet");
        PlaylistItemListResponse response = request.setPlaylistId(playlistId).setMaxResults(50L).execute();
        
        return response.getItems();
    }
    
    public String getHtml5DirectVideoUrl(String urlStr) {
        
        return getYouTubeDLUrl(urlStr);
    }
    
    public String getDirectVideoUrl(String urlStr) {
        
        if (urlStr == null) return null;
        
        return getYouTubeDLUrl(urlStr);
    }
    
    public static String getYouTubeDLUrl(String urlStr) {
        
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
    
    public static InputStream getYouTubeDLStream(String urlStr) {
        
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
        
        return getYouTubeDLStream(urlStr);
    }
    
}

