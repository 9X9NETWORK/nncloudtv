package com.nncloudtv.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.YouTubeLib;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.web.api.NnStatusCode;

@Service
public class IosService {
    
    protected static final Logger log = Logger.getLogger(IosService.class.getName());
    private static final String urlRoot = "http://s3.amazonaws.com/9x9ui/war/v2/ios/";
    
    public String category(String id, String lang, boolean flatten, Mso mso) {
        if (mso.getId() == 1) {
            String filename = "category_en";
            if (lang != null && lang.equals(LangTable.LANG_ZH)) {
                filename = "category_zh";
            }
            if (id != null) {
                filename = "category_" + id;
                if (id.contains("s"))
                    filename = id;
            }
            log.info("file mode:" + filename);
            String url = urlRoot + filename;
            String result = NnNetUtil.urlGet(url);            
            if (result == null)
                return (String) new PlayerApiService().assembleMsgs(NnStatusCode.INPUT_BAD, null);
            else
                return result;
        }
        PlayerApiService api = new PlayerApiService();
        lang = api.checkLang(lang);    
        if (lang == null) {
            return (String) api.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        }
        if (id == null) {
            id = "0";
        }        
        String[] result = {"", "", ""};
        //if it's a set, find channel info
        result[0] = "id" + "\t" + id + "\n";
        if (!id.equals("0")) {            
            long tagId = Long.parseLong(id);
            SysTag tag = NNF.getSysTagMngr().findById(tagId);
            if (tag != null) {
                result[0] += "piwik" + "\t" + "" + "\n";
            }
            List<NnChannel> channels = NNF.getSysTagMngr().findPlayerChannelsById(tagId, lang, SysTag.SORT_SEQ, 0);
            for (NnChannel c : channels) {
                c.setSorting(NnChannelManager.getPlayerDefaultSorting(c));
            }
            result[2] = this.composeChannelLineup(channels);
            return (String) api.assembleMsgs(NnStatusCode.SUCCESS, result);
        }        
        
        List<SysTagDisplay> categories = NNF.getDisplayMngr().findPlayerCategories(lang, mso.getId());
        //if it's just categories, find categories
        for (SysTagDisplay c : categories) { 
            String name =  c.getName();
            int cnt = c.getCntChannel();
            String subItemHint = "ch"; //what's under this level
            String[] str = {String.valueOf(c.getId()), 
                            name, 
                            String.valueOf(cnt), 
                            subItemHint};               
            result[1] += NnStringUtil.getDelimitedStr(str) + "\n";
        }
        return (String) api.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public String composeChannelLineup(List<NnChannel> channels) {
        String result = "";
        for (NnChannel c : channels) {
            String name = c.getPlayerName();
            if (name != null)
                name = name.replaceAll("\n", " ").replaceAll("\t", " ");
            else {
                name = "";
            }
            String[] split = name.split("\\|");
            name = split.length == 2 ? split[0] : name;            
            String intro = c.getPlayerIntro();        
            if (intro != null)
                intro = intro.replaceAll("\n", " ").replaceAll("\t", " ");
            String imageUrl = c.getPlayerPrefImageUrl();
            log.info("ios: imageUrl:" + imageUrl);
            imageUrl = imageUrl.indexOf("|") < 0 ? imageUrl : imageUrl.substring(0, imageUrl.indexOf("|"));
            log.info("ios: after imageUrl:" + imageUrl);
            String youtubeId = "";
            if (c.getSourceUrl() != null && c.getSourceUrl().contains("http://www.youtube.com"))
                youtubeId = YouTubeLib.getYouTubeChannelName(c.getSourceUrl());
            if (c.getContentType() == NnChannel.CONTENTTYPE_FACEBOOK) 
                youtubeId = c.getSourceUrl();
            String[] ori = {Integer.toString(c.getSeq()),                         
                            String.valueOf(c.getId()),
                            name,
                            intro,
                            imageUrl,
                            String.valueOf(c.getCntEpisode()),
                            String.valueOf(c.getType()),
                            String.valueOf(c.getStatus()),
                            String.valueOf(c.getContentType()),
                            youtubeId,
                            String.valueOf(c.getUpdateDate().getTime()),
                            String.valueOf(c.getSorting()),
                            "", // piwik
                            String.valueOf(c.getRecentlyWatchedProgram()),
                            c.getOriName(),
                            String.valueOf(c.getCntSubscribe()),
                            };
            result += NnStringUtil.getDelimitedStr(ori) + "\n";
        }
        return result;
    }

    private String composeLimitProgramInfoStr(String input, long sidx, long limit) {
        if (sidx == 0 && limit == 0)
            return input;
        String[] lines = input.split("\n");
        String result = "";
        long start = sidx - 1;
        long end = start + limit;
        for (int i=0; i<lines.length; i++) {
            if (i>=start && i<end) {
                result += lines[i] + "\n";
            }
            if (i > end) {
                return result;
            }
        }        
        return result;
    }

    public String findPlayerProgramInfoByChannel(long channelId, long sidx, long limit) {
        String result = this.findPlayerProgramInfoByChannel(channelId);
        return this.composeLimitProgramInfoStr(result, sidx, limit);
    }    
    
    public String findPlayerProgramInfoByChannel(long channelId) {
        log.info("request from != v32");
        String result = null;
        try {
            result = (String) CacheFactory.get(CacheFactory.getProgramInfoKey(channelId, 0, 31, PlayerApiService.FORMAT_PLAIN));
        } catch (Exception e) {
            log.info("memcache error");
        }
        if (result != null && channelId != 0) { //id = 0 means fake channel, it is dynamic
            log.info("get programInfo from v31 cache");
            return result;
        }
        List<NnProgram> programs = NNF.getProgramMngr().findPlayerProgramsByChannel(channelId);
        log.info("retrieve v31 from db, channel id:" + channelId + "; program size:" + programs.size());
        String str = this.composeProgramInfoStr(programs);
        CacheFactory.set(CacheFactory.getProgramInfoKey(channelId, 0, 31, PlayerApiService.FORMAT_PLAIN), str);
        return str;
    }    
    
	public String search(String text) {
		List<NnChannel> searchResults = NnChannelManager.search(text, null, null, false, 1, 9);
		String[] result = {""};
		result[0] = this.composeChannelLineup(searchResults);
		return (String) new PlayerApiService().assembleMsgs(NnStatusCode.SUCCESS, result);
	}
    
    public String composeProgramInfoStr(List<NnProgram> programs) {        
        String output = "";        
        String regexCache = "^(http|https)://(9x9cache.s3.amazonaws.com|s3.amazonaws.com/9x9cache)";
        String regexPod = "^(http|https)://(9x9pod.s3.amazonaws.com|s3.amazonaws.com/9x9pod)";
        String cache = "http://cache.9x9.tv";
        String pod = "http://pod.9x9.tv";
        for (NnProgram p : programs) {
            //file urls
            String url1 = p.getFileUrl();
            String url2 = ""; //not used for now
            String url3 = ""; //not used for now
            String url4 = p.getAudioFileUrl();
            String imageUrl = p.getImageUrl();
            String imageLargeUrl = p.getImageUrl();
            if (imageUrl == null) {imageUrl = "";}
            if (imageLargeUrl == null) {imageLargeUrl = "";}
            if (url1 != null) {
                url1 = url1.replaceFirst(regexCache, cache);
                url1 = url1.replaceAll(regexPod, pod);
            }
            url2 = url2.replaceFirst(regexCache, cache);
            url2 = url2.replaceAll(regexPod, pod);
            url3 = url3.replaceFirst(regexCache, cache);
            url3 = url3.replaceAll(regexPod, pod);
            if (url4 != null) {
                url4 = url4.replaceFirst(regexCache, cache);
                url4 = url4.replaceAll(regexPod, pod);
            }
            if (imageUrl != null) {
                imageUrl = imageUrl.replaceFirst(regexCache, cache);
                imageUrl = imageUrl.replaceAll(regexPod, pod);
            }
            if (imageLargeUrl != null) {
                imageLargeUrl = imageLargeUrl.replaceFirst(regexCache, cache);
                imageLargeUrl = imageLargeUrl.replaceAll(regexPod, pod);
            }
            if (p.getUpdateDate() == null)
            	p.setUpdateDate(new Date());
            
            //the rest
            String[] ori = {String.valueOf(p.getChannelId()), 
                            String.valueOf(p.getId()), 
                            p.getPlayerName(), 
                            p.getPlayerIntro(),
                            String.valueOf(p.getContentType()), 
                            p.getDuration(),
                            imageUrl,
                            imageLargeUrl,
                            url1, 
                            url2, 
                            url3, 
                            url4,            
                            String.valueOf(p.getUpdateDate().getTime()),
                            p.getComment()};
            output = output + NnStringUtil.getDelimitedStr(ori);
            output = output.replaceAll("null", "");
            output = output + "\n";
        }
        return output;        
    }    
}
