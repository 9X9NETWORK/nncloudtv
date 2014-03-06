package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.nncloudtv.dao.YtChannelDao;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.YouTubeLib;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.YtChannel;
import com.nncloudtv.web.json.player.ChannelLineup;

public class YtChannelManager {
    protected static final Logger log = Logger.getLogger(YtChannelManager.class.getName());
    
    private YtChannelDao dao = new YtChannelDao();
    
	public long getId(String id) {
		id = id.replace("yt", "");
		return Long.valueOf(id);
	}
    
	public NnChannel convert(String ytId, HttpServletRequest req) {
		YtChannel yt = this.findById(ytId);
		NnChannelManager chMngr = new NnChannelManager();
		NnChannel c = chMngr.findBySourceUrl(yt.getSourceUrl());
		if (c != null) {
			log.info("pool channel already existing in nnchannel. ytId:" + yt.getId() + ";nnId:" + c.getId());
			return c;
		}
        c = chMngr.create(yt.getSourceUrl(), null, yt.getSphere(), req);		
		if (c != null) {
	        c.setStatus(NnChannel.STATUS_SUCCESS);
	        c.setPublic(true);
	        chMngr.save(c);
			log.info("create from pool. ytId:" + yt.getId() + "; new nnId:" + c.getId());
		}	
		return c;
	}
	
    public YtChannel findById(String strId) {
    	long id = this.getId(strId);
    	YtChannel channel = dao.findById(id);
    	return channel;
    }
    
    public Object findRecommend(String userToken, String lang, int version, short format) {
        List<YtChannel> channels = this.findRandomChannels(userToken, lang);
        List<ChannelLineup> objs = new ArrayList<ChannelLineup>();
        String output = "";
        for (YtChannel c : channels) {
            Object o = this.composeEachChannelLineup(c, version, format);
            if (format == PlayerApiService.FORMAT_JSON)
                objs.add((ChannelLineup)o);
            else
                output += (String) o + "\n";
        }        
        if (format == PlayerApiService.FORMAT_JSON) {
            return objs;
        } else {
            List<String> result = new ArrayList<String>();
            result.add(output);
	        String size[] = new String[result.size()];	                    
	        return result.toArray(size);
        }
    }
    
    public List<YtChannel> findRandomChannels(String userToken, String lang) {
        return dao.findRandomTen(lang);
    }    
    
    public Object composeEachChannelLineup(YtChannel c, int version, short format) {
        Object result = null;
        
        String id = "yt" + c.getId();        
        String cacheKey = CacheFactory.getChannelLineupKey(id, version, format);
        try {
            result = CacheFactory.get(cacheKey);
        } catch (Exception e) {
            log.info("memcache error");
        }
        if (result != null && c.getId() != 0) { //id = 0 means fake channel, it is dynamic
            log.info("get ytchannel lineup from cache" + ". v=" + version +";channel=" + c.getId());
            return result;
        }
       
        log.info("ytchannel lineup NOT from cache:" + c.getId());        
        String name = c.getName() == null ? "" : c.getName();
        String intro = c.getIntro();
        String imageUrl = c.getImageUrl();
        String cntEpisode = String.valueOf(c.getCntEpisode());
        short type = 1;       
        short status = c.getStatus();
        short contentType = c.getContentType();
        String poiStr = "";
        String prefSource = YouTubeLib.getYouTubeChannelName(c.getSourceUrl());
        short sorting = NnChannel.SORT_NEWEST_TO_OLDEST;
        long updateTime = c.getUpdateDate().getTime();
        String tag = c.getTag();
        if (format == PlayerApiService.FORMAT_PLAIN) {
            List<String> ori = new ArrayList<String>();
            ori.add("0");
            ori.add(id);
            ori.add(name);
            ori.add(intro);
            ori.add(imageUrl);                 
            ori.add(cntEpisode);
            ori.add(String.valueOf(type));
            ori.add(String.valueOf(status));
            ori.add(String.valueOf(contentType));
            ori.add(prefSource);
            ori.add(String.valueOf(updateTime));
            ori.add(String.valueOf(sorting)); //use default sorting for all
            ori.add(""); //piwik
            ori.add(""); //recently watched program
            ori.add(name); //original name
            ori.add("0"); //cnt subscribe, replace
            ori.add("0");
            ori.add(tag);
            ori.add(""); //ciratorProfile, curator id
            ori.add(""); //userName
            ori.add(""); //userIntro
            ori.add(""); //userImageUrl
            ori.add(""); //subscriberProfile, used to be subscriber profile urls, will be removed
            ori.add(""); //subscriberImage, used to be subscriber image urls
            if (version == 32)
                ori.add(" ");
            else
                ori.add(""); //lastEpisodeTitle
            if (version > 32)
                ori.add(poiStr);
            String size[] = new String[ori.size()];    
            String output = NnStringUtil.getDelimitedStr(ori.toArray(size));
            output = output.replaceAll("null", "");
            log.info("set channelLineup cahce for cacheKey:" + cacheKey);
            CacheFactory.set(cacheKey, output);            
            return output;
        } else {
            ChannelLineup lineup = new ChannelLineup();
            lineup.setPosition((short)0);
            lineup.setId(c.getId());
            lineup.setName(name);
            lineup.setDescription(intro);
            lineup.setThumbnail(imageUrl); //c.getPlayerPrefImageUrl());                        
            lineup.setNumberOfEpisode(c.getCntEpisode());
            lineup.setType(type);
            lineup.setStatus(c.getStatus());
            lineup.setContentType(c.getContentType());
            lineup.setChannelSource(prefSource);
            lineup.setLastUpdateTime(updateTime);
            lineup.setSorting(sorting); //use default sorting for all
            lineup.setPiwikId("");
            lineup.setRecentlyWatchedPrograms(""); //recently watched program
            lineup.setYoutubeName(name);
            lineup.setNumberOfSubscribers(0); //cnt subscribe, replace
            lineup.setNumberOfViews(0);
            lineup.setTags(c.getTag());
            lineup.setCuratorProfile(""); //ciratorProfile, curator id
            lineup.setCuratorName(""); //userName
            lineup.setCuratorDescription(""); //userIntro
            lineup.setCuratorThumbnail(""); //userImageUrl
            lineup.setSubscriberProfiles(""); //subscriberProfile, used to be subscriber profile urls, will be removed
            lineup.setSubscriberThumbnails(""); //subscriberImage, used to be subscriber image urls                
            log.info("set channelLineup cahce for cacheKey:" + cacheKey);
            CacheFactory.set(cacheKey, lineup);
            return lineup;
        }
    }

}
