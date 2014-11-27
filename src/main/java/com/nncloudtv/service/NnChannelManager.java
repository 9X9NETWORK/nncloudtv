package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collection;
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

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnChannelDao;
import com.nncloudtv.dao.NnEpisodeDao;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.FacebookLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.SearchLib;
import com.nncloudtv.lib.stream.YouTubeLib;
import com.nncloudtv.model.LocaleTable;
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
import com.nncloudtv.web.api.ApiContext;
import com.nncloudtv.web.api.NnStatusCode;
import com.nncloudtv.web.json.player.ChannelLineup;

@Service
public class NnChannelManager {

    protected static final Logger log = Logger.getLogger(NnChannelManager.class.getName());
    
    private NnChannelDao dao = NNF.getChannelDao();
    
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
    
    public NnChannel findById(String channelIdStr) {
        
        if (channelIdStr.contains("yt")) {
            
            return NNF.getYtChannelMngr().convert(channelIdStr);
            
        } else {
            
            return dao.findById(channelIdStr);
        }
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
            }
        }
        channel.setPublic(false);
        channel.setLang(lang);
        Date now = NnDateUtil.now();
        channel.setCreateDate(now);
        channel.setUpdateDate(now);
        channel = this.save(channel);
        if (channel.getContentType() == NnChannel.CONTENTTYPE_MAPLE_SOAP ||
            channel.getContentType() == NnChannel.CONTENTTYPE_MAPLE_VARIETY ||
            channel.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_SPECIAL_SORTING) {
            new DepotService().submitToTranscodingService(channel.getId(), channel.getSourceUrl(), req);                                
        }
        
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
        Date now = NnDateUtil.now();
        channel.setCreateDate(now);
        channel.setUpdateDate(now);
        
        return save(channel);
    }
    
    //check existence is your responsibility (for now)
    //passing a good url is your responsibility (for now) 
    public NnChannel createYoutubeChannel(String url) {
        NnChannel channel = new NnChannel(url);
        channel.setStatus(NnChannel.STATUS_PROCESSING);
        channel.setContentType(NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL);
        channel.setPublic(false);
        channel.setLang(LocaleTable.LANG_EN);
        Date now = NnDateUtil.now();
        channel.setCreateDate(now);
        channel.setUpdateDate(now);
        
        return save(channel);
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
        NnProgram p = NNF.getProgramMngr().findById(pId);
        if (p != null) {
            if (p.getChannelId() == favoriteCh.getId())
                NNF.getProgramMngr().delete(p);
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
        if (c.getContentType() != NnChannel.CONTENTTYPE_MIXED) {
            if (p != null && p.getContentType() != NnProgram.CONTENTTYPE_REFERENCE) {
                fileUrl = p.getFileUrl();
                name = p.getName();
                imageUrl = p.getImageUrl();
            }
        }
        NnProgramManager pMngr = NNF.getProgramMngr();
        if (fileUrl != null) {
            NnProgram existFavorite = pMngr.findByChannelAndFileUrl(favoriteCh.getId(), fileUrl);
            if (existFavorite == null) {
                existFavorite = new NnProgram(favoriteCh.getId(), name, "", imageUrl);
                existFavorite.setFileUrl(fileUrl);
                existFavorite.setPublic(true);
                existFavorite.setDuration(duration);
                existFavorite.setStorageId(String.valueOf(c.getId()));
                existFavorite.setStatus(NnProgram.STATUS_OK);                
                NNF.getProgramMngr().save(existFavorite);                
                
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
        
        NnChannel origin = dao.findById(channel.getId());
        Date now = NnDateUtil.now();
        
        if (channel.getCreateDate() == null) {
            channel.setCreateDate(now);
        }
        if (channel.getUpdateDate() == null) {
            channel.setUpdateDate(now);
        }
        if (channel.getIntro() != null) {
            channel.setIntro(channel.getIntro().replaceAll("\n", ""));
            channel.setIntro(channel.getIntro().replaceAll("\t", " "));
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
        
        NnChannel[] channels = { origin, channel };
        if (NNF.getConfigMngr().isQueueEnabled(true)) {
            
        } else {
            
            this.processChannelRelatedCounter(channels);
        }
        processChannelTag(channel);
        resetCache(channel.getId());
        
        return channel;
    }
    
    public Collection<NnChannel> save(Collection<NnChannel> channels, boolean resetCache) {
        
        if (resetCache)
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
        
        return NNF.getChannelDao().search(keyword, content, extra, all, start, limit);
    }
    
    //stack => NnChannel, total number found
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Stack searchSolr(String core, String keyword, String content, String extra, boolean all, int start, int limit) {
        Stack st = SearchLib.search(core, keyword, content, extra, all, start, limit);
        List<Long> ids = (List<Long>) st.pop();
        List<NnChannel> channels = NNF.getChannelDao().findAllByIds(ids);
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
            
            List<NnProgram> programs = NNF.getProgramMngr().findByChannelId(channel.getId());
            
            return programs.size();
            
        } else {
            
            List<NnEpisode> episodes = NNF.getEpisodeMngr().findByChannelId(channel.getId());
            
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
        Collections.sort(channels, getComparator("updateDate"));
        return channels;
    }

    public List<NnChannel> findStack(String name) {
        List<NnChannel> channels = new ArrayList<NnChannel>();
        if (name == null)
            return channels;
        //name += "(9x9" + lang + ")";
        log.info("find stack, tag:" + name);
        channels = dao.findChannelsByTag(name);
        Collections.sort(channels, getComparator("updateDate"));
        return channels;
    }
    
    public static Comparator<NnChannel> getComparator(String sort) {
        
        if (sort.equals("seq")) {
            
            return new Comparator<NnChannel>() {
                
                public int compare(NnChannel channel1, NnChannel channel2) {
                    
                    Short seq1 = channel1.getSeq();
                    Short seq2 = channel2.getSeq();
                    
                    return seq1.compareTo(seq2);
                }
            };
            
        } else if (sort.equals("cntView")) {
            
            return new Comparator<NnChannel>() {
                
                public int compare(NnChannel channel1, NnChannel channel2) {
                    
                    Long cntView1 = channel1.getCntView();
                    Long cntView2 = channel2.getCntView();
                    
                    return cntView2.compareTo(cntView1);
                }
            };
            
        } else if (sort.equals("updateDateInSet")) {
            
            return new Comparator<NnChannel>() {
                
                public int compare (NnChannel channel1, NnChannel channel2) {
                    
                    Date date1 = channel1.getUpdateDate();
                    Date date2 = channel2.getUpdateDate();
                    if (channel1.isAlwaysOnTop()) {
                        date1 = new Date(date1.getTime() * 2);
                    }
                    if (channel2.isAlwaysOnTop()) {
                        date2 = new Date(date2.getTime() * 2);
                    }
                    
                    return date2.compareTo(date1);
                }
            };
            
        } else {
            
            return new Comparator<NnChannel>() {
                
                public int compare(NnChannel channel1, NnChannel channel2) {
                    
                    Date date1 = channel1.getUpdateDate();
                    Date date2 = channel2.getUpdateDate();
                    
                    return date2.compareTo(date1);
                }
            };
        }
    }
    
    public List<NnChannel> findByIds(Collection<Long> ids) {
        return dao.findAllByIds(ids);
    }
    
    public List<NnChannel> findByStatus(short status) {
        
        return dao.findAllByStatus(status);
    }
    
    public List<NnChannel> findAll() {
        return dao.findAll();
    }
    
    public List<NnChannel> list(int page, int limit, String sort) {
        return dao.list(page, limit, sort);
    }
    
    public List<NnChannel> list(int page, int limit, String sort, String filter) {
        return dao.list(page, limit, sort, filter);
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
        
        List<NnChannel> channels = dao.findByUser(user.getIdStr(), limit, isAll);
        if (limit > 0 && channels.size() > limit) {
            
            return channels.subList(0, limit);
        }
        return channels;
    }
    
    public List<NnChannel> findByUsers(List<NnUser> users, int limit) {
        
        List<String> idList = new ArrayList<String>();
        for (NnUser user : users) {
            
            idList.add(user.getIdStr());
        }
        
        return dao.findByUser(StringUtils.join(idList, ","), limit, false);
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
        
        short sorting = c.getSorting();
        if (sorting == NnChannel.SORT_TIMED_LINEAR) {
            
            return sorting;
        }
        if (c.getContentType() == NnChannel.CONTENTTYPE_MAPLE_SOAP || 
            c.getContentType() == NnChannel.CONTENTTYPE_MAPLE_VARIETY || 
            c.getContentType() == NnChannel.CONTENTTYPE_MIXED) {
            
            sorting = NnChannel.SORT_DESIGNATED;
        }
        if (c.getSorting() == 0) {
            if (c.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL)
                sorting = NnChannel.SORT_NEWEST_TO_OLDEST;
            if (c.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_PLAYLIST)
                sorting = NnChannel.SORT_POSITION_FORWARD;
        }
        return sorting;
    }
    
    public void resetCache(Collection<NnChannel> channels) {
        
        if (channels == null || channels.isEmpty()) return;
        
        log.info("reset channel cache count = " + channels.size());
        
        List<String> keys = new ArrayList<String>();
        for (NnChannel channel : channels) {
            
            keys.addAll(CacheFactory.getAllChannelInfoKeys(channel.getId()));
            keys.add(CacheFactory.getChannelCntItemKey(channel.getId()));
        }
        CacheFactory.deleteAll(keys);
    }
    
    public void resetCache(long channelId) {
        
        log.info("reset channel info cache = " + channelId);
        
        List<String> keys = CacheFactory.getAllChannelInfoKeys(channelId);
        keys.add(CacheFactory.getChannelCntItemKey(channelId));
        CacheFactory.deleteAll(keys);
    }
    
    public Object composeReducedChannelLineup(List<NnChannel> channels, short format) {
        String output = "";
        List<ChannelLineup> channelLineup = new ArrayList<ChannelLineup>();
        for (NnChannel c : channels) {
            if (format == ApiContext.FORMAT_PLAIN)
                output += (String)this.composeEachReducedChannelLineup(c, format) + "\n";
            else 
                channelLineup.add((ChannelLineup)this.composeEachReducedChannelLineup(c, format));
        }
        if (format == ApiContext.FORMAT_PLAIN)
            return output;
        else
            return channelLineup;
    }
    
    public Object composeEachReducedChannelLineup(NnChannel c, short format) {
        String ytName = c.getSourceUrl() != null ? YouTubeLib.getYouTubeChannelName(c.getSourceUrl()) : "";        
        String name = c.getPlayerName();
        if (name != null) {
            String[] split = name.split("\\|");
            name = split.length > 2 ? split[0] : name;            
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
        if (format == ApiContext.FORMAT_PLAIN) {
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
    public Object getPlayerChannelLineup(List<NnChannel>channels, boolean channelPos, boolean programInfo, boolean isReduced, ApiContext ctx, List<String> result) {
        
        List<ChannelLineup> channelLineup = new ArrayList<ChannelLineup>();
        String channelOutput = "";
        if (isReduced) {
            log.info("output reduced string");
            if (ctx.isPlainFmt()) {
                channelOutput += (String)this.composeReducedChannelLineup(channels, ctx.getFmt());
            } else {
                channelLineup = (List<ChannelLineup>)this.composeReducedChannelLineup(channels, ctx.getFmt());
            }
        } else {
            if (ctx.isPlainFmt())
                channelOutput += this.composeChannelLineup(channels, ctx);
            else
                channelLineup.addAll((List<ChannelLineup>)this.composeChannelLineup(channels, ctx));
        }
        
        if (channelPos) {
            if (ctx.isPlainFmt())
                channelOutput = (String)this.chAdjust(channels, channelOutput, channelLineup, ctx.getFmt());
            else
                channelLineup = (List<ChannelLineup>)this.chAdjust(channels, channelOutput, channelLineup, ctx.getFmt());
            
        }
        if (ctx.isPlainFmt()) {
            result.add(channelOutput);
            String programStr = "";
            if (programInfo) {
                programStr = (String) NNF.getProgramMngr().findLatestProgramInfoByChannels(channels, ctx.getFmt());
                result.add(programStr);
            } 
            String size[] = new String[result.size()];
            return result.toArray(size);
        } else { 
            return channelLineup;
        }
    }
    
    //List<ChannelLineup> or String
    public Object composeChannelLineup(List<NnChannel> channels, ApiContext ctx) {
        String output = "";
        List<ChannelLineup> lineups = new ArrayList<ChannelLineup>();
        for (NnChannel c : channels) {
            if (ctx.isPlainFmt())  {
                output += this.composeEachChannelLineup(c, ctx) + "\n";
            } else { 
                lineups.add((ChannelLineup)this.composeEachChannelLineup(c, ctx));
            }
        }
        if (ctx.isPlainFmt())  {
            return output;
        } else {
            return lineups;
        }
    }
    
    public void populateMoreImageUrl(NnChannel channel) {
        
        List<String> imgs = new ArrayList<String>();
        String cacheKey = CacheFactory.getNnChannelMoreImageUrlKey(channel.getId());
        
        String result = (String) CacheFactory.get(cacheKey);
        if (result != null) {
            channel.setMoreImageUrl(result);
            return;
        }
        
        String filter = String.format("channelId = %d AND isPublic = TRUE AND imageUrl IS NOT NULL", channel.getId());
        String sort = channel.getSorting() == NnChannel.SORT_POSITION_REVERSE ? "seq DESC" : "seq ASC";
        if (channel.getSorting() == NnChannel.SORT_TIMED_LINEAR)
            sort = NnEpisodeDao.V2_LINEAR_SORTING;
        
        List<NnEpisode> episodes = NNF.getEpisodeMngr().listV2(0, 3, sort, filter);
        
        for (NnEpisode episode : episodes) {
            if (imgs.size() < 3) {
                String imageUrl = episode.getImageUrl();
                if (episode.isPublic() && imageUrl != null && imageUrl.length() > 0)
                    imgs.add(episode.getImageUrl());
            } else {
                break;
            }
        }
        // fill up with default episode thubmnail
        while (imgs.size() < 3)
            imgs.add(NnChannel.IMAGE_EPISODE_URL);
        
        result = StringUtils.join(imgs, "|");
        channel.setMoreImageUrl(result);
        
        CacheFactory.set(cacheKey, result);
    }
    
    public void reorderUserChannels(final NnUser user) {
        
        if (user == null) return;
        
        // due to time consuming, always run it in background
        (new Thread() {
            public void run() {
                long before = NnDateUtil.timestamp();
                System.out.println("[reorder_channels] start");
                List<NnChannel> channels = dao.findByUser(user.getIdStr(), 0, true);
                
                Collections.sort(channels, getComparator("seq"));
                
                for (int i = 0; i < channels.size(); i++) {
                    
                    channels.get(i).setSeq((short)(i + 1));
                }
                
                save(channels, false);
                System.out.println(String.format("[reorder_channels] ended (%d ms)", NnDateUtil.timestamp() - before));
            }
        }).start();
        
    }
    
    public void renewUpdateDateOnly(NnChannel channel) {
        
        if (channel == null) return;
        channel.setUpdateDate(NnDateUtil.now());
        resetCache(channel.getId());
        dao.save(channel);
    }
    
    public List<NnChannel> findPersonalHistory(long userId, long msoId) {
        
        return dao.findPersonalHistory(userId, msoId);
    }
    
    /** adapt NnChannel to format that CMS API required */
    public List<NnChannel> normalize(List<NnChannel> channels) {
        
        for (NnChannel channel : channels) {
            
            normalize(channel);
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
    
    public void populateCategoryId(NnChannel channel) {
        
        if (channel == null) { return; }
        
        String cacheKey = CacheFactory.getSystemCategoryKey(channel.getId());
        Long categoryId = (Long) CacheFactory.get(cacheKey);
        if (categoryId == null) {
            List<Long> categoryIds = NNF.getCategoryService().findSystemCategoryIdsByChannel(channel);
            categoryId = categoryIds.size() > 0 ? categoryIds.get(0) : Long.valueOf(0);
            CacheFactory.set(cacheKey, categoryId);
        }
        channel.setCategoryId(categoryId);
    }
    
    //ChannelLineup or String
    public Object composeEachChannelLineup(NnChannel channel, ApiContext ctx) {
        Object result = null;
        
        String cacheKey = CacheFactory.getChannelLineupKey(String.valueOf(channel.getId()), ctx.getVer(), ctx.getFmt());
        try {
            result = CacheFactory.get(cacheKey);
        } catch (Exception e) {
            log.info("memcache error");
        }
        if (result != null && channel.getId() != 0) { //id = 0 means fake channel, it is dynamic
            log.info("get channel lineup from cache. v=" + ctx.getVer() +";channel=" + channel.getId());
            return result;
        }
        
        // channel banner, social feeds
        String sns = null, banner = null;
        NnChannelPref bannerPref = NNF.getChPrefMngr().findByChannelIdAndItem(channel.getId(), NnChannelPref.BANNER_IMAGE);
        if (bannerPref != null) {
            banner = bannerPref.getValue();
        }
        NnChannelPref snsPref = NNF.getChPrefMngr().findByChannelIdAndItem(channel.getId(), NnChannelPref.SOCIAL_FEEDS);
        if (snsPref != null) {
            sns = snsPref.getValue();
        }
        
        log.info("channel lineup NOT from cache:" + channel.getId());
        //name and last episode title
        //favorite channel name will be overwritten later
        String name = channel.getPlayerName() == null ? "" : channel.getPlayerName();
        String[] split = name.split("\\|");
        name = split.length > 2 ? split[0] : name;
        String lastEpisodeTitle = name;
        //String lastEpisodeTitle = split.length == 2 ? split[1] : "";
        
        //image url, favorite channel image will be overwritten later
        String imageUrl = channel.getPlayerPrefImageUrl();
        if (ctx.getVer() < 32) {
                imageUrl = imageUrl.indexOf("|") < 0 ? imageUrl : imageUrl.substring(0, imageUrl.indexOf("|"));
                log.info("v31 imageUrl:" + imageUrl);
        }
        if (ctx.getVer() > 31 && (
            channel.getContentType() == NnChannel.CONTENTTYPE_MAPLE_SOAP       ||
            channel.getContentType() == NnChannel.CONTENTTYPE_MAPLE_VARIETY    ||
            channel.getContentType() == NnChannel.CONTENTTYPE_MIXED            ||
            channel.getContentType() == NnChannel.CONTENTTYPE_FAVORITE         ||
            channel.getContentType() == NnChannel.CONTENTTYPE_VIRTUAL_CHANNEL1 ||
            channel.getContentType() == NnChannel.CONTENTTYPE_VIRTUAL_CHANNEL2)) {
            
            if (channel.getContentType() == NnChannel.CONTENTTYPE_VIRTUAL_CHANNEL1) {
                
                Long categoryId = Long.parseLong(channel.getSourceUrl());
                if (categoryId != null) {
                    
                    List<NnEpisode> episodes = NNF.getCategoryService().getAllEpisodes(categoryId);
                    Collections.sort(episodes, NnEpisodeManager.getComparator("publishDate"));
                    for (int i = 0; i < 3 && i < episodes.size(); i++) {
                        lastEpisodeTitle += "|" + episodes.get(i).getName();
                        imageUrl += "|" + episodes.get(i).getImageUrl();
                    }
                }
            } else if (channel.getContentType() == NnChannel.CONTENTTYPE_VIRTUAL_CHANNEL2) {
                
                List<NnEpisode> episodes = new ArrayList<NnEpisode>();
                Long categoryId = Long.parseLong(channel.getSourceUrl());
                if (categoryId != null) {
                    
                    List<NnChannel> channels = NNF.getCategoryService().getCategoryChannels(categoryId);
                    for (NnChannel ch : channels) {
                        List<NnEpisode> candidates = NNF.getEpisodeMngr().findPlayerLatestEpisodes(ch.getId(), ch.getSorting());
                        if (candidates.size() > 0) {
                            
                            episodes.add(candidates.get(0));
                        }
                    }
                    Collections.sort(episodes, NnEpisodeManager.getComparator("publishDate"));
                    for (int i = 0; i < 3 && i < episodes.size(); i++) {
                        lastEpisodeTitle += "|" + episodes.get(i).getName();
                        imageUrl += "|" + episodes.get(i).getImageUrl();
                    }
                }
            } else if (channel.getContentType() == NnChannel.CONTENTTYPE_MIXED) {
                List<NnEpisode> episodes = NNF.getEpisodeMngr().findPlayerEpisodes(channel.getId(), channel.getSorting(), 0, 50);
                log.info("episodes = " + episodes.size());
                Collections.sort(episodes, NnEpisodeManager.getComparator("isPublicFirst"));
                for (int i=0; i<3; i++) {
                    if (i < episodes.size()) {
                       lastEpisodeTitle += "|" + episodes.get(i).getName();
                       imageUrl += "|" + episodes.get(i).getImageUrl();
                       log.info("imageUrl = " + imageUrl);
                    } else {
                       i=4;
                    }
                }
            } else {
                List<NnProgram> programs = NNF.getProgramMngr().findPlayerProgramsByChannel(channel.getId());
                log.info("programs = " + programs.size());
                Collections.sort(programs, NNF.getProgramMngr().getProgramComparator("updateDate"));
                for (int i=0; i<3; i++) {
                    if (i < programs.size()) {
                       imageUrl += "|" + programs.get(i).getImageUrl();
                       log.info("imageUrl = " + imageUrl);
                    } else {
                       i=4;
                    }
                }
            }
        }
        short contentType = channel.getContentType();
        if (contentType == NnChannel.CONTENTTYPE_FAKE_FAVORITE)
            contentType = NnChannel.CONTENTTYPE_FAVORITE;
        //poi
        String poiStr = "";
        if (ctx.getVer() > 32) {
            List<PoiPoint> points = NNF.getPoiPointMngr().findCurrentByChannelId(channel.getId());
            List<PoiEvent> events = new ArrayList<PoiEvent>();
            for (PoiPoint point : points) {
                PoiEvent event = NNF.getPoiEventMngr().findByPointId(point.getId());
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
        if (ctx.isPlainFmt()) {
            List<String> ori = new ArrayList<String>();
            ori.add("0");
            ori.add(channel.getIdStr());
            ori.add(name);
            ori.add(channel.getPlayerIntro());
            ori.add(imageUrl); //c.getPlayerPrefImageUrl());                        
            ori.add(String.valueOf(channel.getCntEpisode()));
            ori.add(String.valueOf(channel.getType()));
            ori.add(String.valueOf(channel.getStatus()));
            ori.add(String.valueOf(channel.getContentType()));
            ori.add(channel.getPlayerPrefSource());
            ori.add(String.valueOf(channel.getUpdateDate().getTime()));
            ori.add(String.valueOf(getPlayerDefaultSorting(channel))); //use default sorting for all
            ori.add(String.valueOf(channel.isPaidChannel())); //paid channel reference
            ori.add(""); //recently watched program
            ori.add(channel.getOriName());
            ori.add(String.valueOf(channel.getCntSubscribe())); //cnt subscribe, replace
            ori.add(String.valueOf(populateCntView(channel).getCntView()));
            ori.add(channel.getTag());
            ori.add(""); //ciratorProfile, curator id
            ori.add(""); //userName
            ori.add(""); //userIntro
            ori.add(""); //userImageUrl
            ori.add(sns == null ? "" : sns); //social network, ex: "twitter NBA;facebook ETtoday"
            ori.add(banner == null ? "" : banner); //banner image url
            if (ctx.getVer() == 32)
                ori.add(" ");
            else
                ori.add(lastEpisodeTitle); //lastEpisodeTitle
            if (ctx.getVer() > 32)
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
            lineup.setId(channel.getId());
            lineup.setName(name);
            lineup.setDescription(channel.getPlayerIntro());
            lineup.setThumbnail(imageUrl); //c.getPlayerPrefImageUrl());                        
            lineup.setNumberOfEpisode(channel.getCntEpisode());
            lineup.setType(channel.getType());
            lineup.setStatus(channel.getStatus());
            lineup.setContentType(channel.getContentType());
            lineup.setChannelSource(channel.getPlayerPrefSource());
            lineup.setLastUpdateTime(channel.getUpdateDate().getTime());
            lineup.setSorting(getPlayerDefaultSorting(channel)); //use default sorting for all
            lineup.setPiwikId(""); // piwik
            lineup.setRecentlyWatchedPrograms(""); //recently watched program
            lineup.setYoutubeName(channel.getOriName());
            lineup.setNumberOfSubscribers(channel.getCntSubscribe()); //cnt subscribe, replace
            lineup.setNumberOfViews(populateCntView(channel).getCntView());
            lineup.setTags(channel.getTag());
            lineup.setCuratorProfile(""); //ciratorProfile, curator id
            lineup.setCuratorName(""); //userName
            lineup.setCuratorDescription(""); //userIntro
            lineup.setCuratorThumbnail(""); //userImageUrl
            lineup.setSubscriberProfiles(""); //subscriberProfile, used to be subscriber profile urls, will be removed
            lineup.setSubscriberThumbnails(""); //subscriberImage, used to be subscriber image urls                
            log.info("set channelLineup cahce for cacheKey: " + cacheKey);
            CacheFactory.set(cacheKey, lineup);
            return lineup;
        }
    }
    
    public void populateCntItem(NnChannel channel) {
        
        if (channel == null) return;
        
        String cacheKey = CacheFactory.getChannelCntItemKey(channel.getId());
        Short cntItem = (Short) CacheFactory.get(cacheKey);
        if (cntItem != null) {
            
            channel.setCntItem(cntItem);
            return;
        }
        cntItem = (short) NNF.getItemMngr().findByChannelId(channel.getId()).size();
        channel.setCntItem(cntItem);
        CacheFactory.set(cacheKey, cntItem);
    }
    
    public NnChannel populateCntView(NnChannel channel) {
        
        String cacheName = "u_ch" + channel.getId();
        try {
            String result = (String) CacheFactory.get(cacheName);
            if (result != null) {
                channel.setCntView(Integer.parseInt(result));
                return channel;
            }
            channel.setCntView(CounterFactory.getCount(cacheName));
            log.info("cntView = " + channel.getCntView());
        } catch (Exception e) {
            NnLogUtil.logException(e);
            channel.setCntView(0);
        }
        CacheFactory.set(cacheName, String.valueOf(channel.getCntView()));
        return channel;
    }
    
    public void populateSocialFeeds(NnChannel channel) {
        
        if (channel == null) return;
        
        NnChannelPref channelPref = NNF.getChPrefMngr().getByChannelIdAndItem(channel.getId(), NnChannelPref.SOCIAL_FEEDS);
        if (channelPref != null)
            channel.setSocialFeeds(channelPref.getValue());
    }
    
    public void populateSocialFeeds(long channelId, String socialFeeds) {
        
        NnChannelPrefManager prefMngr = NNF.getChPrefMngr();
        NnChannelPref pref = prefMngr.findByChannelIdAndItem(channelId, NnChannelPref.SOCIAL_FEEDS);
        if (pref == null)
            pref = new NnChannelPref(channelId, NnChannelPref.SOCIAL_FEEDS, socialFeeds);
        else
            pref.setValue(socialFeeds);
        prefMngr.save(pref);
        // clean cache
        CacheFactory.delete(CacheFactory.getNnChannelPrefKey(channelId, NnChannelPref.SOCIAL_FEEDS));
    }
    
    public void populateBannerImageUrl(NnChannel channel) {
        
        if (channel == null) return;
        
        NnChannelPref pref = NNF.getChPrefMngr().getByChannelIdAndItem(channel.getId(), NnChannelPref.BANNER_IMAGE);
        if (pref != null)
            channel.setBannerImageUrl(pref.getValue());
    }
    
    public void populateBannerImageUrl(long channelId, String bannerImage) {
        
        NnChannelPrefManager prefMngr = NNF.getChPrefMngr();
        NnChannelPref pref = prefMngr.findByChannelIdAndItem(channelId, NnChannelPref.BANNER_IMAGE);
        if (pref == null)
            pref = new NnChannelPref(channelId, NnChannelPref.BANNER_IMAGE, bannerImage);
        else
            pref.setValue(bannerImage);
        prefMngr.save(pref);
        // clean cache
        CacheFactory.delete(CacheFactory.getNnChannelPrefKey(channelId, NnChannelPref.BANNER_IMAGE));
    }
    
    public void populateAutoSync(NnChannel channel) {
        
        if (channel == null) return;
        
        NnChannelPref channelPref = NNF.getChPrefMngr().findByChannelIdAndItem(channel.getId(), NnChannelPref.AUTO_SYNC);
        if (channelPref == null)
            channel.setAutoSync(NnChannelPref.OFF);
        else
            channel.setAutoSync(channelPref.getValue());
    }
    
    public void populateAutoSync(long channelId, String autoSync) {
        
        NnChannelPrefManager prefMngr = NNF.getChPrefMngr();
        NnChannelPref pref = prefMngr.findByChannelIdAndItem(channelId, NnChannelPref.AUTO_SYNC);
        if (pref == null)
            pref = new NnChannelPref(channelId, NnChannelPref.AUTO_SYNC, NnChannelPref.OFF);
        if (autoSync.equals(pref.getValue()) == false) {
            pref.setValue(autoSync);
            prefMngr.save(pref);
        }
    }
    
    public int calculateUserChannels(NnUser user) {
        
        return NNF.getChannelMngr().total("contentType != " + NnChannel.CONTENTTYPE_FAVORITE + 
                                          " && userIdStr == " + NnStringUtil.escapedQuote(user.getIdStr()));
    }
    
    public static NnChannel syncNow(NnChannel channel) {
        
        if (channel.isReadonly()) {
            
            String msg = "channel is readonly";
            log.warning(msg);
            
            return channel.setNote(msg);
            
        } else if (channel.getSourceUrl() == null || channel.getSourceUrl().isEmpty()) {
            
            String msg = "sourceUrl is empty";
            log.warning(msg);
            
            return channel.setNote(msg);
        }
        
        NNF.getChannelMngr().save(channel.setReadonly(true));
        
        long before = NnDateUtil.timestamp();
        
        Map<String, String> obj = new HashMap<String, String>();
        obj.put("id",          channel.getIdStr());
        obj.put("sourceUrl",   channel.getSourceUrl());
        obj.put("contentType", String.valueOf(channel.getContentType()));
        obj.put("isRealtime",  "true");
        
        String response = NnNetUtil.urlPostWithJson("http://" + MsoConfigManager.getCrawlerDomain() + "/ytcrawler/crawlerAPI.php", obj);
        
        if (response != null && response.trim().equalsIgnoreCase("Ack")) {
            
            log.info("crawlerAPI return " + response);
            channel.setNote("OK");
            
        } else {
            
            String msg = "crawlerAPI return NOT OK!";
            log.warning(msg);
            
            NNF.getChannelMngr().save(channel.setReadonly(false));
            
            channel.setNote(msg);
        }
        
        log.info(String.format("crawlerAPI costs %d milliseconds", NnDateUtil.timestamp() - before));
        
        return channel;
    }
}
