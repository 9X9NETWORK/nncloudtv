package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.QueueFactory;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.web.json.cms.Set;

@Service
public class SetService {
    
    protected static final Logger log = Logger.getLogger(SetService.class.getName());
    
    public Set composeSet(SysTag sysTag, SysTagDisplay display) {
        
        Set set = new Set();
        
        set.setId(sysTag.getId());
        set.setMsoId(sysTag.getMsoId());
        set.setSeq(sysTag.getSeq());
        set.setSorting(sysTag.getSorting());
        
        set.setCntChannel(display.getCntChannel());
        set.setLang(display.getLang());
        set.setTag(display.getPopularTag());
        set.setName(display.getName());
        set.setAndroidBannerUrl(display.getBannerImageUrl());
        set.setIosBannerUrl(display.getBannerImageUrl2());
        
        return set;
    }
    
    public static Set normalize(Set set) {
        
        set.setName(NnStringUtil.revertHtml(set.getName()));
        
        return set;
    }
    
    /**
     * find Sets that owned by Mso with specify display language
     * 
     * @param msoId
     *            required, result Sets that belong to this specified Mso
     * @param lang
     *            optional, result Sets has specified display language
     * @return list of Sets
     */
    public List<Set> findByMsoIdAndLang(Long msoId, String lang) {
        
        List<Set> results = new ArrayList<Set>();
        Set result = null;
        
        if (msoId == null) {
            return new ArrayList<Set>();
        }
        
        //List<SysTag> results = dao.findByMsoIdAndType(msoId, SysTag.TYPE_SET);
        List<SysTag> sets = NNF.getSysTagMngr().findByMsoIdAndType(msoId, SysTag.TYPE_SET);
        if (sets == null || sets.size() == 0) {
            return new ArrayList<Set>();
        }
        
        SysTagDisplay setMeta = null;
        for (SysTag set : sets) {
            
            if (lang != null) {
                setMeta = NNF.getDisplayMngr().findBySysTagIdAndLang(set.getId(), lang);
            } else {
                setMeta = NNF.getDisplayMngr().findBySysTagId(set.getId());
            }
            
            if (setMeta != null) {
                result = composeSet(set, setMeta);
                results.add(result);
            } else {
                if (lang == null) {
                    log.warning("invalid structure : SysTag's Id=" + set.getId()
                            + " exist but not found any of SysTagDisPlay");
                } else {
                    log.info("SysTag's Id=" + set.getId() + " exist but not found match SysTagDisPlay for lang=" + lang);
                }
            }
        }
        
        return results;
    }
    
    /**
     * find Sets that owned by Mso
     * 
     * @param msoId
     *            required, result Sets that belong to this specified Mso
     * @return list of Sets
     */
    public List<Set> findByMsoId(Long msoId) {
        
        if (msoId == null) {
            return new ArrayList<Set>();
        }
        
        return findByMsoIdAndLang(msoId, null);
    }
    
    public Set findById(String setIdStr) {
        
        SysTag sysTag = NNF.getSysTagDao().findById(setIdStr);
        
        if (sysTag != null) {
            
            SysTagDisplay display = NNF.getDisplayMngr().findBySysTagId(sysTag.getId());
            if (display != null) {
                
                return composeSet(sysTag, display);
            }
        }
        
        return null;
    }
    
    public Set findById(long setId) {
        
        SysTag sysTag = NNF.getSysTagDao().findById(setId);
        
        if (sysTag != null) {
            
            SysTagDisplay display = NNF.getDisplayMngr().findBySysTagId(sysTag.getId());
            if (display != null) {
                
                return composeSet(sysTag, display);
            }
        }
        
        return null;
    }
    
    /**
     * Get Channels from Set ordered by UpdateTime, Channel with AlwaysOnTop set to True will put in the head of
     * results,
     * the Channels populate additional information (TimeStart, TimeEnd, Seq, AlwaysOnTop) retrieve from SysTagMap.
     * 
     * @param setId
     *            required, Set ID
     * @return list of Channels
     */
    public List<NnChannel> getChannels(Long setId) {
        
        SysTag sysTag = NNF.getSysTagMngr().findById(setId);
        if (sysTag == null) { return null; }
        List<NnChannel> channels = NNF.getSysTagMngr().getChannels(setId);
        
        if (sysTag.getSorting() == SysTag.SORT_DATE) {
            
            Collections.sort(channels, NnChannelManager.getComparator("updateDateInSet"));
            
        }
        
        return channels;
    }
    
    public Set create(Set set) {
        
        SysTag sysTag = new SysTag();
        sysTag.setType(SysTag.TYPE_SET);
        sysTag.setMsoId(set.getMsoId());
        sysTag.setSeq(set.getSeq());
        sysTag.setSorting(set.getSortingType());
        sysTag.setFeatured(true);
        
        SysTagDisplay display = new SysTagDisplay();
        display.setCntChannel(0);
        display.setLang(set.getLang());
        display.setName(set.getName());
        display.setPopularTag(set.getTag());
        display.setBannerImageUrl(set.getAndroidBannerUrl());
        display.setBannerImageUrl2(set.getIosBannerUrl());
        
        sysTag = NNF.getSysTagMngr().save(sysTag);
        display.setSystagId(sysTag.getId());
        display = NNF.getDisplayMngr().save(display);
        
        // process thumbnail
        String url = "/podcastAPI/processThumbnail?set=" + sysTag.getId();
        log.info(url);
        QueueFactory.add(url, null);
        
        return composeSet(sysTag, display);
    }
}
