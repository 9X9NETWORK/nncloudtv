package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.SysTagDisplayDao;
import com.nncloudtv.dao.YtProgramDao;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
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
    
    private SysTagDisplayDao dao = new SysTagDisplayDao();
    
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
            SysTag systag = new SysTagManager().findById(dayparting.getSystagId());
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
    
    public void deleteAll(List<SysTagDisplay> sysTagDisplays) {
        if (sysTagDisplays == null || sysTagDisplays.size() < 1) {
            return ;
        }
        dao.deleteAll(sysTagDisplays);
    }
    
    public void addChannelCounter(NnChannel channel) {
        
    }

    @SuppressWarnings("unchecked")
	public Object getPlayerCategoryInfo(SysTagDisplay display, boolean programInfo, List<NnChannel> channels, long start, long limit, long total, int version, short format) {
    	String id = String.valueOf(display.getId());
    	String name = display.getName();    	
        NnChannelManager chMngr = new NnChannelManager();
    	NnProgramManager programMngr = new NnProgramManager();        
        //1. category info            	
    	if (format == PlayerApiService.FORMAT_PLAIN) {
	        List<String> result = new ArrayList<String>();	        
	        String categoryInfo = "";
	        categoryInfo += PlayerApiService.assembleKeyValue("id", id);
	        categoryInfo += PlayerApiService.assembleKeyValue("name", name);
	        categoryInfo += PlayerApiService.assembleKeyValue("start", String.valueOf(start));        
	        categoryInfo += PlayerApiService.assembleKeyValue("count", String.valueOf(limit));
	        categoryInfo += PlayerApiService.assembleKeyValue("total", String.valueOf(total));
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
	        	String programInfoStr = (String) programMngr.findLatestProgramInfoByChannels(channels, format);
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
    		categoryInfo.setProgramInfo((List<ProgramInfo>) programMngr.findLatestProgramInfoByChannels(channels, format));
    		return categoryInfo;
    	}
    }
   
    public Object getPlayerWhatson(String lang, short time, short format, Mso mso) {
        SysTagDisplayManager displayMngr = new SysTagDisplayManager();
        SysTagManager systagMngr = new SysTagManager();
        List<NnChannel> listingChannels = new ArrayList<NnChannel>();
        NnProgramManager programMngr = new NnProgramManager();
        YtProgramDao dao = new YtProgramDao();
        String programInfo = "";

        //special handling for dayparting channels
        //the real dayparting channel section
        SysTagDisplay dayparting = displayMngr.findDayparting(time, lang, mso.getId());
        if (dayparting != null) {
        	System.out.println("dayparting:" + dayparting.getName());
        	List<NnChannel> daypartingChannels = systagMngr.findPlayerChannelsById(dayparting.getSystagId(), lang, true, 0);
        	for (NnChannel d : daypartingChannels) {
        		System.out.println("dayparting channels:" + d.getId() + ";" + d.getName());
        	}
            List<YtProgram> ytprograms = dao.findByChannels(daypartingChannels);            
            programInfo = (String) programMngr.composeYtProgramInfo(ytprograms, format);
        } else {
           return new String[]{"", "", ""};
        }
        //find whaton systag
        SysTagDisplay whatson = this.findByType(mso.getId(), SysTag.TYPE_WHATSON, lang);
        if (whatson != null ) {
            List<NnChannel> whatsonChannels = systagMngr.findPlayerHiddenChannelsById(whatson.getSystagId(), lang, SysTag.SORT_SEQ, mso.getId());
            System.out.println("whatsonchannels:" + whatsonChannels.size());
            listingChannels.addAll(whatsonChannels);
            List<YtProgram> ytprograms = dao.findByChannels(whatsonChannels);            
            programInfo += programMngr.composeYtProgramInfo(ytprograms, format);            	                            
        } else {
            return new String[]{"", "", ""};
        }
        String setStr = "";
        System.out.println("whatson:" + whatson.getName());
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
        //}                
        NnChannelManager chMngr = new NnChannelManager();
        String channelInfo = (String)chMngr.composeReducedChannelLineup(listingChannels, PlayerApiService.FORMAT_PLAIN);        	
        String result[] = {setStr, channelInfo, programInfo};
        return result;    	
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
        SysTagManager systagMngr = new SysTagManager();
        NnChannelManager chMngr = new NnChannelManager();
        String channelStr = "";
        String programStr = "";
        List<ChannelLineup> channelLineup = new ArrayList<ChannelLineup>();
        List<ProgramInfo> programInfo = new ArrayList<ProgramInfo>();
        if (!minimal) {
	        //2: list of channel's channelInfo of every set
	        List<NnChannel> channels = new ArrayList<NnChannel>();
	        if (displays.size() > 0) {
	        	SysTag systag = systagMngr.findById(displays.get(0).getSystagId());
	        	short sort = SysTag.SORT_DATE;
	        	if (systag.getType() == SysTag.TYPE_SET) {
	        		sort = systag.getSorting();
	        	}
	            channels.addAll(systagMngr.findPlayerChannelsById(displays.get(0).getSystagId(), lang, sort, 0));
	        }
	        if (format == PlayerApiService.FORMAT_PLAIN) {
	        	channelStr = (String)chMngr.composeChannelLineup(channels, version, format);
	        } else {
	        	channelLineup = (List<ChannelLineup>)chMngr.composeChannelLineup(channels, version, format);
	        }
	        //3. list of the latest episode of each channel of the first set
	        NnProgramManager programMngr = new NnProgramManager();
	        if (format == PlayerApiService.FORMAT_PLAIN) {
	        	programStr = (String)programMngr.findLatestProgramInfoByChannels(channels, format);
	        } else {
	        	programInfo = (List<ProgramInfo>)programMngr.findLatestProgramInfoByChannels(channels, format);
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
    
    public Object getPlayerSetInfo(Mso mso, SysTagDisplay display, List<NnChannel> channels, List<NnProgram>programs, int version, short format) {
    	String name = mso.getName();
    	String imageUrl = mso.getLogoUrl();
    	String intro = mso.getIntro();
    	String setId = String.valueOf(display.getId());
    	String setName = display.getName();
    	String setImageUrl = display.getImageUrl();
    	String bannerImageUrl = display.getBannerImageUrl();
    	String bannerImageUrl2 = display.getBannerImageUrl2();
        NnProgramManager programMngr = new NnProgramManager();
        for (NnChannel c : channels) {
            if (c.getStatus() == NnChannel.STATUS_SUCCESS && c.isPublic())
                c.setSorting(NnChannelManager.getPlayerDefaultSorting(c));
        }
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
	        //channel info
	        result[2] = (String) new NnChannelManager().composeChannelLineup(channels, version, PlayerApiService.FORMAT_PLAIN);
	        //program info
	        String programStr = (String) programMngr.findLatestProgramInfoByChannels(channels, format);
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
			List<ChannelLineup> lineups = (List<ChannelLineup>)new NnChannelManager().composeChannelLineup(channels, version, PlayerApiService.FORMAT_JSON);
    		json.setChannels(lineups);    		
    		@SuppressWarnings("unchecked")
    		List<ProgramInfo> programInfo = (List<ProgramInfo>) programMngr.findLatestProgramInfoByChannels(channels, format);
    		json.setPrograms(programInfo);
    		return json; 
    	}
    }
    
}
