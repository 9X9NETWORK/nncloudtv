package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnChannelDao;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.FacebookLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.SearchLib;
import com.nncloudtv.lib.YouTubeLib;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoIpg;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnChannelPref;
import com.nncloudtv.model.NnEpisode;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.model.PoiEvent;
import com.nncloudtv.model.PoiPoint;
import com.nncloudtv.model.Tag;
import com.nncloudtv.model.TagMap;
import com.nncloudtv.web.api.NnStatusCode;
import com.nncloudtv.web.json.player.ChannelLineup;

@Service
public class NnChannelManager {

    protected static final Logger log = Logger.getLogger(NnChannelManager.class.getName());
    
    private NnChannelDao dao = new NnChannelDao();
    
    public NnChannelManager() {
    }
    
    public static String convertChannelId(String channelIdStr) {
    	if (channelIdStr != null && channelIdStr.contains("yt")) {
    		channelIdStr = channelIdStr.replace("yt", "");
    	}
        try {
            Long.valueOf(channelIdStr);
            return channelIdStr;
        } catch (NumberFormatException e) {
        }            
        return null;
    }
    
    public NnChannel findById(String channelId) {
    	NnChannel c = null;
    	if (channelId.contains("yt")) {
        	c = new YtChannelManager().convert(channelId);        	
    	} else {
            c = this.findById(Long.parseLong(channelId));
    	}
    	return c;
    }
    
    public NnChannel create(String sourceUrl, String name, String lang, HttpServletRequest req) {
        if (sourceUrl == null) 
            return null;
        String url = this.verifyUrl(sourceUrl);
        log.info("valid url=" + url);
        if (url == null) 
            return null;
        
        NnChannel channel = this.findBySourceUrl(url);        
        if (channel != null) {
            log.info("submit a duplicate channel:" + channel.getId());
            return channel; 
        }
        channel = new NnChannel(url);
        channel.setContentType(this.getContentTypeByUrl(url));
        log.info("new channel contentType:" + channel.getContentType());
        if (channel.getContentType() == NnChannel.CONTENTTYPE_FACEBOOK) {
            FacebookLib lib = new FacebookLib();
            String[] info = lib.getFanpageInfo(url);
            channel.setName(info[0]);
            channel.setImageUrl(info[1]);
            channel.setStatus(NnChannel.STATUS_SUCCESS);            
        } else {
            if (channel.getContentType() == NnChannel.CONTENTTYPE_MAPLE_SOAP ||
                channel.getContentType() == NnChannel.CONTENTTYPE_MAPLE_VARIETY) {    
                channel.setImageUrl(NnChannel.IMAGE_PROCESSING_URL);
                channel.setName("Processing");
                channel.setStatus(NnChannel.STATUS_PROCESSING);
            }            
            if (channel.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL ||
                channel.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_PLAYLIST) {
                Map<String, String> info = null;
                String youtubeName = YouTubeLib.getYouTubeChannelName(url);
                if (channel.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL) {
                    info = YouTubeLib.getYouTubeEntry(youtubeName, true);
                    info.put("type", "channel"); //to create fake youtube account
                } else {
                    info = YouTubeLib.getYouTubeEntry(youtubeName, false);
                    info.put("type", "playlist"); //to create fake youtube account
                }
                if (!info.get("status").equals(String.valueOf(NnStatusCode.SUCCESS)))
                    return null;
                if (name != null)
                    channel.setName(name);
                String oriName = info.get("title");
                if (info.get("title") != null) {
                    channel.setOriName(oriName);
                    if (name == null)
                        channel.setName(oriName);
                }
                if (info.get("totalItems") != null)
                    channel.setCntEpisode(Integer.parseInt(info.get("totalItems")));
                if (info.get("description") != null)
                    channel.setIntro(info.get("description"));
                if (info.get("thumbnail") != null)
                    channel.setImageUrl(info.get("thumbnail"));
                if (info.get("author") == null) {
                    log.info("channel can't find author:" + youtubeName + ";url:" + sourceUrl);
                    channel.setPublic(false);
                }
                //NnUserManager mngr = new NnUserManager();
                //NnUser user = mngr.createFakeYoutube(info, req);
                //channel.setUserIdStr(user.getIdStr());
            }
        }
        channel.setPublic(false);
        channel.setLang(lang);
        Date now = new Date();
        channel.setCreateDate(now);
        channel.setUpdateDate(now);
        channel = this.save(channel);
        if (channel.getContentType() == NnChannel.CONTENTTYPE_MAPLE_SOAP ||
            channel.getContentType() == NnChannel.CONTENTTYPE_MAPLE_VARIETY ||
            channel.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_SPECIAL_SORTING) {
            new DepotService().submitToTranscodingService(channel.getId(), channel.getSourceUrl(), req);                                
        }
        
        // piwik
        /*
        if (channel.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL || 
            channel.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_PLAYLIST) {            
            PiwikLib.createPiwikSite(channel.getId());
        } 
        */       
        return channel;
    }

    public NnChannel createYouTubeWithMeta(String sourceUrl, String name, String intro, String lang, String imageUrl, HttpServletRequest req) {
        if (sourceUrl == null) 
            return null;
        String url = this.verifyUrl(sourceUrl);
        log.info("valid url=" + url);
        if (url == null) 
            return null;
        
        NnChannel channel = this.findBySourceUrl(url);        
        if (channel != null) {
            log.info("submit a duplicate channel:" + channel.getId());
            return channel; 
        }
        channel = new NnChannel(url);
        channel.setName(name);
        channel.setIntro(intro);
        channel.setImageUrl(imageUrl);
        channel.setContentType(this.getContentTypeByUrl(url));
        channel.setPublic(true);
        channel.setLang(lang);
        channel.setSphere(lang);
        Date now = new Date();
        channel.setCreateDate(now);
        channel.setUpdateDate(now);
        channel = this.save(channel);
        return channel;
    }
    
    //check existence is your responsibility (for now)
    //passing a good url is your responsibility (for now) 
    public NnChannel createYoutubeChannel(String url) {
        NnChannel channel = new NnChannel(url);
        channel.setStatus(NnChannel.STATUS_PROCESSING);
        channel.setContentType(NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL);
        channel.setPublic(false);
        channel.setLang(LangTable.LANG_EN);        
        Date now = new Date();
        channel.setCreateDate(now);
        channel.setUpdateDate(now);  
        this.save(channel);
        return channel;
    }
    
    //process tag text enter by users
    //TODO or move to TagManager
    public String processTagText(String channelTag) {
        String result = "";
        String[] multiples = channelTag.split(",");
        for (String m : multiples) {
            String tag = TagManager.getValidTag(m);
            if (tag != null && tag.length() > 0 && tag.length() < 20)
                result += "," + tag;
        }
        result = result.replaceFirst(",", "");
        if (result.length() == 0)
            return null;
        return result;
    }
    
    //IMPORTANT: use processTagText first 
    public void processChannelTag(NnChannel c) {
        TagManager tagMngr = new TagManager();
        List<Tag> originalTags = tagMngr.findByChannel(c.getId());
        Map<Long, String> map = new HashMap<Long, String>();
        for (Tag t : originalTags) {
            map.put(t.getId(), t.getName());
        }
        String tag = c.getTag();
        if (tag == null)
            return;
        String[] multiples = tag.split(",");
        for (String m : multiples) {
            m = m.trim();            
            Tag t = tagMngr.findByName(m);
            if (t == null) {
                t = new Tag(m);
                tagMngr.save(t);
            } else {
                map.remove(t.getId());
            }
            TagMap tm = tagMngr.findByTagAndChannel(t.getId(), c.getId());
            if (tm == null)
                tagMngr.createTagMap(t.getId(), c.getId()); 
        }
        
        Iterator<Entry<Long, String>> it = map.entrySet().iterator();      
        while (it.hasNext()) {
            Map.Entry<Long, String> pairs = (Map.Entry<Long, String>)it.next();
            if (pairs.getValue() != null && !pairs.getValue().contains("(")) {
                log.info("remove tag_map: key:" + pairs.getKey());
                tagMngr.deleteChannel(pairs.getKey(), c.getId());
            }
        }
    }
    
    public void deleteFavorite(NnUser user, long pId) {
        NnChannel favoriteCh = dao.findFavorite(user.getIdStr());
        if (favoriteCh == null)
            return;
        NnProgramManager pMngr = new NnProgramManager();
        NnProgram p = pMngr.findById(pId);
        if (p != null) {
            if (p.getChannelId() == favoriteCh.getId())
                pMngr.delete(p);
        }
        
        // update episode count
        favoriteCh.setCntEpisode(calcuateEpisodeCount(favoriteCh));
        save(favoriteCh);
    }
    
    //create an empty favorite channel
    public NnChannel createFavorite(NnUser user) {
        NnChannel favoriteCh = dao.findFavorite(user.getIdStr());
        if (favoriteCh == null) {
            NnUserProfile profile = user.getProfile();
            favoriteCh = new NnChannel(profile.getName() + "'s Favorite", "", ""); //TODO, maybe assemble the name to avoid name change
            favoriteCh.setUserIdStr(user.getIdStr());
            favoriteCh.setContentType(NnChannel.CONTENTTYPE_FAVORITE);
            favoriteCh.setPublic(true);            
            favoriteCh.setStatus(NnChannel.STATUS_SUCCESS);            
            favoriteCh.setSphere(profile.getSphere());
            favoriteCh = dao.save(favoriteCh);                        
        }
        return favoriteCh;
    }
    
    //save favorite channel along with the program
    //channel and program has been verified if exist
    public void saveFavorite(NnUser user, NnChannel c, NnEpisode e, NnProgram p, String fileUrl, String name, String imageUrl, String duration) {
        if (fileUrl != null && !fileUrl.contains("http")) {
            fileUrl = "http://www.youtube.com/watch?v=" + fileUrl;
        }    
        NnChannel favoriteCh = dao.findFavorite(user.getIdStr());
        if (favoriteCh == null) {
            favoriteCh = this.createFavorite(user);
        }
        NnProgramManager pMngr = new NnProgramManager();        
        if (c.getContentType() != NnChannel.CONTENTTYPE_MIXED) {
            if (p != null && p.getContentType() != NnProgram.CONTENTTYPE_REFERENCE) {
                fileUrl = p.getFileUrl();
                name = p.getName();
                imageUrl = p.getImageUrl();
            }
        }        
        if (fileUrl != null) {
            NnProgram existFavorite = pMngr.findByChannelAndFileUrl(favoriteCh.getId(), fileUrl);
            if (existFavorite == null) {
                existFavorite = new NnProgram(favoriteCh.getId(), name, "", imageUrl);
                existFavorite.setFileUrl(fileUrl);
                existFavorite.setPublic(true);
                existFavorite.setDuration(duration);
                existFavorite.setStorageId(String.valueOf(c.getId()));
                existFavorite.setStatus(NnProgram.STATUS_OK);                
                pMngr.save(existFavorite);                
                
                // update episode count
                favoriteCh.setCntEpisode(calcuateEpisodeCount(favoriteCh));
                save(favoriteCh);
            }
            return;
        }
        //only 9x9 channel or reference of 9x9 channel should hit here,  
        String storageId = "";
        String pname = "";
        String pintro = "";
        String pimageUrl = "";
        if (e != null) { //9x9 channel
            storageId = "e" + String.valueOf(e.getId());
            pname = e.getName();
            pintro = e.getIntro();
            pimageUrl = e.getImageUrl();
        } else { //reference channel
            storageId = p.getStorageId();
            pname = p.getName();
            pintro = p.getIntro();
            pimageUrl = p.getImageUrl();
        }
        NnProgram existFavorite = pMngr.findByChannelAndStorageId(favoriteCh.getId(), storageId);        
        if (existFavorite != null)
            return;
        
        NnProgram newP = new NnProgram(favoriteCh.getId(), pname, pintro, pimageUrl);
        newP.setPublic(true);
        newP.setStatus(NnProgram.STATUS_OK);
        newP.setContentType(NnProgram.CONTENTTYPE_REFERENCE);
        newP.setStorageId(storageId);
        pMngr.save(newP);
        
        // update episode count
        favoriteCh.setCntEpisode(calcuateEpisodeCount(favoriteCh));
        save(favoriteCh);
    }                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
    
    public NnChannel save(NnChannel channel) {
        NnChannel original = dao.findById(channel.getId());
        Date now = new Date();
        if (channel.getCreateDate() == null)
            channel.setCreateDate(now);
        if (channel.getUpdateDate() == null)
            channel.setUpdateDate(now);        
        if (channel.getIntro() != null) {
            channel.setIntro(channel.getIntro().replaceAll("\n", ""));
            channel.setIntro(channel.getIntro().replaceAll("\t", " "));
            if (channel.getIntro().length() > 500)
                channel.getIntro().substring(0, 499);
        }
        if (channel.getName() != null) {
            channel.setName(channel.getName().replaceAll("\n", ""));
            channel.setName(channel.getName().replaceAll("\t", " "));
        }
        //TODO will be inconsistent with those stored in tag table
        if (channel.getTag() != null) {
            channel.setTag(this.processTagText(channel.getTag()));
        }
        channel = dao.save(channel);
        
        NnChannel[] channels = {original, channel};
        if (NNF.getConfigMngr().isQueueEnabled(true)) {
        } else {
            this.processChannelRelatedCounter(channels);
        }
        this.processChannelTag(channel);
        this.resetCache(channel.getId());
        return channel;
    }
    
    public List<NnChannel> saveAll(List<NnChannel> channels) {
        resetCache(channels);
        return dao.saveAll(channels);
    }
    
    public void processChannelRelatedCounter(NnChannel[] channels) {
    }
        
    public List<NnChannel> searchBySvi(String queryStr, short userShard, long userId, String sphere) {
        List<NnChannel> channels = new ArrayList<NnChannel>();
        String url = "http://svi.9x9.tv/api/search.php?";
        url += "shard=" + userShard;
        url += "&userid=" + userId;
        url += "&lang=" + sphere;        
        url += "&s=" + queryStr;
        log.info("svi query url:" + url);
        String chStr = NnNetUtil.urlGet(url);
        log.info("return from svi:" + chStr);
        if (chStr != null) {
            String chs[] = chStr.split(",");
            int i=1;
            for (String cId : chs) {
                if (i > 9) break;                    
                System.out.println("cid:" + cId);
                NnChannel c = this.findById(Long.parseLong(cId.trim()));
                if (c != null)
                    channels.add(c);
                i++;
            }
        }
        return channels;
    }
    
    public static List<NnChannel> search(String keyword, String content, String extra, boolean all, int start, int limit) {
        return NnChannelDao.search(keyword, content, extra, all, start, limit);
    }

    //stack => NnChannel, total number found
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Stack searchSolr(String core, String keyword, String content, String extra, boolean all, int start, int limit) {
        Stack st = SearchLib.search(core, keyword, content, extra, all, start, limit);
        List<Long> ids = (List<Long>) st.pop();
        List<NnChannel> channels = new NnChannelDao().findByIds(ids);
        st.push(channels);
        return st;
    }
 
    public static long searchSize(String queryStr, boolean all) {
        return NnChannelDao.searchSize(queryStr, all);
    }
    
    /**
     * No deletion so we can keep track of blacklist urls 
     */
    public void delete(NnChannel channel) {
    }        
    
    //the url has to be verified(verifyUrl) first
    public short getContentTypeByUrl(String url) {
        short type = NnChannel.CONTENTTYPE_PODCAST;
        if (url.contains("http://www.youtube.com"))
            type = NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL;
        if (url.contains("http://www.youtube.com/view_play_list?p="))
            type = NnChannel.CONTENTTYPE_YOUTUBE_PLAYLIST;
        if (url.contains("facebook.com")) 
            type = NnChannel.CONTENTTYPE_FACEBOOK;
        if (url.contains("http://www.maplestage.net/show"))
            type = NnChannel.CONTENTTYPE_MAPLE_VARIETY;
        if (url.contains("http://www.maplestage.net/drama"))
            type = NnChannel.CONTENTTYPE_MAPLE_SOAP;
        return type;
    }        
            
    public boolean isCounterQualified(NnChannel channel) {
        boolean qualified = false;
        if (channel.getStatus() == NnChannel.STATUS_SUCCESS &&
            channel.getCntEpisode() > 0 &&
            channel.isPublic()) {
            qualified = true;
        }
        return qualified;
    }
    
    public int calcuateEpisodeCount(NnChannel channel) {
        
        if (channel.getContentType() == NnChannel.CONTENTTYPE_FAVORITE) {
            
            NnProgramManager programMngr = new NnProgramManager();
            List<NnProgram> programs = programMngr.findByChannelId(channel.getId());
            
            return programs.size();
            
        } else {
            
            NnEpisodeManager episodeMngr = new NnEpisodeManager();
            List<NnEpisode> episodes = episodeMngr.findByChannelId(channel.getId());
            
            return episodes.size();
        }
    }
    
    public NnChannel findBySourceUrl(String url) {
        if (url == null) {return null;}
        return dao.findBySourceUrl(url);
    }
    
    public NnChannel findById(long id) {
        return dao.findById(id);
    }
    
    public List<NnChannel> findMsoDefaultChannels(long msoId, boolean needSubscriptionCnt) {        
        //find msoIpg
        MsoIpgManager msoIpgMngr = new MsoIpgManager();
        List<MsoIpg>msoIpg = msoIpgMngr.findChannelsByMso(msoId);
        //retrieve channels
        List<NnChannel> channels = new ArrayList<NnChannel>();
        for (MsoIpg i : msoIpg) {
            NnChannel channel = this.findById(i.getChannelId());
            if (channel != null) {
                channel.setType(i.getType());
                channel.setSeq(i.getSeq());
                channels.add(channel);
            }
        }
        return channels;
    }    
    
    public List<NnChannel> findByType(short type) {
        return dao.findByContentType(type);        
    }
    
    public List<NnChannel> findMaples() {
        List<NnChannel> variety = this.findByType(NnChannel.CONTENTTYPE_MAPLE_VARIETY);
        List<NnChannel> soap = this.findByType(NnChannel.CONTENTTYPE_MAPLE_SOAP);
        List<NnChannel> channels = new ArrayList<NnChannel>();
        channels.addAll(variety);
        channels.addAll(soap);
        return channels;
    }

    /**
     * Find hot, featured, trending stories.
     * Featured and recommended can not be overlapped
     * 
     * Hot: 9 channels automatically selected from the BILLBOARD POOL according to the number-of-views (on 9x9.tv)
     * Featured: 9 channels randomly selected from the BILLBOARD POOL; can overlap with HOTTEST, but not RECOMMENDED
     */
    public List<NnChannel> findBillboard(String name, String lang) { 
        List<NnChannel> channels = new ArrayList<NnChannel>();
        RecommendService service = new RecommendService();
        if (name == null)
            return channels;
        if (name.contains(Tag.TRENDING)) {            
            TagManager tagMngr = new TagManager();        
            //name += "(9x9" + lang + ")";
            log.info("find channelsByTag, tag:" + name);
            channels = tagMngr.findChannelsByTag(name, true);
        } else if (name.contains(Tag.FEATURED)) {
            log.info("find featured channels, billboard pool search");
            channels = service.findBillboardPool(9, lang);
        } else if (name.contains(Tag.HOT)) {
            log.info("find hot channels, billboard pool search");
            channels = service.findBillboardPool(9, lang);
            /*
            TagManager tagMngr = new TagManager();        
            name += "(9x9" + lang + ")";
            channels = tagMngr.findChannelsByTag(name, true);
            log.info("find billboard, tag:" + name);
            */            
        } else {
            TagManager tagMngr = new TagManager();        
            //name += "(9x9" + lang + ")";
            log.info("find channelsByTag, tag:" + name);
            channels = tagMngr.findChannelsByTag(name, true);            
        }
        Collections.sort(channels, this.getChannelComparator("updateDate"));
        return channels;
    }

    public List<NnChannel> findStack(String name) {
        List<NnChannel> channels = new ArrayList<NnChannel>();
        if (name == null)
            return channels;
        //name += "(9x9" + lang + ")";
        log.info("find stack, tag:" + name);
        channels = dao.findChannelsByTag(name);
        Collections.sort(channels, this.getChannelComparator("updateDate"));
        return channels;
    }
    
    public Comparator<NnChannel> getChannelComparator(String sort) {
        if (sort.equals("seq")) {
            class ChannelComparator implements Comparator<NnChannel> {
                public int compare(NnChannel channel1, NnChannel channel2) {
                Short seq1 = channel1.getSeq();
                Short seq2 = channel2.getSeq();
                return seq1.compareTo(seq2);
                }
            }
            return new ChannelComparator();    
        }
        if (sort.equals("cntView")) {
            class ChannelComparator implements Comparator<NnChannel> {
                public int compare(NnChannel channel1, NnChannel channel2) {
                Long cntView1 = channel1.getCntView();
                Long cntView2 = channel2.getCntView();
                return cntView2.compareTo(cntView1);
                }
            }
            return new ChannelComparator();    
        }    
        class ChannelComparator implements Comparator<NnChannel> {
            public int compare(NnChannel channel1, NnChannel channel2) {
                Date date1 = channel1.getUpdateDate();
                Date date2 = channel2.getUpdateDate();                
                return date2.compareTo(date1);
            }
        }        
        return new ChannelComparator();
    }
        
    public List<NnChannel> findByIds(List<Long> ids) {        
        return dao.findByIds(ids);
    }
    
    public List<NnChannel> findByStatus(short status) {
        List<NnChannel> channels = dao.findAllByStatus(status);        
        return channels;
    }
    
    public List<NnChannel> findAll() {
        return dao.findAll();
    }
    
    public List<NnChannel> list(int page, int limit, String sidx, String sord) {
        return dao.list(page, limit, sidx, sord);
    }
    
    public List<NnChannel> list(int page, int limit, String sidx, String sord, String filter) {
        return dao.list(page, limit, sidx, sord, filter);
    }
    
    public int total() {
        return dao.total();
    }
    
    public int total(String filter) {
        return dao.total(filter);
    }
    
    public String verifyUrl(String url) {
        if (url == null) return null;
        if (!url.contains("http://") && !url.contains("https://"))
            return null;        
        if (url.contains("youtube.com")) {
            return YouTubeLib.formatCheck(url);
        } else if (url.contains("facebook.com")) {
            return url;
        } else if (url.contains("www.maplestage.net")) {
        //} else if (url.contains("www.maplestage.net") && !url.contains("9x9.tv")) {
            return url;
        }
        return null;
    }

    //find channels created by the user, aka curator
    //player true returns only good and public channels
    public List<NnChannel> findByUser(NnUser user, int limit, boolean isAll) {
        String userIdStr = user.getShard() + "-" + user.getId();
        List<NnChannel> channels = dao.findByUser(userIdStr, limit, isAll);
        if (limit == 0) {
            return channels;
        } else {             
            if (channels.size() > limit)
            return channels.subList(0, limit);
        }
        return channels;
    }

    //TODO change to list, and merge with byUser, and subList is not real
    //used only in player for specific occasion
    public List<NnChannel> findByUserAndHisFavorite(NnUser user, int limit, boolean isAll) {        
        String userIdStr = user.getShard() + "-" + user.getId();
        List<NnChannel> channels = dao.findByUser(userIdStr, limit, isAll);
        boolean needToFake = true;
        for (NnChannel c : channels) {
            if (c.getContentType() == NnChannel.CONTENTTYPE_FAVORITE) {
                needToFake = false;
            }
        }
        if (needToFake) {
            log.info("need to fake");
            NnUserProfile profile = user.getProfile();
            String name = profile.getName() + "'s favorite";
            NnChannel c = new NnChannel(name, profile.getImageUrl(), "");
            c.setContentType(NnChannel.CONTENTTYPE_FAKE_FAVORITE);
            c.setUserIdStr(user.getIdStr());
            c.setNote(c.getFakeId(profile.getProfileUrl())); //shortcut, maybe not very appropriate
            c.setStatus(NnChannel.STATUS_SUCCESS);
            c.setPublic(true);
            c.setSeq((short)(channels.size()+1));
            channels.add(c);
        }
        if (limit == 0) {
            return channels;
        } else {             
            if (channels.size() > limit)
               return channels.subList(0, limit);
        }        
        return channels;
    }
    
    public static short getPlayerDefaultSorting(NnChannel c) {
        //short sorting = NnChannel.SORT_NEWEST_TO_OLDEST;
        short sorting = c.getSorting(); 
        if (c.getContentType() == NnChannel.CONTENTTYPE_MAPLE_SOAP || 
            c.getContentType() == NnChannel.CONTENTTYPE_MAPLE_VARIETY || 
            c.getContentType() == NnChannel.CONTENTTYPE_MIXED)
            sorting = NnChannel.SORT_DESIGNATED;
        if (c.getSorting() == 0) {
            if (c.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL)
                sorting = NnChannel.SORT_NEWEST_TO_OLDEST;
            if (c.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_PLAYLIST)
                sorting = NnChannel.SORT_POSITION_FORWARD;            
        }
        return sorting;
    }           
    
    public void resetCache(List<NnChannel> channels) {
        for (NnChannel c : channels) {
            resetCache(c.getId());
        }
    }
    
    public void resetCache(long channelId) {        
        log.info("reset channel info cache: " + channelId);
        String cId = String.valueOf(channelId);
        CacheFactory.delete(CacheFactory.getChannelLineupKey(cId, 31, PlayerApiService.FORMAT_PLAIN));
        CacheFactory.delete(CacheFactory.getChannelLineupKey(cId, 32, PlayerApiService.FORMAT_PLAIN));
        CacheFactory.delete(CacheFactory.getChannelLineupKey(cId, 40, PlayerApiService.FORMAT_JSON));
        CacheFactory.delete(CacheFactory.getChannelLineupKey(cId, 40, PlayerApiService.FORMAT_PLAIN));
    }
    
    public Object composeReducedChannelLineup(List<NnChannel> channels, short format) {
        String output = "";
        List<ChannelLineup> channelLineup = new ArrayList<ChannelLineup>();
        for (NnChannel c : channels) {
            if (format == PlayerApiService.FORMAT_PLAIN)
                output += (String)this.composeEachReducedChannelLineup(c, format) + "\n";
            else 
                channelLineup.add((ChannelLineup)this.composeEachReducedChannelLineup(c, format));
        }
        if (format == PlayerApiService.FORMAT_PLAIN)
            return output;
        else
            return channelLineup;
    }    

    
    public Object composeEachReducedChannelLineup(NnChannel c, short format) {
        String ytName = c.getSourceUrl() != null ? YouTubeLib.getYouTubeChannelName(c.getSourceUrl()) : "";        
        String name = c.getPlayerName();
        if (name != null) {
            String[] split = name.split("\\|");
            name = split.length == 2 ? split[0] : name;            
        }
        String imageUrl = c.getPlayerPrefImageUrl();
        imageUrl = imageUrl.indexOf("|") < 0 ? imageUrl : imageUrl.substring(0, imageUrl.indexOf("|"));
        String[] ori = {"0",
                        c.getIdStr(),
                        name,
                        c.getPlayerIntro(),
                        imageUrl,
                        String.valueOf(c.getContentType()),
                        ytName,
                        String.valueOf(c.getCntEpisode()),
                        String.valueOf(c.getType()),
                        String.valueOf(c.getStatus()),
                       };
        String output = NnStringUtil.getDelimitedStr(ori);
        output = output.replaceAll("null", "");
        return output;
    }
    
    public Object chAdjust(List<NnChannel> channels, String channelInfo, List<ChannelLineup>channelLineup, short format) {
        if (format == PlayerApiService.FORMAT_PLAIN) {
            String adjust = "";            
            String[] lines = channelInfo.split("\n");
            if (channels.size() > 0) {
                for (int i=0; i<lines.length; i++) {                   
                    lines[i] = lines[i].replaceAll("^\\d+\\t", channels.get(i).getSeq() + "\t");
                    //log.info("ch id:" + channels.get(i).getId() + "; seq = " + channels.get(i).getSeq());
                    adjust += lines[i] + "\n";
                }
            }
            return adjust;
        } else {
            for (int i=0; i<channelLineup.size(); i++) {
                channelLineup.get(i).setPosition(channels.get(i).getSeq());
            }
            return channelLineup;
        }
    }
    
    //return List<ChannelLineup>
    @SuppressWarnings("unchecked")
    public Object getPlayerChannelLineup(List<NnChannel>channels, boolean channelPos, boolean programInfo, boolean isReduced, int version, short format, List<String> result) {
        NnProgramManager programMngr = new NnProgramManager();
        //List<String> result = new ArrayList<String>();
        List<ChannelLineup> channelLineup = new ArrayList<ChannelLineup>();
        String channelOutput = "";
        if (isReduced) {
            log.info("output reduced string");
            if (format == PlayerApiService.FORMAT_PLAIN) {
                channelOutput += (String)this.composeReducedChannelLineup(channels, format);
            } else {
                channelLineup = (List<ChannelLineup>)this.composeReducedChannelLineup(channels, format);
            }
        } else {
            if (format == PlayerApiService.FORMAT_PLAIN)
                channelOutput += this.composeChannelLineup(channels, version, format);
            else
                channelLineup.addAll((List<ChannelLineup>)this.composeChannelLineup(channels, version, format));
        }
        
        if (channelPos) {
            if (format == PlayerApiService.FORMAT_PLAIN)
                channelOutput = (String)this.chAdjust(channels, channelOutput, channelLineup, format);
            else
                channelLineup = (List<ChannelLineup>)this.chAdjust(channels, channelOutput, channelLineup, format);
            
        }
        if (format == PlayerApiService.FORMAT_PLAIN) {
            result.add(channelOutput);
            String programStr = "";
            if (programInfo) {
                programStr = (String) programMngr.findLatestProgramInfoByChannels(channels, format);
                result.add(programStr);
            } 
            String size[] = new String[result.size()];
            return result.toArray(size);
        } else { 
            return channelLineup;
        }
    }
    
    //List<ChannelLineup> or String
    public Object composeChannelLineup(List<NnChannel> channels, int version, short format) {
        String output = "";
        List<ChannelLineup> lineups = new ArrayList<ChannelLineup>();
        for (NnChannel c : channels) {
            if (format == PlayerApiService.FORMAT_PLAIN)  {
                output += this.composeEachChannelLineup(c, version, format) + "\n";
            } else { 
                lineups.add((ChannelLineup)this.composeEachChannelLineup(c, version, format));
            }
        }
        if (format == PlayerApiService.FORMAT_PLAIN)  {
            return output;
        } else {
            return lineups;
        }
    }
    
    public void populateMoreImageUrl(NnChannel channel) {
        
        List<String> imgs = new ArrayList<String>();
        
        if (channel.getContentType() == NnChannel.CONTENTTYPE_FAVORITE) {
            NnProgramManager programMngr = new NnProgramManager();
            String filter = "channelId == " + channel.getId();
            List<NnProgram> programs = programMngr.list(1, 50, "updateDate", "desc", filter);
            
            for (int i = 0; i < programs.size() && imgs.size() < 3; i++) {
                
                String img = programs.get(i).getImageUrl();
                if (img != null && img.length() > 0) {
                    imgs.add(img);
                }
            }
            
        } else {
            NnEpisodeManager episodeMngr = new NnEpisodeManager();
            String filter = "channelId == " + channel.getId();
            List<NnEpisode> episodes = episodeMngr.list(1, 50, "seq", "asc", filter);
            
            for (int i = 0; i < episodes.size() && imgs.size() < 3; i++) {
                
                String img = episodes.get(i).getImageUrl();
                
                if (img != null && img.length() > 0) {
                    imgs.add(img);
                }
            }
        }
        
        // fill up with default episode thubmnail
        while (imgs.size() < 3) {
            imgs.add(NnChannel.IMAGE_EPISODE_URL);
        }
        
        if (imgs.size() > 0) {
            
            String moreImageUrl = imgs.remove(0);
            for (String imageUrl : imgs) {
                
                moreImageUrl += "|" + imageUrl;
            }
            
            channel.setMoreImageUrl(moreImageUrl);
        }
        
    }
    
    public boolean isChannelOwner(NnChannel channel, String mail) {
        
        if (channel == null || mail == null) {
            return false;
        }
        
        NnUserManager userMngr = new NnUserManager();
        NnUser user = userMngr.findById(channel.getUserId(), 1);
        if(user == null) {
            return false;
        }
        
        if ((user.getUserEmail() != null) && user.getUserEmail().equals(mail)) {
            return true;
        }
        
        return false;
    }

    public void reorderUserChannels(NnUser user) {
        
        // the results should be same as ApiUser.userChannels() GET operation, but not include fake channel.
        String userIdStr = user.getShard() + "-" + user.getId();
        List<NnChannel> channels = dao.findByUser(userIdStr, 0, true);
        
        Collections.sort(channels, getChannelComparator("seq"));
        
        for (int i = 0; i < channels.size(); i++) {
            
            channels.get(i).setSeq((short)(i + 1));
        }
        
        saveAll(channels);
    }
    
    public void renewChannelUpdateDate(long channelId) {
        Date now = new Date();
        NnChannel channel = dao.findById(channelId);
        if (channel == null) {
            return ;
        }
        channel.setUpdateDate(now);
        save(channel);
    }
    
    public List<NnChannel> findPersonalHistory(long userId, long msoId) {
        return dao.findPersonalHistory(userId, msoId);
    }
    
    /** adapt NnChannel to format that CMS API required */
    public List<NnChannel> responseNormalization(List<NnChannel> channels) {
        
        for (NnChannel channel : channels) {
            this.normalize(channel);
        }
        
        return channels;
    }
    
    /** adapt NnChannel to format that CMS API required */
    public void normalize(NnChannel channel) {
        
        // imageUrl TODO YouTube-sync-channel may need such process, check later when database ready
        if ((channel.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL ||
                channel.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_PLAYLIST) &&
             channel.getImageUrl() != null) {
            
            String[] imageUrls = channel.getImageUrl().split("\\|");
            channel.setImageUrl(imageUrls[0]);
        }
        
        // name
        channel.setName(NnStringUtil.revertHtml(channel.getName()));
        
        // intro
        channel.setIntro(NnStringUtil.revertHtml(channel.getIntro()));
        
    }
    
    /** get CategoryId that Channel belongs to */
    public Long getCategoryId(Long channelId) {
        
        if (channelId == null) {
            return null;
        }
        
        MsoManager msoMngr = new MsoManager();
        Mso nnMso = msoMngr.findNNMso();
        StoreService storeServ = new StoreService();
        List<Long> categoryIds = storeServ.findCategoryIdsByChannelId(channelId, nnMso.getId());
        if (categoryIds != null && categoryIds.size() > 0) {
            return categoryIds.get(0);
        } else {
            return null;
        }
    }
    
    public void populateCategoryId(NnChannel channel) {
        
        if (channel == null)
            return;
        
        MsoManager msoMngr = new MsoManager();
        Mso nnMso = msoMngr.findNNMso();
        StoreService storeServ = new StoreService();
        
        List<Long> categoryIds = storeServ.findCategoryIdsByChannelId(channel.getId(), nnMso.getId());
        if (categoryIds != null && categoryIds.size() > 0) {
            channel.setCategoryId(categoryIds.get(0));
        }
    }
    
    //ChannelLineup or String
    public Object composeEachChannelLineup(NnChannel c, int version, short format) {
        Object result = null;
        log.info("version number: " + version);

        String cacheKey = CacheFactory.getChannelLineupKey(String.valueOf(c.getId()), version, format);
        try {
            result = CacheFactory.get(cacheKey);
        } catch (Exception e) {
            log.info("memcache error");
        }
        if (result != null && c.getId() != 0) { //id = 0 means fake channel, it is dynamic
            log.info("get channel lineup from cache" + ". v=" + version +";channel=" + c.getId());
            return result;
        }
       
        log.info("channel lineup NOT from cache:" + c.getId());
        //name and last episode title
        //favorite channel name will be overwritten later
        String name = c.getPlayerName() == null ? "" : c.getPlayerName();
        String[] split = name.split("\\|");
        name = split.length == 2 ? split[0] : name;
        //String lastEpisodeTitle = split.length == 2 ? split[1] : "";

        //image url, favorite channel image will be overwritten later
        String imageUrl = c.getPlayerPrefImageUrl();
        if (version < 32) {
                imageUrl = imageUrl.indexOf("|") < 0 ? imageUrl : imageUrl.substring(0, imageUrl.indexOf("|"));
                log.info("v31 imageUrl:" + imageUrl);
        }
        if (version > 31 && (
                c.getContentType() == NnChannel.CONTENTTYPE_MAPLE_SOAP ||
            c.getContentType() == NnChannel.CONTENTTYPE_MAPLE_VARIETY ||
            c.getContentType() == NnChannel.CONTENTTYPE_MIXED ||
            c.getContentType() == NnChannel.CONTENTTYPE_FAVORITE)) {
            if (c.getContentType() != NnChannel.CONTENTTYPE_MIXED) {
                NnProgramManager pMngr = new NnProgramManager();
                List<NnProgram> programs = pMngr.findPlayerProgramsByChannel(c.getId());
                log.info("programs = " + programs.size());
                Collections.sort(programs, pMngr.getProgramComparator("updateDate"));
                for (int i=0; i<3; i++) {
                    if (i < programs.size()) {
                       //lastEpisodeTitle = programs.get(0).getName();
                       imageUrl += "|" + programs.get(i).getImageUrl();
                       log.info("imageUrl = " + imageUrl);
                    } else {
                       i=4;
                    }
                }
            } else {
                NnEpisodeManager eMngr = new NnEpisodeManager();
                List<NnEpisode> episodes = eMngr.findPlayerEpisodes(c.getId(), c.getSorting(), 0, 50);
                log.info("episodes = " + episodes.size());
                Collections.sort(episodes, eMngr.getEpisodePublicSeqComparator());
                for (int i=0; i<3; i++) {
                    if (i < episodes.size()) {
                       //lastEpisodeTitle = episodes.get(0).getName();
                       imageUrl += "|" + episodes.get(i).getImageUrl();
                       log.info("imageUrl = " + imageUrl);
                    } else {
                       i=4;
                    }
                }
            }
        }
        short contentType = c.getContentType();
        if (contentType == NnChannel.CONTENTTYPE_FAKE_FAVORITE)
            contentType = NnChannel.CONTENTTYPE_FAVORITE;
        //poi
        String poiStr = "";
        if (version > 32) {
            PoiEventManager eventMngr = new PoiEventManager();
            PoiPointManager pointMngr = new PoiPointManager();
            List<PoiPoint> points = pointMngr.findCurrentByChannel(c.getId());
            //List<Poi> pois = pointMngr.findCurrentPoiByChannel(c.getId());
            List<PoiEvent> events = new ArrayList<PoiEvent>();
            for (PoiPoint p : points) {
                PoiEvent event = eventMngr.findByPoint(p.getId());
                events.add(event);
            }
            if (points.size() != events.size()) {
                log.info("Bad!!! should not continue.");
                points.clear();
            }
            //format: start time;endTime;type;context|
            for (int i=0; i<points.size(); i++) {
                PoiPoint point = points.get(i);
                PoiEvent event = events.get(i);
                //Poi poi = pois.get(i);
                String context = NnStringUtil.urlencode(event.getContext());
                //String poiStrHere = poi.getId() + ";" + point.getStartTime() + ";" + point.getEndTime() + ";" + event.getType() + ";" + context + "|";
                String poiStrHere = point.getStartTime() + ";" + point.getEndTime() + ";" + event.getType() + ";" + context + "|";
                log.info("poi output:" + poiStrHere);
                poiStr += poiStrHere;
                log.info("poi output:" + poiStr);
            }
        }
        if (format == PlayerApiService.FORMAT_PLAIN) {
            List<String> ori = new ArrayList<String>();
            ori.add("0");
            ori.add(c.getIdStr());
            ori.add(name);
            ori.add(c.getPlayerIntro());
            ori.add(imageUrl); //c.getPlayerPrefImageUrl());                        
            ori.add(String.valueOf(c.getCntEpisode()));
            ori.add(String.valueOf(c.getType()));
            ori.add(String.valueOf(c.getStatus()));
            ori.add(String.valueOf(c.getContentType()));
            ori.add(c.getPlayerPrefSource());
            ori.add(String.valueOf(c.getUpdateDate().getTime()));
            ori.add(String.valueOf(getPlayerDefaultSorting(c))); //use default sorting for all
            ori.add(c.getPiwik());
            ori.add(""); //recently watched program
            ori.add(c.getOriName());
            ori.add(String.valueOf(c.getCntSubscribe())); //cnt subscribe, replace
            ori.add(String.valueOf(populateCntView(c).getCntView()));
            ori.add(c.getTag());
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
            lineup.setDescription(c.getPlayerIntro());
            lineup.setThumbnail(imageUrl); //c.getPlayerPrefImageUrl());                        
            lineup.setNumberOfEpisode(c.getCntEpisode());
            lineup.setType(c.getType());
            lineup.setStatus(c.getStatus());
            lineup.setContentType(c.getContentType());
            lineup.setChannelSource(c.getPlayerPrefSource());
            lineup.setLastUpdateTime(c.getUpdateDate().getTime());
            lineup.setSorting(getPlayerDefaultSorting(c)); //use default sorting for all
            lineup.setPiwikId(c.getPiwik());
            lineup.setRecentlyWatchedPrograms(""); //recently watched program
            lineup.setYoutubeName(c.getOriName());
            lineup.setNumberOfSubscribers(c.getCntSubscribe()); //cnt subscribe, replace
            lineup.setNumberOfViews(populateCntView(c).getCntView());
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
            
    public static boolean isValidChannelSourceUrl(String urlStr) {        
        if (urlStr == null) {
            return false;
        }
        
        if (urlStr.contains(YouTubeLib.youtubeChannelPrefix) || urlStr.contains(YouTubeLib.youtubePlaylistPrefix)) {
            return true;
        }
        return false;
    }
    
    public NnChannel populateCntView(NnChannel channel) {
        
        try {
            String name = "u_ch" + channel.getId();
            String result = (String)CacheFactory.get(name);
            if (result != null) {
                channel.setCntView(Integer.parseInt(result));
                return channel;
            }
            log.info("cnt view not in the cache:" + name);
            CounterFactory factory = new CounterFactory();            
            long cntView = factory.getCount(name);
            channel.setCntView(cntView);
            CacheFactory.set(name, String.valueOf(cntView));
        } catch (Exception e) {
            e.printStackTrace();
            channel.setCntView(0);
        }
        return channel;
    }
    
    public NnChannel populateCntVisit(NnChannel channel) { // is CntVisit == CntView ??
        try {
            String name = "u_ch" + channel.getId();
            String result = (String)CacheFactory.get(name);
            if (result != null) {
                channel.setCntVisit(Integer.parseInt(result));
                return channel;
            }
            log.info("cnt view not in the cache:" + name);
            CounterFactory factory = new CounterFactory();            
            long cntVisit = factory.getCount(name);
            channel.setCntVisit(cntVisit);
            CacheFactory.set(name, String.valueOf(cntVisit));
        } catch (Exception e){
            //e.printStackTrace();
            System.out.println("msg:" + e.getMessage());
            System.out.println("cause:" + e.getCause());
            channel.setCntVisit(0);
        }
        return channel;
    }
    
    public void populateAutoSync(NnChannel channel) {
        
        if (channel == null) return;
        
        NnChannelPrefManager prefMngr = new NnChannelPrefManager();
        
        List<NnChannelPref> channelPrefs = prefMngr.findByChannelIdAndItem(channel.getId(), NnChannelPref.AUTO_SYNC);
        if (channelPrefs == null || channelPrefs.isEmpty()) {
            
            channel.setAutoSync(NnChannelPref.OFF);
            return;
        }
        channel.setAutoSync(channelPrefs.get(0).getValue());
    }
}
