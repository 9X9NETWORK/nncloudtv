package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.SysTagDisplayDao;
import com.nncloudtv.dao.YtProgramDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.model.SysTagMap;
import com.nncloudtv.model.YtProgram;
import com.nncloudtv.web.json.player.CategoryInfo;
import com.nncloudtv.web.json.player.ChannelLineup;
import com.nncloudtv.web.json.player.PlayerSetInfo;
import com.nncloudtv.web.json.player.Portal;
import com.nncloudtv.web.json.player.ProgramInfo;
import com.nncloudtv.web.json.player.SetInfo;

@Service
public class SysTagDisplayManager {
    
    protected static final Logger log = Logger.getLogger(SysTagDisplayManager.class.getName());
    
    private SysTagDisplayDao dao = NNF.getDisplayDao();
    
    public List<SysTagDisplay> findPlayerCategories(String lang, long msoId) {
        return dao.findPlayerCategories(lang, msoId);       
    }

    public List<SysTagDisplay> findPlayerCategoriesAll(String lang, long msoId) {
        List<SysTagDisplay> categories = new ArrayList<SysTagDisplay>();
        if (msoId != 1) {            
            categories.addAll(this.findPlayerCategories(lang, msoId));
        }
        categories.addAll(this.findPlayerCategories(lang, 1));
        return dao.findPlayerCategories(lang, msoId);       
    }

    public List<SysTagDisplay> findRecommendedSets(String lang, long msoId) {
        List<SysTagDisplay> sets = dao.findRecommendedSets(lang, msoId, SysTag.TYPE_SET);
        log.info("recommended size:" + sets.size());        
        return sets;
    }

    public List<SysTagDisplay> find33RecommendedSets(String lang, long msoId) {
        List<SysTagDisplay> sets = dao.findRecommendedSets(lang, msoId, SysTag.TYPE_33SET);
        log.info("33 recommended size:" + sets.size());        
        return sets;
    }

    public SysTagDisplay findDayparting(short baseTime, String lang, long msoId) {
        List<SysTagDisplay> sets = dao.findDayparting(baseTime, lang, msoId);
        if (sets.size() > 0)
            return sets.get(0);            
        return null;
    } 

    public SysTagDisplay findByType(long msoId, short type, String lang) {
        List<SysTagDisplay> sets = dao.findByType(msoId, type, lang);
        if (sets.size() > 0)
            return sets.get(0);            
        return null;
    } 

    public SysTagDisplay findPrevious(long msoId, String lang, SysTagDisplay dayparting) {
        List<SysTagDisplay> display = dao.findByType(msoId, SysTag.TYPE_PREVIOUS, lang);
        if (display.size() > 0) {
            SysTag systag = NNF.getSysTagMngr().findById(dayparting.getSystagId());
            if (systag != null ) { 
                long systagId = Long.parseLong(systag.getAttr());
                display.get(0).setSystagId(systagId);
                SysTagDisplay previousDisplay = this.findBySysTagIdAndLang(systagId, lang);                
                display.get(0).setImageUrl(previousDisplay.getImageUrl());
                log.info("previous systag id set:" + systagId);
            }
            log.info("previous systag id:" + display.get(0).getSystagId());
            return display.get(0);
        }
        return null;
    } 
    
    public SysTagDisplay findById(long id) {
        return dao.findById(id);
    }
    
    public SysTagDisplay findBySysTagIdAndLang(Long sysTagId, String lang) {
        if (sysTagId == null) {
            return null;
        }
        return dao.findBySysTagIdAndLang(sysTagId, lang);
    }
    
    /** if multiple display, only pick one, for now using in Set */
    public SysTagDisplay findBySysTagId(Long sysTagId) {
        if (sysTagId == null) {
            return null;
        }
        return dao.findBySysTagId(sysTagId);
    }
    
    public List<SysTagDisplay> findAllBySysTagId(Long sysTagId) {
        if (sysTagId == null) {
            return new ArrayList<SysTagDisplay>();
        }
        return dao.findAllBySysTagId(sysTagId);
    }
    
    public SysTagDisplay findByName(String name, long msoId) {
        return dao.findByName(name);
    }
        
    public SysTagDisplay save(SysTagDisplay sysTagDisplay) {
        
        if (sysTagDisplay == null) {
            return null;
        }
        
        Date now = new Date();
        sysTagDisplay.setUpdateDate(now);
        
        sysTagDisplay = dao.save(sysTagDisplay);
        
        return sysTagDisplay;
    }
    
    public void delete(SysTagDisplay sysTagDisplay) {
        if (sysTagDisplay == null) {
            return ;
        }
        dao.delete(sysTagDisplay);
    }
    
    public void delete(List<SysTagDisplay> sysTagDisplays) {
        
        dao.deleteAll(sysTagDisplays);
    }
    
    public void addChannelCounter(NnChannel channel) {
        
    }
    
    @SuppressWarnings("unchecked")
    public Object getPlayerCategoryInfo(SysTagDisplay display, boolean programInfo, List<NnChannel> channels, long start, long limit, long total, int version, short format) {
        String id = String.valueOf(display.getId());
        String name = display.getName();        
        NnChannelManager chMngr = NNF.getChannelMngr();
        //在Store裡, "最新上架"就是"alwaysOnTop".            
        String latest = this.getTag(display.getSystagId(), "store");
        //1. category info                
        if (format == PlayerApiService.FORMAT_PLAIN) {
            List<String> result = new ArrayList<String>();            
            String categoryInfo = "";
            categoryInfo += PlayerApiService.assembleKeyValue("id", id);
            categoryInfo += PlayerApiService.assembleKeyValue("name", name);
            categoryInfo += PlayerApiService.assembleKeyValue("start", String.valueOf(start));        
            categoryInfo += PlayerApiService.assembleKeyValue("count", String.valueOf(limit));
            categoryInfo += PlayerApiService.assembleKeyValue("total", String.valueOf(total));
            categoryInfo += PlayerApiService.assembleKeyValue("channeltag", latest);
            result.add(categoryInfo);
            //2. category tag
            String tagInfo = "";
            String tags = display.getPopularTag();
            if (tags != null) {
                String[] tag = tags.split(",");
                for (String t : tag) {
                    tagInfo += t + "\n";
                }
            }
            result.add(tagInfo);
            // 3. channelInfo
            String channelInfo = (String) chMngr.composeChannelLineup(channels, version, format); 
            result.add(channelInfo);
            // 4. programInfo
            if (programInfo) {
                String programInfoStr = (String) NNF.getProgramMngr().findLatestProgramInfoByChannels(channels, format);
                result.add(programInfoStr);
            }
            String size[] = new String[result.size()];            
            return result.toArray(size);
        } else {
            CategoryInfo categoryInfo = new CategoryInfo();
            categoryInfo.setId(id);
            categoryInfo.setName(name);
            categoryInfo.setStart(start);
            categoryInfo.setCount(limit);
            categoryInfo.setTotal(total);
            String tags = display.getPopularTag();
            if (tags != null) {
                String[] tag = tags.split(",");
                for (String t : tag) {
                    categoryInfo.getTags().add(t);
                }
            }
            categoryInfo.setChannelLineup((List<ChannelLineup>) chMngr.composeChannelLineup(channels, version, format));
            categoryInfo.setProgramInfo((List<ProgramInfo>) NNF.getProgramMngr().findLatestProgramInfoByChannels(channels, format));
            return categoryInfo;
        }
    }

    /**
     * SysTag type TYPE_WHATSON. It maps to: 
     * 1. a dayparting channel: channel type 14:
     *    It is a "mask" channel. The real channels based on the "time" can only be found through:
     *      (1) Find the real dayparting systag based by time, language and mso: SysTagDisplay dayparting = displayMngr.findDayparting(time, lang, mso.getId());
     *      (2) Based on step 1, find channels: List<NnChannel> daypartingChannels = systagMngr.findDaypartingChannelsById(dayparting.getSystagId(), lang, msoId, time);
     *      <note 1> Its programs is from ytprograms.
     *      <note 2> mask channel id is used to tell player it's a mask channel. If users want to subscribe such channel, they go to the "real one".
     *         the channel id format is: "mask channel id:real channelid". Reference composeEachYtProgramInfo in NnProgramManager.
     * 2. two trending channels: 
     *    channel type 15. its program is from ytprogram. they are "hidden" channels meaning they are not meant to be searchable.
     *    those programs are crawled by ytwritter.py under ytcrawler project.
     * 3. cache:
     *    (1) findDaypartingChannelsById in SysTagManager caches list of NnChannels.
     *    (2) findByDaypartingChannels caches programInfo string based on dayparting channels.
     *    (3) cronjob on "channelwatch" machine wipes out the cache hourly by the python program, /var/www/ytcrawler/dayparting_cache.py
     */
    public Object getPlayerWhatson(String lang, short time, short format, Mso mso, boolean minimal, int version) {
        
        List<NnChannel> listingChannels = new ArrayList<NnChannel>();
        String programInfo = "";
        String channelInfo = "";
        //find whaton systag and its channels
        SysTagDisplay whatson = this.findByType(mso.getId(), SysTag.TYPE_WHATSON, lang);
        if (!minimal) {
	        NnChannel daypartingChannel = null;
	        if (whatson != null ) {
	            List<NnChannel> whatsonChannels = NNF.getSysTagMngr().findPlayerAllChannelsById(whatson.getSystagId(), lang, SysTag.SORT_SEQ, mso.getId());
	            listingChannels.addAll(whatsonChannels);
	            for (NnChannel c : whatsonChannels) {
	                if (c.getContentType() == NnChannel.CONTENTTYPE_DAYPARTING_MASK) {
	                    daypartingChannel = c;                    
	                } else {
	                    programInfo += (String) NNF.getYtProgramMngr().findByChannel(c);
	                }
	            }	            
	        }	        
	        //find the real dayparting channels
	        SysTagDisplay dayparting = this.findDayparting(time, lang, mso.getId());
	        if (dayparting != null) {
	            log.info("dayparting:" + dayparting.getName());
	            List<NnChannel> daypartingChannels = NNF.getSysTagMngr().findDaypartingChannelsById(dayparting.getSystagId(), lang, mso.getId(), time);
	            programInfo += (String) NNF.getYtProgramMngr().findByDaypartingChannels(daypartingChannels, daypartingChannel, mso.getId(), time, lang);
	        } else {
	            return new String[]{"", "", ""};
	        }
	        //temporarily fix the channel image issue. to give the dayparting mask channel 4 thumbnails
            if (listingChannels.get(0).getContentType() == NnChannel.CONTENTTYPE_DAYPARTING_MASK) {
	            String[] lines = programInfo.split("\n");
	            String imageUrl = "";
	            if (lines.length > 4) {
		            for (int i=0; i<4; i++) {
		            	String l = lines[i];
		                String[] data = l.split("\t");
		                imageUrl += "|" + data[6];
		            }
		            imageUrl = imageUrl.replaceFirst("\\|", "");
		            listingChannels.get(0).setImageUrl(imageUrl);
	            }
            }
	        channelInfo = (String)NNF.getChannelMngr().composeChannelLineup(listingChannels, version, PlayerApiService.FORMAT_PLAIN);
        }
        String setStr = "";
        String id = whatson.getId() + "-" + whatson.getSystagId();
        String name = whatson.getName();
        String intro = "";
        String imageUrl = whatson.getImageUrl();
        int cntChannel = whatson.getCntChannel();
        String imageUrl2 = whatson.getImageUrl2();
        String bannerImageUrl = whatson.getBannerImageUrl();
        String bannerImageUrl2 = whatson.getBannerImageUrl2();
        String[] obj = {
            id,
            name,
            intro, //description
            imageUrl,
            String.valueOf(cntChannel),
            imageUrl2,
            bannerImageUrl,
            bannerImageUrl2,
        };
        setStr += NnStringUtil.getDelimitedStr(obj) + "\n";
        String result[] = {setStr, channelInfo, programInfo};
        return result;        
    }
    
    //type: "store", "portal"
    private String getTag(long systagId, String type) {
        //在portal裡, "best"就是"alwaysOnTop", "hot"就是"featured".
        //在portal的 Program, 如果排序是根據 seq (手動排序) 的話，要把「BEST」標籤的資訊隱藏起來
        //在Store裡, best"就是"alwaysOnTop".
        String TYPE_STORE = "store";
        String TYPE_PORTAL = "portal";
        SysTag systag = NNF.getSysTagMngr().findById(systagId);
        if (systag == null)
            return "";
        List<SysTagMap> maps = NNF.getSysTagMapMngr().findBySysTagId(systagId);
        List<String> tags = new ArrayList<String>();
        String featured = "hot:";
        String alwaysOnTop = "best:";

        for (SysTagMap map : maps) {
            if (map.isFeatured() && type.equals(TYPE_PORTAL)) {                
                featured += map.getChannelId() + ",";
            }
            if (map.isAlwaysOnTop()) {
                if (type.equals(TYPE_STORE) || (type.equals(TYPE_PORTAL) && systag.getSorting() != SysTag.SORT_SEQ))
                    alwaysOnTop += map.getChannelId() + ",";
            }
        }
        featured = featured.replaceAll(",$", "");
        alwaysOnTop = alwaysOnTop.replaceAll(",$", "");
        if (featured.endsWith(":"))
            featured = "";
        if (alwaysOnTop.endsWith(":"))
            alwaysOnTop = "";
        tags.add(featured);
        tags.add(alwaysOnTop);
        String tagStr = "";     
        for (String tag : tags) {
            if (tag.length() > 0)
                tagStr += tag + ";";
        }
        tagStr = tagStr.replaceAll(";$", "");
        return tagStr;
    }
    
    @SuppressWarnings("unchecked")
    public Object getPlayerPortal(String lang, boolean minimal, int version, short format, Mso mso) {
        /*
        //1: list of sets, including dayparting         
        //The dayparting set is system set, always shows up
        Mso nnMso = msoMngr.findNNMso();
        SysTagDisplay dayparting = displayMngr.findDayparting(baseTime, lang, nnMso.getId());
        if (dayparting != null) {
            displays.add(dayparting);
        }
        SysTagDisplay previously = displayMngr.findPrevious(nnMso.getId(), lang, dayparting);
        if (previously != null) {
            displays.add(previously);
        }
        */
        List<SysTagDisplay> displays = this.findRecommendedSets(lang, mso.getId());
        String setStr = "";
        List<SetInfo> setInfo = new ArrayList<SetInfo>();
        for (SysTagDisplay display : displays) {
            String id = display.getId() + "-" + display.getSystagId();
            String name = display.getName();
            String intro = "";
            String imageUrl = display.getImageUrl();
            int cntChannel = display.getCntChannel();
            String imageUrl2 = display.getImageUrl2();
            String bannerImageUrl = display.getBannerImageUrl();
            String bannerImageUrl2 = display.getBannerImageUrl2();
            String channeltag = this.getTag(display.getSystagId(), "portal");
            if (format == PlayerApiService.FORMAT_PLAIN) {
                String[] obj = {
                    id,
                    name,
                    intro, //description
                    imageUrl,
                    String.valueOf(cntChannel),
                    imageUrl2,
                    bannerImageUrl,
                    bannerImageUrl2,
                    channeltag,
                };
                setStr += NnStringUtil.getDelimitedStr(obj) + "\n";
            } else {
                SetInfo info = new SetInfo();
                info.setId(id);
                info.setName(name);
                info.setThumbnail(imageUrl);
                info.setNumberOfChannels(cntChannel);
                info.setThumbnail2(imageUrl2);
                info.setBannerImageUrl(bannerImageUrl2);
                info.setBannerImageUrl2(bannerImageUrl2);
                setInfo.add(info);
            }
        }
        String channelStr = "";
        String programStr = "";
        List<ChannelLineup> channelLineup = new ArrayList<ChannelLineup>();
        List<ProgramInfo> programInfo = new ArrayList<ProgramInfo>();
        if (!minimal) {
            //2: list of channel's channelInfo of every set
            List<NnChannel> channels = new ArrayList<NnChannel>();
            if (displays.size() > 0) {
                SysTag systag = NNF.getSysTagMngr().findById(displays.get(0).getSystagId());
                short sort = SysTag.SORT_DATE;
                if (systag.getType() == SysTag.TYPE_SET) {
                    sort = systag.getSorting();
                }
                channels.addAll(NNF.getSysTagMngr().findPlayerChannelsById(displays.get(0).getSystagId(), lang, sort, 0));
            }
            if (format == PlayerApiService.FORMAT_PLAIN) {
                channelStr = (String)NNF.getChannelMngr().composeChannelLineup(channels, version, format);
            } else {
                channelLineup = (List<ChannelLineup>)NNF.getChannelMngr().composeChannelLineup(channels, version, format);
            }
            //3. list of the latest episode of each channel of the first set
            if (format == PlayerApiService.FORMAT_PLAIN) {
                programStr = (String) NNF.getProgramMngr().findLatestProgramInfoByChannels(channels, format);
            } else {
                programInfo = (List<ProgramInfo>) NNF.getProgramMngr().findLatestProgramInfoByChannels(channels, format);
            }
        }
        if (format == PlayerApiService.FORMAT_PLAIN) {
            if (minimal) {
                String result[] = {""};
                result[0] = setStr;
                return result;
            } else {
                String result[] = {"", "", ""};
                result[0] = setStr;            
                result[1] = channelStr;
                result[2] = programStr;
                return result;            
            }            
        } else {
            Portal portal = new Portal();
            portal.setSetInfo(setInfo);
            if (minimal) {
                return portal;
            } else {
                portal.setChannelLineup(channelLineup);
                portal.setProgramInfo(programInfo);
                return portal;
            }
        }         
    }       
    
    public Object getPlayerSetInfo(Mso mso, SysTag systag, SysTagDisplay display, List<NnChannel> channels, List<NnProgram>programs, int version, short format, short time, boolean isProgramInfo) {
        String name = mso.getName();
        String imageUrl = mso.getLogoUrl();
        String intro = mso.getIntro();
        String setId = String.valueOf(display.getId());
        String setName = display.getName();
        String setImageUrl = display.getImageUrl();
        String bannerImageUrl = display.getBannerImageUrl();
        String bannerImageUrl2 = display.getBannerImageUrl2();
        if (format == PlayerApiService.FORMAT_PLAIN) {
        String result[] = {"", "", "", ""};
        //mso info
        result[0] += PlayerApiService.assembleKeyValue("name", name);
        result[0] += PlayerApiService.assembleKeyValue("imageUrl", imageUrl); 
        result[0] += PlayerApiService.assembleKeyValue("intro", intro);            
        //set info
        result[1] += PlayerApiService.assembleKeyValue("id", setId);
        result[1] += PlayerApiService.assembleKeyValue("name", setName);
        result[1] += PlayerApiService.assembleKeyValue("imageUrl", setImageUrl);
        result[1] += PlayerApiService.assembleKeyValue("bannerImageUrl", bannerImageUrl);
        result[1] += PlayerApiService.assembleKeyValue("bannerImageUrl2", bannerImageUrl2);
        result[1] += PlayerApiService.assembleKeyValue("channeltag", this.getTag(display.getSystagId(), "portal"));
        //channel info
        result[2] = (String) NNF.getChannelMngr().composeChannelLineup(channels, version, PlayerApiService.FORMAT_PLAIN);
        //program info
        String programStr = "";
        if (isProgramInfo) {
	        if (systag.getType() != SysTag.TYPE_WHATSON) {
	            programStr = (String) NNF.getProgramMngr().findLatestProgramInfoByChannels(channels, format);            
	        } else {
	        	//please reference getPlayerWhatson
	            NnChannel daypartingChannel = null;
	            for (NnChannel c : channels) {
	                if (c.getContentType() == NnChannel.CONTENTTYPE_DAYPARTING_MASK)
	                   daypartingChannel = c;                    
	            }   
	            //find real dayparting channels and its programs
	            SysTagDisplay dayparting = this.findDayparting(time, display.getLang(), mso.getId());
	            if (dayparting != null) {
	               log.info("dayparting:" + dayparting.getName());
	               List<NnChannel> daypartingChannels = NNF.getSysTagMngr().findDaypartingChannelsById(dayparting.getSystagId(), display.getLang(), mso.getId(), time);
	               //List<YtProgram> ytprograms = new YtProgramDao().findByChannels(daypartingChannels);           
	               //programStr += (String) programMngr.composeYtProgramInfo(daypartingChannel, ytprograms, format);
	               programStr += (String) new YtProgramManager().findByDaypartingChannels(daypartingChannels, daypartingChannel, mso.getId(), time, display.getLang());
	            }
	            //find trending channels' programs
	            List<YtProgram> ytprograms = new YtProgramDao().findByChannels(channels);            
	            programStr += NNF.getProgramMngr().composeYtProgramInfo(null, ytprograms, format);                                                            
	        }
        }
        result[3] = programStr;
        return result;
       } else {
            PlayerSetInfo json = new PlayerSetInfo();
            json.setMsoName(name);
            json.setMsoImageUrl(imageUrl);
            json.setMsoDescription(intro);
            SetInfo setInfo = new SetInfo();
            setInfo.setId(setId);
            setInfo.setName(setName);
            setInfo.setThumbnail(setImageUrl);
            setInfo.setBannerImageUrl(bannerImageUrl);
            setInfo.setBannerImageUrl2(bannerImageUrl2);
            List<SetInfo> setInfoList = new ArrayList<SetInfo>();
            setInfoList.add(setInfo);
            json.setSetInfo(setInfoList);
            @SuppressWarnings("unchecked")
            List<ChannelLineup> lineups = (List<ChannelLineup>) NNF.getChannelMngr().composeChannelLineup(channels, version, PlayerApiService.FORMAT_JSON);
            json.setChannels(lineups);            
            @SuppressWarnings("unchecked")
            List<ProgramInfo> programInfo = (List<ProgramInfo>) NNF.getProgramMngr().findLatestProgramInfoByChannels(channels, format);
            json.setPrograms(programInfo);
            return json; 
        }
    }
    
}
