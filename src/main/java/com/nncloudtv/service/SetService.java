package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.model.SysTagMap;
import com.nncloudtv.web.json.cms.Set;

@Service
public class SetService {
    
    protected static final Logger log = Logger.getLogger(SetService.class.getName());
    
    /** build Set from SysTag and SysTagDisplay */
    public Set composeSet(SysTag set, SysTagDisplay setMeta) {
        
        Set setResp = new Set();
        setResp.setId(set.getId());
        setResp.setMsoId(set.getMsoId());
        setResp.setCntChannel(setMeta.getCntChannel());
        setResp.setLang(setMeta.getLang());
        setResp.setSeq(set.getSeq());
        setResp.setTag(setMeta.getPopularTag());
        setResp.setName(setMeta.getName());
        setResp.setSortingType(set.getSorting());
        
        return setResp;
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
    
    /**
     * find Set by SysTag's Id
     * 
     * @param setId
     *            required, SysTag's ID with type = Set
     * @return object Set or null if not exist
     */
    public Set findById(Long setId) {
        
        if (setId == null) {
            return null;
        }
        
        SysTag set = NNF.getSysTagMngr().findById(setId);
        if (set == null || set.getType() != SysTag.TYPE_SET) {
            return null;
        }
        
        SysTagDisplay setMeta = NNF.getDisplayMngr().findBySysTagId(set.getId());
        if (setMeta == null) {
            log.warning("invalid structure : SysTag's Id=" + set.getId() + " exist but not found any of SysTagDisPlay");
            return null;
        }
        
        return composeSet(set, setMeta);
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
    
    /**
     * check if input Channel's IDs represent all Channels in the Set
     * 
     * @param setId
     *            required, SysTag's ID with type = Set
     * @param channelIds
     *            required, Channel's IDs to be tested
     * @return true if full match, false for not
     */
    public boolean isContainAllChannels(Long setId, List<Long> channelIds) {
        
        if (setId == null || channelIds == null) {
            return false;
        }
        
        // it must same as setChannels's result
        List<SysTagMap> setChannels = NNF.getSysTagMapMngr().findBySysTagId(setId);
        if (setChannels == null) {
            if (channelIds.size() == 0) {
                return true;
            } else {
                return false;
            }
        }
        
        int index;
        for (SysTagMap channel : setChannels) {
            index = channelIds.indexOf(channel.getChannelId());
            if (index > -1) {
                // pass
            } else {
                // input missing this Channel ID 
                return false;
            }
        }
        
        if (setChannels.size() != channelIds.size()) {
            // input contain duplicate or other Channel Id
            return false;
        }
        
        return true;
    }
    
    public Set create(Set set) {
        
        SysTag newSet = new SysTag();
        newSet.setType(SysTag.TYPE_SET);
        newSet.setMsoId(set.getMsoId());
        newSet.setSeq(set.getSeq());
        newSet.setSorting(set.getSortingType());
        newSet.setFeatured(true);
        
        SysTagDisplay newSetMeta = new SysTagDisplay();
        newSetMeta.setCntChannel(0);
        newSetMeta.setLang(set.getLang());
        newSetMeta.setName(set.getName());
        newSetMeta.setPopularTag(set.getTag());
        
        SysTag savedSet = NNF.getSysTagMngr().save(newSet);
        newSetMeta.setSystagId(savedSet.getId());
        SysTagDisplay savedSetMeta = NNF.getDisplayMngr().save(newSetMeta);
        
        Set result = composeSet(savedSet, savedSetMeta);
        
        return result;
    }
    
    public static boolean isValidSortingType(Short sortingType) {
        
        return SysTagManager.isValidSortingType(sortingType);
    }
    
}
