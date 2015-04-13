package com.nncloudtv.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.net.URL;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.nncloudtv.lib.stream.YouTubeLib;
import com.nncloudtv.support.NnTestAll;

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
