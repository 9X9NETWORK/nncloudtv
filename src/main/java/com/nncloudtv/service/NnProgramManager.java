package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnProgramDao;
import com.nncloudtv.dao.TitleCardDao;
import com.nncloudtv.dao.YtProgramDao;
import com.nncloudtv.exception.NotPurchasedException;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.CookieHelper;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnEpisode;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.PoiEvent;
import com.nncloudtv.model.PoiPoint;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.model.TitleCard;
import com.nncloudtv.model.YtProgram;
import com.nncloudtv.web.api.ApiContext;
import com.nncloudtv.web.json.player.PlayerPoi;
import com.nncloudtv.web.json.player.PlayerTitleCard;
import com.nncloudtv.web.json.player.ProgramInfo;
import com.nncloudtv.web.json.player.SubEpisode;

@Service
public class NnProgramManager {
    
    protected static final Logger log = Logger.getLogger(NnProgramManager.class
                                              .getName());
    
    private NnProgramDao dao = NNF.getProgramDao();
    
    public NnProgram create(NnEpisode episode, NnProgram program) {
        
        program = save(program);
        
        // non-specified sub-position
        if (program.getSubSeqInt() == 0) {
            reorderEpisodePrograms(episode.getId());
        }
        
        return program;
    }
    
    public void create(NnChannel channel, NnProgram program) {        
        Date now = new Date();
        program.setCreateDate(now);
        program.setUpdateDate(now);
        program.setChannelId(channel.getId());
        dao.save(program);
        resetCache(channel.getId());
        
        //!!!!! clean plus "hook, auto share to facebook"
        //set channel count
        int count = channel.getCntEpisode() + 1;
        channel.setCntEpisode(count);
        NNF.getChannelMngr().save(channel);
        
        //if the channel's original programCount is zero, its count will not be in the category, adding it now.
        if (count == 1) {
            NNF.getDisplayMngr().addChannelCounter(channel);
        }
    } 
    
    /**
     * Save programs massively, and keep updateDate untouched
     * why? processCache() takes too much time when saving individually
     * 
     * @param programs
     * @return programs
     */
    public List<NnProgram> save(List<NnProgram> programs) {
        
        List<Long> channelIds = new ArrayList<Long>();
        
        for (NnProgram program : programs) {            
            Date now = new Date();
            if (program.getCreateDate() == null)
                program.setCreateDate(now);
            if (program.getUpdateDate() == null) {
                program.setUpdateDate(now);
            }            
            
            if (channelIds.indexOf(program.getChannelId()) < 0) {
                channelIds.add(program.getChannelId());
            }
        }
        
        programs = dao.saveAll(programs);
        
        log.info("channel count = " + channelIds.size());
        for (Long channelId : channelIds) {
            resetCache(channelId);
        }
        
        return programs;
    }
    
    public NnProgram save(NnProgram program) {
        
        if (program == null) {
            return program;
        }
        
        Date now = new Date();
        
        if (program.getCreateDate() == null)
            program.setCreateDate(now);
        
        program.setUpdateDate(now);
        program = dao.save(program);
        
        resetCache(program.getChannelId());
        
        return program;
    }
    
    public void delete(NnProgram program) {
        
        if (program == null) {
            return;
        }
        
        // delete titleCards
        TitleCardManager titleCardMngr = new TitleCardManager();
        List<TitleCard> titleCards = titleCardMngr.findByProgramId(program.getId());
        titleCardMngr.delete(titleCards);
        
        // delete poiPoints at program level
        List<PoiPoint> points = NNF.getPoiPointMngr().findByProgram(program.getId());
        if (points != null && points.size() > 0) {
            NNF.getPoiPointMngr().delete(points);
        }
        
        long cId = program.getChannelId();
        dao.delete(program);
        resetCache(cId);
    }
    
    public void delete(List<NnProgram> programs) {
        
        if (programs == null || programs.size() == 0) {
            return;
        }
        
        // delete titleCards, delete poiPoints at program level
        TitleCardManager titlecardMngr = new TitleCardManager();
        List<TitleCard> titlecards = null;
        List<TitleCard> titlecardDeleteList = new ArrayList<TitleCard>();
        List<PoiPoint> points = null;
        List<PoiPoint> pointDeleteList = new ArrayList<PoiPoint>();
        for (NnProgram program : programs) { // TODO : sql in loop is bad
            titlecards = null;
            points = null;
            
            titlecards = titlecardMngr.findByProgramId(program.getId());
            if (titlecards != null && titlecards.size() > 0) {
                titlecardDeleteList.addAll(titlecards);
            }
            
            points = NNF.getPoiPointMngr().findByProgram(program.getId());
            if (points != null && points.size() > 0) {
                pointDeleteList.addAll(points);
            }
        }
        titlecardMngr.delete(titlecardDeleteList);
        NNF.getPoiPointMngr().delete(pointDeleteList);
        
        List<Long> channelIds = new ArrayList<Long>();
        
        for (NnProgram program : programs) {
            
            if (channelIds.indexOf(program.getChannelId()) < 0) {
                channelIds.add(program.getChannelId());
            }
        }
        
        dao.deleteAll(programs);
        
        log.info("channel count = " + channelIds.size());
        for (Long channelId : channelIds) {
            resetCache(channelId);
        }
    }
    
    public NnProgram findByChannelAndStorageId(long channelId, String storageId) {
        return dao.findByChannelAndStorageId(channelId, storageId);
    }
    
    public NnProgram findByChannelAndFileUrl(long channelId, String fileUrl) {
        return dao.findByChannelAndFileUrl(channelId, fileUrl);
    }
    
    @SuppressWarnings("unchecked")
    public Object findLatestProgramInfoByChannels(List<NnChannel> channels, short format) {
        YtProgramDao ytDao = new YtProgramDao();
        String output = "";
        List<ProgramInfo> json = new ArrayList<ProgramInfo>();
        for (NnChannel channel : channels) {
            String cacheKey = CacheFactory.getLatestProgramInfoKey(channel.getId(), format);
            Object programInfo = CacheFactory.get(cacheKey);
            if (programInfo != null) {
                log.info("got programInfo from cache, channelId = " + channel.getId() + ", cacheKey = " + cacheKey);
                if (format == ApiContext.FORMAT_PLAIN) {
                    String programInfoStr = (String) programInfo;
                    if (!programInfoStr.isEmpty())
                        output += programInfoStr;
                } else {
                    List<ProgramInfo> info = (ArrayList<ProgramInfo>) programInfo;
                    if (info != null)
                        json.addAll(info);
                }
                continue;
            }
            if (channel.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL || 
                channel.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_PLAYLIST) {
                YtProgram yt = ytDao.findLatestByChannel(channel.getId());
                List<YtProgram> programs = new ArrayList<YtProgram>();
                if (yt != null) {
                    programs.add(yt);
                    log.info("find latest yt program:" + yt.getId());
                    programInfo = this.composeYtProgramInfo(channel, programs, format);
                }
            }
            if (channel.getContentType() == NnChannel.CONTENTTYPE_MIXED) {
                List<NnEpisode> episodes = NNF.getEpisodeMngr().findPlayerLatestEpisodes(channel.getId(), channel.getSorting());
                if (episodes.size() > 0) {
                    log.info("find latest episode id:" + episodes.get(0).getId());
                    List<NnProgram> programs = this.findByEpisodeId(episodes.get(0).getId());
                    programInfo = this.composeNnProgramInfo(channel, episodes, programs, format);
                }
            }
            if (programInfo != null) {
                log.info("save lastProgramInfo to cache, channelId = " + channel.getId() + ", cacheKey = " + cacheKey);
                if (format == ApiContext.FORMAT_PLAIN) {
                    output += (String)programInfo;
                    CacheFactory.set(cacheKey, programInfo);
                } else {
                    json.addAll((ArrayList<ProgramInfo>)programInfo);
                    CacheFactory.set(cacheKey, programInfo);
                }
            } else {
                log.info("save lastProgramInfo to cache, channelId = " + channel.getId() + ", though its empty");
                if (format == ApiContext.FORMAT_PLAIN) {
                    CacheFactory.set(cacheKey, ""); // save an empty string to prevent cache miss
                } else {
                    CacheFactory.set(cacheKey, null);
                }
            }
        }
        if (format == ApiContext.FORMAT_PLAIN) {
            return output;
        } else {
            return json;
        }
    }
    
    public static String getCntViewCacheName(long channelId, String programId) {
        return "ch" + String.valueOf(channelId) + "-ep" + programId;
    }
    
    /**
     * Get a position of an episode in a channel.
     * Works only for fixed sorting channel such as maplestage channel or 9x9 channel.
     *  
     * @param player program info string
     * @param programId program key
     * @return program id position
     */
    public int getEpisodeIndex(String input, String programId) {
        String[] lines = input.split("\n");
        int index = 0;
        for (int i=0; i<lines.length; i++) {
            String[] tabs = lines[i].split("\t");
            if (tabs[1].equals(programId)) {
                index = i+1;
                i = lines.length + 1;
            }
        }
        return index;
    }
    
    public short getContentType(NnProgram program) {
        if (program.getAudioFileUrl() != null)
            return NnProgram.CONTENTTYPE_RADIO;
        if (program.getFileUrl().contains("youtube.com"))
            return NnProgram.CONTENTTYPE_YOUTUBE;
        return NnProgram.CONTENTTYPE_MP4;
    }
    
    public YtProgram findYtProgramById(Long ytProgramId) {
        return NNF.getYtProgramDao().findById(ytProgramId);
    }
    
    public NnProgram findByStorageId(String storageId) {
        return dao.findByStorageId(storageId);
    }
    
    public NnProgram findById(long id) {
        NnProgram program = dao.findById(id);
        return program;
    }
    
    public List<NnProgram> findByYtVideoId(String videoId) {
        List<NnProgram> programs = dao.findByYtVideoId(videoId);
        return programs;
    }
    
    public List<NnProgram> findByChannelId(long channelId) {
        return dao.findByChannel(channelId);
    }
    
    public List<NnProgram> findByChannelIdAndSeq(long channelId, Short seq) {
        return dao.findByChannelAndSeq(channelId, NnStringUtil.seqToStr(seq));
    }
    
    public void resetCache(long channelId) {
        log.info("reset program info cache: " + channelId);
        //programInfo version 40, format json
        CacheFactory.delete(CacheFactory.getAllprogramInfoKeys(channelId, ApiContext.FORMAT_JSON));
        //programInfo, version 40, format text
        CacheFactory.delete(CacheFactory.getAllprogramInfoKeys(channelId, ApiContext.FORMAT_PLAIN));
        //programInfo, version 31
        CacheFactory.delete(CacheFactory.getProgramInfoKey(channelId,   0, 31, ApiContext.FORMAT_PLAIN));
        CacheFactory.delete(CacheFactory.getProgramInfoKey(channelId,   0, 32, ApiContext.FORMAT_PLAIN));
        //latestProgramInfo
        CacheFactory.delete(CacheFactory.getLatestProgramInfoKey(channelId, ApiContext.FORMAT_JSON));
        CacheFactory.delete(CacheFactory.getLatestProgramInfoKey(channelId, ApiContext.FORMAT_PLAIN));
        //channelLineup
        String cId = String.valueOf(channelId);
        CacheFactory.delete(CacheFactory.getChannelLineupKey(cId, 32, ApiContext.FORMAT_PLAIN));
        CacheFactory.delete(CacheFactory.getChannelLineupKey(cId, 40, ApiContext.FORMAT_PLAIN));
        CacheFactory.delete(CacheFactory.getChannelLineupKey(cId, 40, ApiContext.FORMAT_JSON));
    }           
    
    public int total() {
        return dao.total();
    }
    
    public int total(String filter) {
        return dao.total(filter);
    }
    
    public List<NnProgram> list(int page, int limit, String sidx, String sord) {
        return dao.list(page, limit, sidx, sord);
    }
    
    public List<NnProgram> list(int page, int limit, String sidx, String sord, String filter) {
        return dao.list(page, limit, sidx, sord, filter);
    }
    
    public List<NnProgram> findByEpisodeId(long episodeId) {
        
        if (episodeId == 0) {
            return new ArrayList<NnProgram>();
        }
        
        return dao.findProgramsByEpisode(episodeId); // sorted already
    }
    
    public void reorderEpisodePrograms(long episodeId) {
        
        List<NnProgram> programs = findByEpisodeId(episodeId);
        
        Collections.sort(programs, getProgramComparator("subSeq"));
        
        log.info("programs.size() = " + programs.size());
        
        for (int i = 0; i < programs.size(); i++) {
            programs.get(i).setSubSeq(i + 1);
        }
        
        save(programs);
        
    }
    
    public Comparator<NnProgram> getProgramComparator(String sort) {
        if (sort.equals("updateDate")) {
            class ProgramComparator implements Comparator<NnProgram> {
                public int compare(NnProgram program1, NnProgram program2) {
                    Date d1 = program1.getUpdateDate();
                    Date d2 = program2.getUpdateDate();
                    return d2.compareTo(d1);
                }
            }
            return new ProgramComparator();            
        }
        if (sort.equals("subSeq")) {
            class ProgramComparator implements Comparator<NnProgram> {
                public int compare(NnProgram program1, NnProgram program2) {
                    int subSeq1 = program1.getSubSeqInt();
                    int subSeq2 = program2.getSubSeqInt();
                    return (subSeq1 - subSeq2);
                }
            }
            return new ProgramComparator();
        }
        //default, subSeq
        class NnProgramSeqComparator implements Comparator<NnProgram> {            
            public int compare(NnProgram program1, NnProgram program2) {
                int subSeq1 = program1.getSubSeqInt();
                int subSeq2 = program2.getSubSeqInt();                
                return (subSeq1 - subSeq2);
            }
        }
        return new NnProgramSeqComparator();
    }
    
    public List<NnProgram> getUserFavorites(NnUser user) {
    
        List<NnProgram> empty = new ArrayList<NnProgram>();
        
        List<NnChannel> channelFavorites = NNF.getChannelMngr().findByUser(user, 0, false);
        
        for (NnChannel channel : channelFavorites) {
            
            if (channel.getContentType() == NnChannel.CONTENTTYPE_FAVORITE) {
                
                List<NnProgram> favorites = findByChannelId(channel.getId());
                log.info("favorites count = " + favorites.size());
                return favorites;
            }
        }
        
        log.info("no favorite channel");
        return empty;
    }
    
    public Object findPlayerProgramInfoByEpisode(NnEpisode episode, NnChannel channel, short format) {
        
        return assembleProgramInfo(episode, channel, format);
    }
    
    //player programInfo entry
    //don't cache dayparting for now. dayparting means content type = CONTENTTYPE_DAYPARTING_MASK
    public Object findPlayerProgramInfoByChannel(long channelId, int start, int end, short time, ApiContext ctx) throws NotPurchasedException {
        
        NnChannel channel = NNF.getChannelMngr().findById(channelId);
        if (channel == null) { return ""; }
        
        // paid channel check
        if (channel.isPaidChannel()) {
            
            String errMsg = "paid channel";
            String userToken = ctx.getCookie(CookieHelper.USER);
            if (userToken == null) {
                
                throw new NotPurchasedException(errMsg);
            }
            NnUser user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
            if (user == null) {
                
                throw new NotPurchasedException(errMsg);
            }
            
            if (NNF.getPurchaseMngr().isPurchased(user, channel) == false) {
                
                throw new NotPurchasedException(errMsg);
            }
        }
        
        //don't cache dayparting for now
        log.info("time:" + time);
        String cacheKey = null;
        if (channel.getContentType() != NnChannel.CONTENTTYPE_DAYPARTING_MASK) {
            
            cacheKey = CacheFactory.getProgramInfoKey(channelId, start, ctx.getVersion(), ctx.getFormat());
            if (start < PlayerApiService.MAX_EPISODES) { // cache only if the start is less then 200
                try {
                    String result = (String) CacheFactory.get(cacheKey);
                    if (result != null) {
                        log.info("cached programInfo, channelId = " + cacheKey);
                        return result;
                    }
                } catch (Exception e) {
                    log.info("memcache error");
                }
            }
        }
        Object output = this.assembleProgramInfo(channel, ctx.getFormat(), start, end, time, ctx.getMso());
        if (start < PlayerApiService.MAX_EPISODES) { // cache only if the start is less than 200
            if (cacheKey != null) {
                log.info("store programInfo, key = " + cacheKey);
                CacheFactory.set(cacheKey, output);
            }
        }
        return output;
    }
    
    //find "good" programs, to find nnchannel type of programs, use findPlayerNnProgramsByChannel
    public List<NnProgram> findPlayerProgramsByChannel(long channelId) {
        
        List<NnProgram> programs = new ArrayList<NnProgram>();
        NnChannel channel = NNF.getChannelMngr().findById(channelId);
        if (channel == null)
            return programs;
        programs = dao.findPlayerProgramsByChannel(channel); //sort by seq and subSeq
        return programs;
    }
        
    //find player programs through nnepisode
    public List<NnProgram> findPlayerNnProgramsByChannel(long channelId) {
        
        return dao.findPlayerNnProgramsByChannel(channelId); //sort by episode seq and nnprogram subSeq
    }
    
    //for favorite 9x9 program, find the referenced data
    //return value: obj[0] = NnEpisode; obj[1] = List<NnProgram>
    public Object[] findFavoriteReferenced(String storageId) {
        
        Object[] obj = new Object[2];
        List<NnProgram> programs = new ArrayList<NnProgram>();
        if (storageId == null)
            return null;
        storageId = storageId.replace("e", "");
        NnEpisode episode = NNF.getEpisodeMngr().findById(Long.parseLong(storageId));
        if (episode == null) { return null; }
        if (episode.isPublic() == true) {
            //TODO and some other conditions?
            programs = dao.findProgramsByEpisode(Long.parseLong(storageId));
            obj[0] = episode;
            obj[1] = programs;
            log.info("find favorite reference's real programs size:" + programs.size());
        }
        return obj;
    }
    
    public Object assembleProgramInfo(NnEpisode episode, NnChannel channel, short format) {
        
        List<NnEpisode> episodes = new ArrayList<NnEpisode>();
        episodes.add(episode);
        List<NnProgram> programs = findByEpisodeId(episode.getId());
        
        return composeNnProgramInfo(channel, episodes, programs, format);
    }
    
    //based on channel type, assemble programInfo string
    public Object assembleProgramInfo(NnChannel channel, short format, int start, int end, short time, Mso mso) {
        
        if (channel.getContentType() == NnChannel.CONTENTTYPE_MIXED || channel.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_LIVE ) {
            
            List<NnEpisode> episodes = NNF.getEpisodeMngr().findPlayerEpisodes(channel.getId(), channel.getSorting(), start, end);
            
            List<NnProgram> programs = this.findPlayerNnProgramsByChannel(channel.getId());
            
            return composeNnProgramInfo(channel, episodes, programs, format);
            
        } else if (channel.getContentType() == NnChannel.CONTENTTYPE_DAYPARTING_MASK) {
            
            SysTagDisplay dayparting = NNF.getDisplayMngr().findDayparting(time, channel.getLang(), mso.getId());
            List<YtProgram> ytprograms = new ArrayList<YtProgram>();
            if (dayparting != null) {
                
                log.info("dayparting:" + dayparting.getName());
                long msoId = 0;
                if (mso != null) {
                    msoId = mso.getId();
                }
                List<NnChannel> daypartingChannels = NNF.getSysTagMngr().findDaypartingChannelsById(dayparting.getSystagId(), channel.getLang(), msoId, time);
                
                return NNF.getYtProgramMngr().findByDaypartingChannels(daypartingChannels, channel, mso.getId(), time, channel.getLang());
            }
            
            return composeYtProgramInfo(channel, ytprograms, format);
            
        } else if (channel.getContentType() == NnChannel.CONTENTTYPE_TRENDING) {
            
            List<NnChannel> channels = new ArrayList<NnChannel>();
            channels.add(channel);
            List<YtProgram> ytprograms =  new YtProgramDao().findByChannels(channels);
            
            return composeYtProgramInfo(channel, ytprograms, format);
            
        } else if (channel.getContentType() == NnChannel.CONTENTTYPE_VIRTUAL_CHANNEL1) {
            
            List<NnEpisode> classics = new ArrayList<NnEpisode>();
            List<NnEpisode> episodes = new ArrayList<NnEpisode>();
            List<NnProgram> programs = new ArrayList<NnProgram>();
            Long categoryId = Long.parseLong(channel.getSourceUrl());
            
            if (categoryId != null && start < PlayerApiService.PAGING_ROWS) {
                
                List<NnChannel> channels = NNF.getCategoryService().getChannels(categoryId);
                for (NnChannel ch : channels) {
                    if (ch.isAlwaysOnTop()) { // classic channel
                        
                        List<NnEpisode> candidates = NNF.getEpisodeMngr().findPlayerLatestEpisodes(ch.getId(), ch.getSorting());
                        if (candidates.size() > 0) {
                            NnEpisode candidate = candidates.get(0);
                            classics.add(candidate);
                            
                            if (candidate.getUpdateDate().getTime() < NnDateUtil.timestamp() - 80000000) {
                                
                                candidate.setSeq(ch.getCntEpisode() + PlayerApiService.MAX_EPISODES);
                                log.info("rotate classics channelId = " + ch.getId() + ", cntEpisode = " + ch.getCntEpisode());
                                NNF.getEpisodeMngr().save(candidate);
                                NNF.getEpisodeMngr().reorderChannelEpisodes(ch.getId());
                            }
                        }
                    } else {
                        
                        episodes.addAll(NNF.getEpisodeMngr().findPlayerLatestEpisodes(ch.getId(), ch.getSorting()));
                    }
                }
                
                log.info("virtual_channel1 channels = " + channels.size() +
                                        ", episodes = " + episodes.size() +
                                        ", classics = " + classics.size());
                
                Collections.sort(episodes, NnEpisodeManager.getComparator("publishDate"));
                if (episodes.size() > (PlayerApiService.PAGING_ROWS)) {
                    episodes = episodes.subList(0, PlayerApiService.PAGING_ROWS);
                }
                Random rand = new Random(NnDateUtil.timestamp());
                for (NnEpisode ep : classics) {
                    episodes.add(rand.nextInt(episodes.size()) + 1, ep);
                }
                log.info("episodes = " + episodes.size());
                programs = NNF.getProgramMngr().findByEipsodes(episodes);
            }
            log.info("programs = " + programs.size());
            
            return composeNnProgramInfo(channel, episodes, programs, format);
            
        } else if (channel.getContentType() == NnChannel.CONTENTTYPE_VIRTUAL_CHANNEL2) {
            
            List<NnEpisode> classics = new ArrayList<NnEpisode>();
            List<NnEpisode> episodes = new ArrayList<NnEpisode>();
            List<NnProgram> programs = new ArrayList<NnProgram>();
            Long categoryId = Long.parseLong(channel.getSourceUrl());
            
            if (categoryId != null && start < PlayerApiService.PAGING_ROWS) {
                
                List<NnChannel> channels = NNF.getCategoryService().getChannels(categoryId);
                for (NnChannel ch : channels) {
                    
                    List<NnEpisode> candidates = NNF.getEpisodeMngr().findPlayerLatestEpisodes(ch.getId(), ch.getSorting());
                    if (candidates.size() > 0) {
                        NnEpisode candidate = candidates.get(0);
                        if (ch.isAlwaysOnTop()) {
                            
                            classics.add(candidate);
                            if (candidate.getUpdateDate().getTime() < NnDateUtil.timestamp() - 80000000) {
                                
                                candidate.setSeq(ch.getCntEpisode() + PlayerApiService.MAX_EPISODES);
                                log.info("rotate classics channelId = " + ch.getId() + ", cntEpisode = " + ch.getCntEpisode());
                                NNF.getEpisodeMngr().save(candidate);
                                NNF.getEpisodeMngr().reorderChannelEpisodes(ch.getId());
                            }
                        } else {
                            
                            episodes.add(candidate);
                        }
                    }
                }
                
                log.info("virtual_channel2 channels = " + channels.size() +
                                        ", episodes = " + episodes.size() +
                                        ", classics = " + classics.size());
                
                Collections.sort(episodes, NnEpisodeManager.getComparator("publishDate"));
                if (episodes.size() > PlayerApiService.PAGING_ROWS) {
                    episodes = episodes.subList(0, PlayerApiService.PAGING_ROWS);
                }
                Random rand = new Random(NnDateUtil.timestamp());
                for (NnEpisode ep : classics) {
                    episodes.add(rand.nextInt(episodes.size()) + 1, ep);
                }
                log.info("episodes = " + episodes.size());
                programs = NNF.getProgramMngr().findByEipsodes(episodes);
            }
            log.info("programs = " + programs.size());
            
            return composeNnProgramInfo(channel, episodes, programs, format);
            
        } else {
            
            List<NnProgram> programs = this.findPlayerProgramsByChannel(channel.getId());
            log.info("channel id:" + channel.getId() + "; program size:" + programs.size());
            
            return composeProgramInfo(channel, programs, format);
        }
    }
    
    private List<NnProgram> findByEipsodes(List<NnEpisode> episodes) {
        
        List<NnProgram> programs = new ArrayList<NnProgram>();
        
        for (NnEpisode episode : episodes) {
            
            programs.addAll(dao.findAllByEpisodeId(episode.getId()));
        }
        
        return programs;
    }
    
    //provide cache
    public Object findYtProgramInfoByChannel(NnChannel channel, List<YtProgram> programs, short format) {
        
        if (programs.size() == 0) {
            if (format == ApiContext.FORMAT_JSON) {
                return null;
            } else {
                return "";
            }
        }
        String cacheKey = CacheFactory.getYtProgramInfoKey(channel.getId());
        Object programInfo = CacheFactory.get(cacheKey);
        if (programInfo != null) {
        	return programInfo;
        }
        String result = ""; 
        for (YtProgram p : programs) {
            result += (String)this.composeEachYtProgramInfo(channel, p, format);
        }
        return result;
    }
    
    public Object composeYtProgramInfo(NnChannel channel, List<YtProgram> programs, short format) {
        
        if (programs.size() == 0) {
            if (format == ApiContext.FORMAT_JSON) {
                return null;
            } else {
                return "";
            }
        }
        String result = "";
        List<ProgramInfo> json = new ArrayList<ProgramInfo>();
        for (YtProgram program : programs) {
            if (format == ApiContext.FORMAT_PLAIN)
                result += (String)this.composeEachYtProgramInfo(channel, program, format);
            else {
                json.add((ProgramInfo)this.composeEachYtProgramInfo(channel, program, format));
            }
        }
        if (format == ApiContext.FORMAT_PLAIN)
            return result;
        else
            return json;
    }
    
    //compose programInfo for non 9x9 and non favorite channel
    //favorite channel is not really supported
    public Object composeProgramInfo(NnChannel channel, List<NnProgram> programs, short format) {
        
        if (programs.size() == 0) { return ""; }
        
        String result = "";
        List<ProgramInfo> jsonResult = new ArrayList<ProgramInfo>();
        if (channel == null) { return null; }
        if (channel.getContentType() != NnChannel.CONTENTTYPE_MIXED &&
            channel.getContentType() != NnChannel.CONTENTTYPE_FAVORITE) {
            
            if (format == ApiContext.FORMAT_PLAIN) {
                
                for (NnProgram program : programs) {
                    result += this.composeEachProgramInfo(program, format);
                }
                return result;
                
            } else {
                
                for (NnProgram program : programs) {
                    jsonResult.add((ProgramInfo) this.composeEachProgramInfo(program, format));
                }
                return jsonResult;
            }
        }
        //do not have json support
        if (channel.getContentType() == NnChannel.CONTENTTYPE_FAVORITE) {
            
            for (NnProgram pprogram : programs) {
                
                if (pprogram.getContentType() == NnProgram.CONTENTTYPE_REFERENCE) {
                    
                    Object[] obj = this.findFavoriteReferenced(pprogram.getStorageId());
                    if (obj != null) {
                        
                        List<NnEpisode> epList = new ArrayList<NnEpisode>();
                        epList.add((NnEpisode)obj[0]);
                        @SuppressWarnings("unchecked")
                        List<NnProgram> referencePrograms = (List<NnProgram>)obj[1];
                        log.info("reference program size:" + referencePrograms.size());
                        if (format == ApiContext.FORMAT_PLAIN) {
                            
                            String favoriteStr = (String)this.composeNnProgramInfo(channel, epList, referencePrograms, format);
                            String[] lines = favoriteStr.split("\n");
                            for (String line : lines) {
                                
                                //replace with favorite's own channel id and program id
                                Pattern pattern = Pattern.compile("\t.*?\t");
                                Matcher m = pattern.matcher(line);
                                if (m.find()) {
                                    line = m.replaceFirst("\t" + String.valueOf(pprogram.getId()) + "\t");
                                }
                                pattern = Pattern.compile(".*?\t");
                                m = pattern.matcher(line);
                                if (m.find()) {
                                    line = m.replaceFirst(pprogram.getChannelId() + "\t");
                                }                        
                                result += line + "\n";
                            }
                        } else {
                            //not supported
                        }
                    }
                } else {
                    
                    if (format == ApiContext.FORMAT_PLAIN) {
                        
                        result += this.composeEachProgramInfo(pprogram, format);
                        
                    } else {
                        
                        jsonResult.add((ProgramInfo)this.composeEachProgramInfo(pprogram, format));
                    }
                }
            }
        }
        if (format == ApiContext.FORMAT_PLAIN) {
            return result;
        } else {
            return jsonResult;
        }
    }
    
    //compose nnchannel programs and favorite channels, otherwise use composeProgramInfo
    //format == json won't go to composeEachEpisodeInfo
    public Object composeNnProgramInfo(
            NnChannel channel, List<NnEpisode> episodes,
            List<NnProgram> programs, short format) {
        
        if (episodes.size() == 0 || programs.size() == 0) {
            if (format == ApiContext.FORMAT_PLAIN) {
                return "";
            } else {
                return null;
            }
        }
        String result = "";
        Map<Long, List<NnProgram>> map = new TreeMap<Long, List<NnProgram>>();
        for (NnProgram program : programs) {
            List<NnProgram> list = map.get(program.getEpisodeId());
            list = (list == null) ? new ArrayList<NnProgram>() : list;
            list.add(program);
            map.put(program.getEpisodeId(), list);
        }      
        //title card in map, nnprogram retrieves title card based on program id and type 
        List<TitleCard> cards = new TitleCardDao().findByChannel(channel.getId()); //order by channel id and program id
        HashMap<String, TitleCard> cardMap = new HashMap<String, TitleCard>();
        for (TitleCard card : cards) {
            String key = String.valueOf(card.getProgramId() + ";" + card.getType());
            cardMap.put(key, card);
        }
        
        //find all the programs, and find its event
        //episode number;text;link;start time;end time|episode number;link;text;start time;end time
        //List<Poi> pois = NNF.getPoiPointMngr().findByChannel(channel.getId()); //find all the programs        
        //List<PoiEvent> events = NNF.getPoiEventMngr().findByChannel(channel.getId());
        List<ProgramInfo> programInfos = new ArrayList<ProgramInfo>();
        for (NnEpisode episode : episodes) {
            List<NnProgram> list = (List<NnProgram>) map.get(episode.getId());
            if (list == null) {
                log.info("episode:" + episode.getId() + " have no programs");
            }
            if (list != null && list.size() > 0) {
                
                Collections.sort(list, getProgramComparator("subSeq"));
                String videoUrl      = "";
                String duration      = String.valueOf(episode.getDuration());
                String name          = getNotPipedProgramInfoData(episode.getName());
                String imageUrl      = episode.getImageUrl();
                String imageLargeUrl = episode.getImageUrl();
                String intro         = getNotPipedProgramInfoData(episode.getIntro());
                String card          = "";
                String contentType   = String.valueOf(episode.getContentType());
                int    iCounter      = 1;
                String poiStr        = "";
                ProgramInfo info = new ProgramInfo();
                if (imageUrl != null) {
                   if (imageUrl.equals("") || imageUrl.equals("|")) {
                      imageUrl = "https://s3-us-west-2.amazonaws.com/9x9pm1/blank.jpeg";
                   } 
                } else {
                      imageUrl = "https://s3-us-west-2.amazonaws.com/9x9pm1/blank.jpeg";
                }
                if (format == ApiContext.FORMAT_JSON) {
                    info.setId(String.valueOf(episode.getId()));
                    info.setChannelId(String.valueOf(episode.getChannelId()));
                    info.setDuration(duration);
                    info.setName(name);
                    info.setThumbnail(imageUrl);
                    info.setThumbnailLarge(imageLargeUrl);
                    info.setDescription(intro);
                    programInfos.add(info);
                }
                List<SubEpisode> subEpisodes = new ArrayList<SubEpisode>();
                for (NnProgram program : list) { //sub-episodes
                    
                    List<PlayerPoi>       playerPois       = new ArrayList<PlayerPoi>();
                    List<PlayerTitleCard> playerTitleCards = new ArrayList<PlayerTitleCard>();
                    List<PoiPoint>        points           = NNF.getPoiPointMngr().findCurrentByProgram(program.getId());
                    
                    log.info("points size:" + points.size());
                    
                    List<PoiEvent> events = new ArrayList<PoiEvent>();
                    for (PoiPoint point : points) {
                        PoiEvent event = NNF.getPoiEventMngr().findByPoint(point.getId());
                        events.add(event);
                    }
                    if (points.size() != events.size()) {
                        log.info("Bad!!! should not continue.");
                        points.clear();
                    }
                    for (int j = 0; j < points.size(); j++) {
                        
                        PoiPoint point = points.get(j);
                        PoiEvent event = events.get(j);
                        String context = NnStringUtil.urlencode(event.getContext());
                        if (format == ApiContext.FORMAT_PLAIN) {
                            
                            String poiStrHere = iCounter + ";" + point.getStartTime() + ";" + point.getEndTime() + ";" + event.getType() + ";" + context + "|";
                            log.info("poi output:" + poiStrHere);
                            poiStr += poiStrHere;
                            
                        } else {
                            
                            PlayerPoi playerPoi = new PlayerPoi();
                            playerPoi.setStartTime(point.getStartTime());
                            playerPoi.setEndTime(point.getEndTime());
                            playerPoi.setType(String.valueOf(event.getType()));
                            playerPoi.setContext(context);
                            playerPois.add(playerPoi);
                        }
                    }
                    String cardKey1 = String.valueOf(program.getId() + ";" + TitleCard.TYPE_BEGIN); 
                    String cardKey2 = String.valueOf(program.getId() + ";" + TitleCard.TYPE_END);
                    if (program.getSubSeq() != null && program.getSubSeq().length() > 0) {
                        
                        if (cardMap.containsKey(cardKey1) || cardMap.containsKey(cardKey2)) {
                            
                            String key = cardKey1;
                            if (cardMap.containsKey(cardKey2))
                                key = cardKey2;
                            String syntax = cardMap.get(key).getPlayerSyntax();
                            if (format == ApiContext.FORMAT_PLAIN) {
                                card += "subepisode" + "%3A%20" + iCounter + "%0A";
                                card += syntax + "%0A--%0A";
                            } else {
                                PlayerTitleCard titleCard = new PlayerTitleCard();
                                titleCard.setKey(String.valueOf(iCounter));
                                titleCard.setSyntax(syntax);
                                playerTitleCards.add(titleCard);
                            }
                            cardMap.remove(key);
                        }
                    }
                    if (program.getStartTime() != null && program.getStartTime().equals("0") &&
                        program.getEndTime() != null && program.getEndTime().equals("0")) {
                        if (program.getDuration() != null && !program.getDuration().equals("0")) {
                            program.setStartTime("0");
                            program.setEndTime(program.getDuration());
                        } else {
                            program.setStartTime("");
                            program.setEndTime("");
                        }
                    }
                    String fileUr1 = program.getFileUrl();
                    if (program.getAudioFileUrl() != null) {
                        fileUr1 = program.getAudioFileUrl();
                    }
                    if (format == ApiContext.FORMAT_PLAIN) {
                        
                        //log.info("fileUrl1:" + fileUr1);
                        String d1      = (program.getStartTime() != null) ? ";" + program.getStartTime() : ";";
                        String d2      = (program.getEndTime() != null) ? ";" + program.getEndTime() : ";";
                        videoUrl      += "|" + fileUr1;
                        videoUrl      += d1;
                        videoUrl      += d2;
                        //log.info("video url :" + videoUrl);
                        name          += "|" + program.getPlayerName();
                        imageUrl      += "|" + program.getImageUrl();
                        imageLargeUrl += "|" + program.getImageLargeUrl();
                        intro         += "|" + program.getPlayerIntro();
                        duration      += "|" + program.getDurationInt();
                        contentType   += "|" + program.getContentType();
                        
                    } else {
                        
                        SubEpisode subEpisode = new SubEpisode();
                        subEpisode.setStartTime(program.getStartTime());
                        subEpisode.setEndTime(program.getEndTime());
                        subEpisode.setFileUrl(fileUr1);
                        subEpisode.setName(program.getPlayerName());
                        subEpisode.setThumbnail(program.getImageUrl());
                        subEpisode.setThumbnailLarge(program.getImageLargeUrl());
                        subEpisode.setDescription(program.getPlayerIntro());
                        subEpisode.setDuration(String.valueOf(program.getDurationInt()));
                        subEpisode.setContentType(String.valueOf(program.getContentType()));
                        subEpisode.setTitleCards(playerTitleCards);
                        subEpisode.setPois(playerPois);
                        subEpisodes.add(subEpisode);
                    }
                    iCounter++;
                }
                //////// start of episode magic \\\\\\\\
                // use referenced episode to rewrite current episode
                if (episode.getStorageId() > 0) {
                    NnEpisode reference = NNF.getEpisodeMngr().findById(episode.getStorageId());
                    if (reference != null) {
                        log.info("ep" + episode.getId() + " reference to ep" + reference.getId());
                        episode.setId(reference.getId());
                        episode.setChannelId(reference.getChannelId());
                        episode.setStorageId(0);
                    }
                }
                // if episode is come from another channel
                // temporarily use storageId to store foreign channel
                long real = episode.getChannelId();
                if (real != 0 && real != channel.getId()) {
                    episode.setStorageId(real);
                    episode.setChannelId(channel.getId());
                } else {
                    episode.setStorageId(0);
                }
                //////// end of episode magic \\\\\\\\
                if (format == ApiContext.FORMAT_PLAIN) {
                    poiStr = poiStr.replaceAll("\\|$", "");
                    result += composeEachEpisodeInfo(episode, name, intro, imageUrl, imageLargeUrl, videoUrl, duration, card, contentType, poiStr, format);
                } else {
                    info.setSubEpisodes(subEpisodes);
                }
            }
        }
        if (format == ApiContext.FORMAT_PLAIN) {
            return result;
        } else { 
            return programInfos;
        }
    }
    
    private String removePlayerUnwanted(String value) {
        
        if (value == null) return value;        
        value = value.replaceAll("\\s", " ");
        return value;
    }
    
    //TODO make all piped string coming through here, actually process all player string here
    private String getNotPipedProgramInfoData(String str) {
        
        if (str == null) return null;
        str = str.replaceAll("\\|", "\\\\|");
        return str;
    }
    
    public Object composeEachEpisodeInfo(
            NnEpisode episode, String name,          String intro,
            String imageUrl,   String imageLargeUrl, String videoUrl,
            String duration,   String card,          String contentType,
            String poiStr,     short format) {
        
        //zero file to play
        name = this.removePlayerUnwanted(name);
        intro = this.removePlayerUnwanted(intro);
        String output = "";
        
        // publishDate --> scheduleDate --> now()
        long publishTime = NnDateUtil.timestamp();
        if (episode.getPublishDate() != null) {
            publishTime = episode.getPublishDate().getTime();
        } else if (episode.getScheduleDate() != null) {
            publishTime = episode.getScheduleDate().getTime();
        }
        
        String cId = String.valueOf(episode.getChannelId());
        if (episode.getChannelId() == 0) { // orphan episode
            
            cId = String.valueOf(episode.getStorageId());
            
        } else if (episode.getStorageId() != 0) { // virtual channel
            
            cId += ":" + String.valueOf(episode.getStorageId());
        }
        String eId = "e" + String.valueOf(episode.getId());
        
        if (format == ApiContext.FORMAT_PLAIN) {
            
            String[] ori = {
                    
                    cId, 
                    eId, 
                    name, 
                    intro,
                    contentType,
                    duration,
                    imageUrl,
                    imageLargeUrl, //imageLargeUrl
                    videoUrl,
                    "",     //url2
                    "",     //url3
                    "",     //audio file
                    String.valueOf(publishTime), // or scheduleTime
                    String.valueOf(episode.isPublic()),
                    card,
                    poiStr
            };
            output = output + NnStringUtil.getDelimitedStr(ori);
            output = output.replaceAll("null", "") + "\n";
            
            return output;
            
        } else {
            
            ProgramInfo info = new ProgramInfo();
            info.setChannelId(cId);
            info.setId(eId);
            info.setName(name);
            info.setDescription(intro);
            info.setContentType(contentType);
            info.setDuration(duration);
            info.setThumbnail(imageUrl);
            info.setThumbnailLarge(imageLargeUrl);
            info.setFileUrl(videoUrl);
            info.setPublishTime(publishTime);
            
            return info;
        }
    }
    
    public Object composeEachProgramInfo(NnProgram program, short format) {
        
        String output     = "";
        String regexCache = "^(http|https)://(9x9cache.s3.amazonaws.com|s3.amazonaws.com/9x9cache)";
        String regexPod   = "^(http|https)://(9x9pod.s3.amazonaws.com|s3.amazonaws.com/9x9pod)";
        String cache      = "http://cache.9x9.tv";
        String pod        = "http://pod.9x9.tv";
        String url1       = program.getFileUrl();
        String imageUrl   = program.getImageUrl();
        
        if (url1 != null) {
            url1 = url1.replaceFirst(regexCache, cache);
            url1 = url1.replaceAll(regexPod, pod);
        }
        if (imageUrl != null) {
            imageUrl = imageUrl.replaceFirst(regexCache, cache);
            imageUrl = imageUrl.replaceAll(regexPod, pod);
        }
        if (program.getPublishDate() == null) {
            program.setPublishDate(new Date()); //should not happen, just in case
        }
        
        long   channelId    = program.getChannelId();
        long   pid          = program.getId();
        String name         = program.getName();
        String intro        = program.getIntro();
        short  contentType  = program.getContentType();
        String duration     = program.getDuration();
        String audioFileUrl = program.getAudioFileUrl();
        long   publishTime  = program.getPublishDate().getTime();
        String comment      = program.getComment();
        
        if (format == ApiContext.FORMAT_PLAIN) {
            
            String[] ori = {
                    
                    String.valueOf(channelId),
                    String.valueOf(pid), 
                    name, 
                    intro,
                    String.valueOf(contentType),
                    duration,
                    imageUrl,
                    "",
                    url1,   // video url
                    "",     // file type 2 
                    "",     // file type 3
                    audioFileUrl, //audio file
                    String.valueOf(publishTime),
                    comment,
                    ""      // card
            };
            output = output + NnStringUtil.getDelimitedStr(ori);
            output = output.replaceAll("null", "") + "\n";
            
            return output;
            
        } else {
            
            ProgramInfo json = new ProgramInfo();
            json.setChannelId(String.valueOf(channelId));
            json.setId(String.valueOf(pid));
            json.setName(name);
            json.setDescription(intro);
            json.setContentType(String.valueOf(contentType));
            json.setDuration(duration);
            json.setThumbnail(imageUrl);
            json.setFileUrl(url1);
            json.setAudioFileUrl(audioFileUrl);
            json.setPublishTime(publishTime);
            json.setComment(comment);
            
            return json;
        }
    }
    
    public Object composeEachYtProgramInfo(NnChannel channel, YtProgram ytProgram, short format) {
        
        String cIdStr = String.valueOf(ytProgram.getChannelId());
        if (channel != null && channel.getId() != ytProgram.getChannelId()) {
            
            cIdStr = channel.getId() + ":" + ytProgram.getChannelId(); 
        }
        
        String pId         = String.valueOf(ytProgram.getId());
        String name        = ytProgram.getPlayerName();
        String intro       = ytProgram.getPlayerIntro();
        short  contentType = NnProgram.CONTENTTYPE_YOUTUBE;
        String duration    = ytProgram.getDuration();
        String imageUrl    = ytProgram.getImageUrl();
        long   updateTime  = ytProgram.getUpdateDate().getTime();
        String url1        = "";
        
        if (ytProgram.getYtVideoId() != null) {
            
            url1 = "http://www.youtube.com/watch?v=" + ytProgram.getYtVideoId();
        }
        if (format == ApiContext.FORMAT_PLAIN) {
            
            String output = "";
            String[] ori = {
                    
                    cIdStr, 
                    pId, 
                    name, 
                    intro,
                    String.valueOf(contentType),
                    duration,
                    imageUrl,
                    "",
                    url1,   // video url
                    "",     // file type 2 
                    "",     // file type 3
                    "",     // audio file
                    String.valueOf(updateTime),
                    "",     // comment
                    ""      // crad
            };
            output = output + NnStringUtil.getDelimitedStr(ori);
            output = output.replaceAll("null", "") + "\n";
            
            return output;
            
        } else {
            
            ProgramInfo info = new ProgramInfo();
            info.setChannelId(cIdStr);
            info.setId(pId);
            info.setName(name);
            info.setContentType(String.valueOf(contentType));
            info.setDuration(duration);
            info.setThumbnail(imageUrl);
            info.setFileUrl(url1);
            info.setPublishTime(updateTime);
            
            return info;
        }
    }
}
