package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.SysTagMap;

/**
 * This is father class of SetService and CategoryService, since their operation are similar
 *   and should put basic method into father class.
 * Other type list in SysTag.type may has its Service and recommend reference this class. 
 *
 */
@Service
public class ContainerService {
    
    protected static final Logger log = Logger.getLogger(ContainerService.class.getName());
    
    /**
     * Get Channels from Container ordered by sequence, the Channels populate additional information (TimeStart, TimeEnd, Seq, AlwaysOnTop)
     *   retrieve from SysTagMap.
     * @param sysTagId required, SysTag ID
     * @return list of Channels */
    public List<NnChannel> findChannels(Long sysTagId) {
        
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
    
    /**
     * Get Channels from Container ordered by updateTime, Channel with AlwaysOnTop set to True will put in the head of results,
     *   the Channels populate additional information (TimeStart, TimeEnd, Seq, AlwaysOnTop) retrieve from SysTagMap.
     * @param sysTagId required, SysTag ID
     * @return list of Channels */
    public List<NnChannel> getChannelsOrderByUpdateTime(Long sysTagId) {
        
        if (sysTagId == null) {
            return new ArrayList<NnChannel>();
        }
        List<NnChannel> channels = findChannels(sysTagId);
        if (channels == null) {
            return new ArrayList<NnChannel>();
        }
        if (channels.size() < 2) {
            return channels;
        }
        
        List<NnChannel> results = new ArrayList<NnChannel>();
        List<NnChannel> sortedChannels = new ArrayList<NnChannel>();
        for (NnChannel channel : channels) {
            if (channel.isAlwaysOnTop() == true) {
                results.add(channel);
            } else {
                sortedChannels.add(channel);
            }
        }
        
        Collections.sort(sortedChannels, NNF.getChannelMngr().getChannelComparator("default"));
        results.addAll(sortedChannels);
        
        return results;
    }
    
    public void addChannel(Long sysTagId, Long channelId, Short timeStart, Short timeEnd, Boolean alwaysOnTop, Boolean featured, Short seq) {
        
        if (sysTagId == null || channelId == null) {
            throw new IllegalArgumentException("sysTagId or channelId is null");
        }
        
        // create if not exist
        SysTagMap sysTagMap = NNF.getSysTagMapMngr().findOne(sysTagId, channelId);
        if (sysTagMap == null) {
            sysTagMap = new SysTagMap(sysTagId, channelId);
            sysTagMap.setSeq((short) 0);
            sysTagMap.setTimeStart((short) 0);
            sysTagMap.setTimeEnd((short) 0);
            sysTagMap.setAlwaysOnTop(false);
            sysTagMap.setFeatured(false);
        }
        
        sysTagMap.setTimeStart(timeStart);
        sysTagMap.setTimeEnd(timeEnd);
        sysTagMap.setAlwaysOnTop(alwaysOnTop);
        sysTagMap.setFeatured(featured);
        sysTagMap.setSeq(seq);
        
        NNF.getSysTagMapMngr().save(sysTagMap);
    }

}
