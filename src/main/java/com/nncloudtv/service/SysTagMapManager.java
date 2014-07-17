package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.SysTagMapDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.SysTagMap;

@Service
public class SysTagMapManager {
    
    protected static final Logger log = Logger.getLogger(SysTagMapManager.class.getName());
    
    private SysTagMapDao dao = NNF.getSysTagMapDao();
    
    public SysTagMap save(SysTagMap sysTagMap) {
        
        if (sysTagMap == null) { return null; }
        
        Date now = new Date();
        if (sysTagMap.getCreateDate() == null) {
            sysTagMap.setCreateDate(now);
        }
        sysTagMap.setUpdateDate(now);
        
        return NNF.getSysTagMapDao().save(sysTagMap);
    }
    
    public List<SysTagMap> save(List<SysTagMap> sysTagMaps) {
        
        Date now = new Date();
        for (SysTagMap sysTagMap : sysTagMaps) {
            if (sysTagMap.getCreateDate() == null) {
                sysTagMap.setCreateDate(now);
            }
            sysTagMap.setUpdateDate(now);
        }
        
        return dao.saveAll(sysTagMaps);
    }
    
    public void reorderSysTagMaps(Long sysTagId) {
        
        if (sysTagId == null) {
            return ;
        }
        
        List<SysTagMap> sysTagMaps = dao.findBySysTagId(sysTagId);
        if (sysTagMaps == null || sysTagMaps.size() == 0) {
            return ;
        }
        Collections.sort(sysTagMaps, getSysTagMapComparator());
        
        log.info("sysTagMaps.size() = " + sysTagMaps.size());
        
        for (int i = 0; i < sysTagMaps.size(); i++) {
            sysTagMaps.get(i).setSeq((short) (i + 1));
        }
        
        dao.saveAll(sysTagMaps);
    }
    
    private Comparator<SysTagMap> getSysTagMapComparator() {
        class SysTagMapComparator implements Comparator<SysTagMap> {
            public int compare(SysTagMap sysTagMap1, SysTagMap sysTagMap2) {
                short seq1 = sysTagMap1.getSeq();
                short seq2 = sysTagMap2.getSeq();
                return (int) (seq1 - seq2);
            }
        }
        return new SysTagMapComparator();
    }
    
    public void delete(SysTagMap sysTagMap) {
        
        dao.delete(sysTagMap);
    }
    
    public void delete(List<SysTagMap> sysTagMaps) {
        
        dao.deleteAll(sysTagMaps);
    }
    
    public SysTagMap findOne(Long sysTagId, Long channelId) {
        
        return dao.findBySysTagIdAndChannelId(sysTagId, channelId);
    }
    
    public SysTagMap findByChannelId(Long channelId) {
    	if (channelId == null)
    		return null;
    	List<Long> ids = new ArrayList<Long>();
    	ids.add(channelId);
        List<SysTagMap> channels = dao.findByChannelIds(ids);
        if (channels.size() > 0)
        	return channels.get(0);
        return null;    	
    }
    
    public List<SysTagMap> findBySysTagIdAndChannelIds(Long sysTadId, List<Long> channelIds) {
        
        if (sysTadId == null || channelIds == null || channelIds.size() < 1) {
            return new ArrayList<SysTagMap>();
        }
        
        List<SysTagMap> channels = dao.findByChannelIds(channelIds);
        
        List<SysTagMap> results = new ArrayList<SysTagMap>();
        for (SysTagMap channel : channels) {
            if (channel.getSysTagId() == sysTadId) {
                results.add(channel);
            }
        }
        
        return results;
    }
    
    public List<SysTagMap> findBySysTagId(Long sysTagId) {
        
        return dao.findBySysTagId(sysTagId);
    }
    
    public List<SysTagMap> findCategoryMapsByChannelId(Long channelId, Long msoId) {
        
        if (channelId == null || msoId == null) {
            return new ArrayList<SysTagMap>();
        }
        
        List<SysTagMap> results = dao.findCategoryMapsByChannelId(channelId, msoId);
        if (results == null) {
            return new ArrayList<SysTagMap>();
        }
        
        return results;
    }
    
    /** call when NnChannel is going to delete **/
    public void deleteByChannelId(Long channelId) {
        // delete sysTagMaps
    }
    
    /** call when SysTag is going to delete **/
    public void deleteBySysTagId(Long sysTagId) {
        // delete sysTagMaps
    }
    
}
