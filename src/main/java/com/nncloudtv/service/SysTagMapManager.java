package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.SysTagMapDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
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
    
    public Collection<SysTagMap> save(Collection<SysTagMap> sysTagMaps) {
        Date now = NnDateUtil.now();
        for (SysTagMap sysTagMap : sysTagMaps) {
            if (sysTagMap.getCreateDate() == null)
                sysTagMap.setCreateDate(now);
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
        Collections.sort(sysTagMaps, getDefaultComparator());
        
        log.info("sysTagMaps.size() = " + sysTagMaps.size());
        
        for (int i = 0; i < sysTagMaps.size(); i++) {
            sysTagMaps.get(i).setSeq((short) (i + 1));
        }
        
        dao.saveAll(sysTagMaps);
    }
    
    private Comparator<SysTagMap> getDefaultComparator() {
        
       return new Comparator<SysTagMap>() {
           
            public int compare(SysTagMap sysTagMap1, SysTagMap sysTagMap2) {
                
                short seq1 = sysTagMap1.getSeq();
                short seq2 = sysTagMap2.getSeq();
                
                return ((int) seq1 -seq2);
            }
        };
    }
    
    public void delete(SysTagMap sysTagMap) {
        
        dao.delete(sysTagMap);
    }
    
    public void delete(List<SysTagMap> sysTagMaps) {
        
        dao.deleteAll(sysTagMaps);
    }
    
    public SysTagMap findOne(long sysTagId, long channelId) {
        
        return dao.findBySysTagIdAndChannelId(sysTagId, channelId);
    }
    
    public List<SysTagMap> findAll(Long sysTadId, List<Long> channelIds) {
        
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
    
    public List<SysTagMap> findBySysTagId(long sysTagId) {
        
        return dao.findBySysTagId(sysTagId);
    }
    
    public List<SysTagMap> findCategoryMaps(long channelId, long msoId) {
        
        return dao.findCategoryMaps(channelId, msoId);
    }
    
}
