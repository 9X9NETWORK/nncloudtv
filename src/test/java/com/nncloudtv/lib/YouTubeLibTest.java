package com.nncloudtv.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.nncloudtv.lib.stream.YouTubeLib;
import com.nncloudtv.lib.stream.YouTubeLib.MyFeed;
import com.nncloudtv.lib.stream.YouTubeLib.Thumbnail;
import com.nncloudtv.lib.stream.YouTubeLib.Video;
import com.nncloudtv.lib.stream.YouTubeLib.VideoFeed;
import com.nncloudtv.lib.stream.YouTubeLib.YouTubeUrl;
import com.nncloudtv.support.NnTestAll;
import com.nncloudtv.web.api.NnStatusCode;

@RunWith(PowerMockRunner.class)
@PrepareForTest({YouTubeLib.class, URL.class, HttpRequestFactory.class, HttpRequest.class, HttpResponse.class})
@Category(NnTestAll.class)
public class YouTubeLibTest {
    
    protected static final Logger log = Logger.getLogger(YouTubeLibTest.class.getName());
    
    @Test
    public void testFormatCheck() {
        
        String errorMsg = "Transform error when transform to YouTube channel.";
        String expected = "http://www.youtube.com/user/userid";
        String[] validChannels = {
                "http://www.youtube.com/userid",
                "http://www.youtube.com/user/userid",
                "http://www.youtube.com/profile?user=userid",
                "http://www.youtube.com/profile?user=userid#g/u",
                "http://www.youtube.com/userid#g/a",
                "http://www.youtube.com/user/userid#g/p"
        };
        for (String validUrl : validChannels) {
            assertEquals(errorMsg, expected, YouTubeLib.formatCheck(validUrl));
        }
        
        errorMsg = "Transform error when transform to YouTube playlist.";
        expected = "http://www.youtube.com/view_play_list?p=1A2B";
        String[] validPlaylists = {
                "http://www.youtube.com/view_play_list?p=1A2B",
                "http://www.youtube.com/playlist?list=PL1A2B",
                "http://www.youtube.com/user/UCBerkeley#p/c/1A2B",
                "http://www.youtube.com/user/UCBerkeley#g/c/1A2B",
                "http://www.youtube.com/watch?v=-dQltKG3NlI&p=1A2B",
                "http://www.youtube.com/user/UCBerkeley#p/c/1A2B/0/-dQltKG3NlI",
                "http://www.youtube.com/profile?user=UCBerkeley#grid/user/1A2B",
                "http://www.youtube.com/watch?v=-dQltKG3NlI&playnext=1&list=1A2B"
        };
        for (String validUrl : validPlaylists) {
            assertEquals(errorMsg, expected, YouTubeLib.formatCheck(validUrl));
        }
        
        expected = "http://www.youtube.com/view_play_list?p=03D59E2ECDDA66DF";
        String[] validPlaylists2 = {
                "http://www.youtube.com/playlist?list=PL03D59E2ECDDA66DF",
                "http://www.youtube.com/view_play_list?p=03D59E2ECDDA66DF",
                "http://www.youtube.com/user/UCBerkeley#p/c/03D59E2ECDDA66DF",
                "http://www.youtube.com/user/UCBerkeley#g/c/03D59E2ECDDA66DF",
                "http://www.youtube.com/watch?v=-dQltKG3NlI&p=03D59E2ECDDA66DF",
                "http://www.youtube.com/user/UCBerkeley#p/c/03D59E2ECDDA66DF/0/-dQltKG3NlI",
                "http://www.youtube.com/profile?user=UCBerkeley#grid/user/03D59E2ECDDA66DF",
                "http://www.youtube.com/watch?v=-dQltKG3NlI&playnext=1&list=PL03D59E2ECDDA66DF",
                "http://www.youtube.com/watch?v=-dQltKG3NlI&playnext=1&list=PL03D59E2ECDDA66DF&feature=list_related"
        };
        for (String validUrl : validPlaylists2) {
            assertEquals(errorMsg, expected, YouTubeLib.formatCheck(validUrl));
        }
        
        String[] invalids = {"index", "videos",
                "entertainment", "music", "news", "movies",
                "comedy", "gaming", "sports", "education",
                "shows", 
                "store", "channels", "contests_main"};
        for (String invalid : invalids) {
            assertNull("'" + invalid + "' is invalid user name.",
                    YouTubeLib.formatCheck("http://www.youtube.com/user/" + invalid));
        }
        
        String specialInvalid = "http://www.youtube.com/user/watch";
        assertNull("'" + specialInvalid + "' is not valid url.", YouTubeLib.formatCheck(specialInvalid));
    }
    
    @Test
    public void testGetYouTubeVideo() {
        
        // input arguments
        final String videoId = "videoId";
        
        // mock data
        YouTubeUrl videoUrl = new YouTubeUrl("https://gdata.youtube.com/feeds/api/videos");
        videoUrl.q = videoId;
        videoUrl.maxResults = 1;
        
        MyFeed feed = new YouTubeLib.MyFeed();
        Video video = new Video();
        
        String videoTitle = "videoTitle";
        String videoDescription = "videoDescription";
        
        video.title = videoTitle;
        video.description = videoDescription;
        
        Thumbnail thumbnail = new Thumbnail();
        
        String sqDefault = "sqDefault";
        thumbnail.sqDefault = sqDefault;
        
        video.thumbnail = thumbnail;
        feed.items = new ArrayList<Video>();
        feed.items.add(video);
        
        // mock object
        HttpRequestFactory factory = PowerMockito.mock(HttpRequestFactory.class);
        HttpRequest request = PowerMockito.mock(HttpRequest.class);
        HttpResponse response = PowerMockito.mock(HttpResponse.class);
        
        // stubs
        PowerMockito.spy(YouTubeLib.class);
        when(YouTubeLib.getFactory()).thenReturn(factory);
        try {
            when(factory.buildGetRequest(videoUrl)).thenReturn(request);
            when(request.execute()).thenReturn(response);
            when(response.parseAs(MyFeed.class)).thenReturn(feed);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // execute
        Map<String, String> actual = YouTubeLib.getYouTubeVideo(videoId);
        
        // verify
        PowerMockito.verifyStatic();
        YouTubeLib.getFactory();
        try {
            verify(factory).buildGetRequest(videoUrl);
            verify(request).execute();
            verify(response).parseAs(MyFeed.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("title", videoTitle);
        expected.put("description", videoDescription);
        expected.put("imageUrl", sqDefault);
        
        assertEquals("Wrong format.", expected, actual);
    }
    
    @Test
    public void testGetYouTubeEntryPlaylist() {
        
        // input arguments
        final String userIdStr = "03D59E2ECDDA66DF";
        final boolean channel = false;
        
        // mock data
        VideoFeed feed = new YouTubeLib.VideoFeed();
        
        int totalItems = 1;
        String feedTitle = "feedTitle";
        String feedAuthor = "feedAuthor";
        String feedDescription = "feedDescription";
        
        feed.totalItems = totalItems;
        feed.title = feedTitle;
        feed.author = feedAuthor;
        feed.description = feedDescription;
        
        Video video = new Video();
        
        String videoTitle = "videoTitle";
        String videoDescription = "videoDescription";
        
        video.title = videoTitle;
        video.description = videoDescription;
        
        Video innerVideo = new Video();
        
        Thumbnail thumbnail = new Thumbnail();
        
        String sqDefault = "sqDefault";
        thumbnail.sqDefault = sqDefault;
        
        innerVideo.thumbnail = thumbnail;
        video.video = innerVideo;
        feed.items = new ArrayList<Video>();
        feed.items.add(video);
        
        
        YouTubeUrl videoUrl = new YouTubeUrl("https://gdata.youtube.com/feeds/api/playlists/" + userIdStr);
        videoUrl.maxResults = 1;
        
        // mock object
        HttpRequestFactory factory = PowerMockito.mock(HttpRequestFactory.class);
        HttpRequest request = PowerMockito.mock(HttpRequest.class);
        HttpResponse response = PowerMockito.mock(HttpResponse.class);
        
        // stubs
        PowerMockito.spy(YouTubeLib.class);
        when(YouTubeLib.getFactory()).thenReturn(factory);
        try {
            when(factory.buildGetRequest(videoUrl)).thenReturn(request);
            when(request.execute()).thenReturn(response);
            when(response.parseAs(VideoFeed.class)).thenReturn(feed);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // execute
        Map<String, String> actual = YouTubeLib.getYouTubeEntry(userIdStr, channel);
        
        // verify
        PowerMockito.verifyStatic();
        YouTubeLib.getFactory();
        try {
            verify(factory).buildGetRequest(videoUrl);
            verify(request).execute();
            verify(response).parseAs(VideoFeed.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("status", String.valueOf(NnStatusCode.SUCCESS));
        expected.put("totalItems", String.valueOf(totalItems));
        expected.put("thumbnail", sqDefault);
        expected.put("title", feedTitle);
        expected.put("author", feedAuthor);
        expected.put("description", feedDescription);
        
        assertEquals("Wrong format.", expected, actual);
    }
    
    @Test
    public void testGetYouTubeEntryUserChannel() {
        
        // input arguments
        final String userIdStr = "test123";
        final boolean channel = true;
        
        // mock data
        GenericUrl profileUrl = new GenericUrl("http://gdata.youtube.com/feeds/api/users/" + userIdStr);
        YouTubeUrl videoUrl = new YouTubeUrl("https://gdata.youtube.com/feeds/api/videos");
        videoUrl.author = userIdStr;
        videoUrl.maxResults = 1;
        
        String profileThumbnail = "profileThumbnail";
        String profileFeed = "what ever media:thumbnail url='" + profileThumbnail + "'/> what ever";
        
        VideoFeed videoFeed = new YouTubeLib.VideoFeed();
        
        int totalItems = 1;
        String feedTitle = "feedTitle";
        String feedAuthor = "feedAuthor";
        String feedDescription = "feedDescription";
        
        videoFeed.totalItems = totalItems;
        videoFeed.title = feedTitle;
        videoFeed.author = feedAuthor;
        videoFeed.description = feedDescription;
        
        Video video = new Video();
        
        String videoTitle = "videoTitle";
        String videoDescription = "videoDescription";
        
        video.title = videoTitle;
        video.description = videoDescription;
        
        Video innerVideo = new Video();
        
        Thumbnail thumbnail = new Thumbnail();
        
        String sqDefault = "sqDefault";
        thumbnail.sqDefault = sqDefault;
        
        innerVideo.thumbnail = thumbnail;
        video.video = innerVideo;
        videoFeed.items = new ArrayList<Video>();
        videoFeed.items.add(video);
        
        // mock object
        HttpRequestFactory factory = PowerMockito.mock(HttpRequestFactory.class);
        HttpRequest request = PowerMockito.mock(HttpRequest.class);
        HttpRequest request2 = PowerMockito.mock(HttpRequest.class);
        HttpResponse response = PowerMockito.mock(HttpResponse.class);
        HttpResponse response2 = PowerMockito.mock(HttpResponse.class);
        
        // stubs
        PowerMockito.spy(YouTubeLib.class);
        when(YouTubeLib.getFactory()).thenReturn(factory);
        try {
            when(factory.buildGetRequest(profileUrl)).thenReturn(request);
            when(request.execute()).thenReturn(response);
            when(response.parseAsString()).thenReturn(profileFeed);
            
            when(factory.buildGetRequest(videoUrl)).thenReturn(request2);
            when(request2.execute()).thenReturn(response2);
            when(response2.parseAs(VideoFeed.class)).thenReturn(videoFeed);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // execute
        Map<String, String> actual = YouTubeLib.getYouTubeEntry(userIdStr, channel);
        
        // verify
        PowerMockito.verifyStatic();
        YouTubeLib.getFactory();
        try {
            verify(factory).buildGetRequest(profileUrl);
            verify(request).execute();
            verify(response).parseAsString();
            
            verify(factory).buildGetRequest(videoUrl);
            verify(request2).execute();
            verify(response2).parseAs(VideoFeed.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("status", String.valueOf(NnStatusCode.SUCCESS));
        expected.put("thumbnail", profileThumbnail);
        expected.put("author", userIdStr);
        expected.put("totalItems", String.valueOf(totalItems));
        expected.put("title", videoTitle);
        expected.put("description", videoDescription);
        
        assertEquals("Wrong format.", expected, actual);
    }
    
    @Test
    public void testGetYouTubeChannelName() {
        
        String userName = "test123";
        String youtubeChannelPrefix = "http://www.youtube.com/user/";
        assertEquals("Url format must be " + youtubeChannelPrefix + "<userId> .",
                userName, YouTubeLib.getYouTubeChannelName(youtubeChannelPrefix + userName));
        
        String playListId = "03D59E2ECDDA66DF";
        String youtubePlaylistPrefix = "http://www.youtube.com/view_play_list?p=";
        assertEquals("Url format must be " + youtubePlaylistPrefix + "<playlistId> .",
                playListId, YouTubeLib.getYouTubeChannelName(youtubePlaylistPrefix + playListId));
    }
    
    @Test
    public void testYouTubeCheck() {
        
        // input arguments
        final String urlStr = "http://www.youtube.com/user/test123";
        
        // mock object
        URL url = PowerMockito.mock(URL.class);
        HttpURLConnection connection =  Mockito.mock(HttpURLConnection.class);
        
        // stubs
        try {
            PowerMockito.whenNew(URL.class).withArguments(anyString()).thenReturn(url);
            when(url.openConnection()).thenReturn(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // stubs & execute & verify
        try {
            when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
            assertTrue("Response code : " + HttpURLConnection.HTTP_OK + " should return true.",
                    YouTubeLib.youTubeCheck(urlStr));
            int[] respCodes = {
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    HttpURLConnection.HTTP_FORBIDDEN,
                    HttpURLConnection.HTTP_NOT_FOUND,
                    HttpURLConnection.HTTP_INTERNAL_ERROR
            };
            for (int respCode : respCodes) {
                when(connection.getResponseCode()).thenReturn(respCode);
                assertFalse("Response code : " + respCode + " should return false.",
                        YouTubeLib.youTubeCheck(urlStr));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testIsVideoUrlNormalized() {
        
        assertTrue("Wrong format.", (new YouTubeLib()).isUrlMatched("http://www.youtube.com/watch?v=-dQltKG3NlI"));
    }
    
    @Test
    public void testGetYouTubeVideoIdStr() {
        
        final String videoIdStr = "-dQltKG3NlI";
        final String videoUrlPrefix = "http://www.youtube.com/watch?v=";
        
        assertEquals("Wrong format.", videoIdStr, (new YouTubeLib()).getVideoId(videoUrlPrefix + videoIdStr));
    }
}
