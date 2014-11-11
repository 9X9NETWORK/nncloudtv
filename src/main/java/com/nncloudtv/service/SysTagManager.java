package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.SysTagDao;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagMap;

@Service
public class SysTagManager {
    
    protected static final Logger log = Logger.getLogger(SysTagManager.class.getName());
    
    private SysTagDao dao = NNF.getSysTagDao();
    
    public SysTag findById(long id) {
        
        return dao.findById(id);
    }
    
    public SysTag save(SysTag sysTag) {
        
        if (sysTag == null) {
            return null;
        }
        
        Date now = new Date();
        sysTag.setUpdateDate(now);
        if (sysTag.getCreateDate() == null) {
            sysTag.setCreateDate(now);
        }
        
        sysTag = dao.save(sysTag);
        
        return sysTag;
    }
    
    public void delete(SysTag sysTag) {
        
        if (sysTag == null) { return ; }
        
        //SysTagMap(s)
        NNF.getSysTagMapDao().deleteAll(NNF.getSysTagMapDao().findBySysTagId(sysTag.getId()));;
        
        //SysTagDisplay(s)
        NNF.getDisplayDao().deleteAll(NNF.getDisplayDao().findAllBySysTagId(sysTag.getId()));;
        
        dao.delete(sysTag);
    }
    
    /** call when Mso is going to delete **/
    public void deleteByMsoId(Long msoId) {
        // delete sysTags, sysTagDisplays, sysTagMaps
    }
    
    public List<SysTag> findByMsoIdAndType(Long msoId, short type) {
        
        if (msoId == null) {
            return new ArrayList<SysTag>();
        }
        
        return dao.findByMsoIdAndType(msoId, type);
    }
    
    public long findPlayerChannelsCountById(long systagId, String lang, long msoId) {
        
        return NNF.getChannelDao().findPlayerChannelsCountBySysTag(systagId, lang, msoId);
    }
    
    // isPlayer: means status = true and isPublic = true
    //     lang: if lang is null, then don't filter sphere
    //     sort: if order = SysTag.SEQ, order by seq, otherwise order by updateDate
    //    msoId: if msoId = 0, do store_listing black list
    public List<NnChannel> findPlayerChannelsById(long systagId, String lang, int start, int count, short sort, long msoId) {
        
        return NNF.getChannelDao().findBySysTag(systagId, lang, false, start, count, sort, msoId, true);
    }
    
    //TODO virtual? front-page? still using?
    public List<NnChannel> findPlayerChannelsById(long systagId, String lang, boolean rand, long msoId) {
        
        return NNF.getChannelDao().findBySysTag(systagId, lang, true, 0, 0, SysTag.SORT_DATE, 0, true);
    }
    
    //all: find those non-public as well
    public List<NnChannel> findPlayerAllChannelsById(long systagId, String lang, short sort, long msoId) {
        
        return NNF.getChannelDao().findBySysTag(systagId, lang, false, 0, 0, sort, msoId, false);
    }
    
    public void resetDaypartingCache(long msoId, String lang) {
        
        for (short i = 0; i < 24; i++) {
            
            CacheFactory.delete(CacheFactory.getDayPartingChannelKey(msoId, i, lang));
            CacheFactory.delete(CacheFactory.getDaypartingProgramsKey(msoId, i, lang));
        }
    }
    
    public List<NnChannel> findDaypartingChannelsById(long id, String lang, long msoId, short time) {
        String cacheKey = CacheFactory.getDayPartingChannelKey(msoId, time, lang);
        try {
            @SuppressWarnings("unchecked")
            List<NnChannel> channels = (List<NnChannel>) CacheFactory.get(cacheKey);
            if (channels != null)
                return channels;
        } catch (Exception e) {
            log.info("memcache error");
        }
        return  NNF.getChannelDao().findBySysTag(id, lang, true, 0, 0, SysTag.SORT_DATE, 0, true);
    }
    
    public short convertDashboardType(long systagId) {
        SysTag tag = this.findById(systagId);
        if (tag == null)
            return 99;
        if (tag.getType() == SysTag.TYPE_DAYPARTING)
            return 0;
        if (tag.getType() == SysTag.TYPE_ACCOUNT)
            return 2;
        if (tag.getType() == SysTag.TYPE_SUBSCRIPTION)
            return 1;
        return 0;
            
    }
    
    public SysTagMap addChannel(long sysTagId, long channelId, boolean alwaysOnTop, boolean featured, short seq) {
        
        // create if not exist
        SysTagMap sysTagMap = NNF.getSysTagMapMngr().findOne(sysTagId, channelId);
        if (sysTagMap == null) {
            
            sysTagMap = new SysTagMap(sysTagId, channelId);
        }
        
        sysTagMap.setAlwaysOnTop(alwaysOnTop);
        sysTagMap.setFeatured(featured);
        sysTagMap.setSeq(seq);
        
        return NNF.getSysTagMapMngr().save(sysTagMap);
    }
    
    /**
     * Get Channels from Container ordered by sequence, the Channels populate additional information (TimeStart, TimeEnd, Seq, AlwaysOnTop)
     *   retrieve from SysTagMap.
     * @param sysTagId required, SysTag ID
     * @return list of Channels */
    public List<NnChannel> getChannels(Long sysTagId) {
        
        List<SysTagMap> sysTagMaps = NNF.getSysTagMapMngr().findBySysTagId(sysTagId);
        
        List<NnChannel> results = new ArrayList<NnChannel>();
        for (SysTagMap sysTagMap : sysTagMaps) {
            
            NnChannel channel = NNF.getChannelMngr().findById(sysTagMap.getChannelId());
            if (channel != null) {
                
                channel.setTimeStart(sysTagMap.getTimeStart());
                channel.setTimeEnd(sysTagMap.getTimeEnd());
                channel.setSeq(sysTagMap.getSeq());
                channel.setAlwaysOnTop(sysTagMap.isAlwaysOnTop());
                channel.setFeatured(sysTagMap.isFeatured());
                
                results.add(channel);
            }
        }
        
        return results;
    }
    
    public SysTag findById(String idStr) {
        
        return dao.findById(idStr);
    }
}
