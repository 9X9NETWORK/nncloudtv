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
import com.nncloudtv.web.json.cms.NnSet;

@Service
public class SetService {
    
    protected static final Logger log = Logger.getLogger(SetService.class.getName());
    
    public NnSet composeNnSet(SysTag sysTag, SysTagDisplay display) {
        
        NnSet nnSet = new NnSet();
        
        nnSet.setId(sysTag.getId());
        nnSet.setMsoId(sysTag.getMsoId());
        nnSet.setSeq(sysTag.getSeq());
        nnSet.setSorting(sysTag.getSorting());
        
        nnSet.setCntChannel(display.getCntChannel());
        nnSet.setLang(display.getLang());
        nnSet.setTag(display.getPopularTag());
        nnSet.setName(display.getName());
        nnSet.setAndroidBannerUrl(display.getBannerImageUrl());
        nnSet.setIosBannerUrl(display.getBannerImageUrl2());
        
        return nnSet;
    }
    
    public static NnSet normalize(NnSet set) {
        
        set.setName(NnStringUtil.revertHtml(set.getName()));
        
        return set;
    }
    
    public List<NnSet> findByMsoIdAndLang(long msoId, String lang) {
        
        List<NnSet> results = new ArrayList<NnSet>();
        NnSet nnSet = null;
        
        List<SysTag> sets = NNF.getSysTagMngr().findByMsoIdAndType(msoId, SysTag.TYPE_SET);
        if (sets == null || sets.size() == 0) {
            return new ArrayList<NnSet>();
        }
        
        SysTagDisplay display = null;
        for (SysTag sysTag : sets) {
            
            if (lang == null) {
                display = NNF.getDisplayMngr().findBySysTagId(sysTag.getId());
            } else {
                display = NNF.getDisplayMngr().findBySysTagIdAndLang(sysTag.getId(), lang);
            }
            if (display != null) {
                nnSet = composeNnSet(sysTag, display);
                results.add(nnSet);
            }
        }
        
        return results;
    }
    
    public NnSet findById(String setIdStr) {
        
        SysTag sysTag = NNF.getSysTagDao().findById(setIdStr);
        
        if (sysTag != null) {
            
            SysTagDisplay display = NNF.getDisplayMngr().findBySysTagId(sysTag.getId());
            if (display != null) {
                
                return composeNnSet(sysTag, display);
            }
        }
        
        return null;
    }
    
    public NnSet findById(long setId) {
        
        SysTag sysTag = NNF.getSysTagDao().findById(setId);
        
        if (sysTag != null) {
            
            SysTagDisplay display = NNF.getDisplayMngr().findBySysTagId(sysTag.getId());
            if (display != null) {
                
                return composeNnSet(sysTag, display);
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
    
    public NnSet create(NnSet set) {
        
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
        
        return composeNnSet(sysTag, display);
    }
}
