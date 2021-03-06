package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.StoreListingDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.StoreListing;

@Service
public class StoreListingManager {
    
    protected static final Logger log = Logger.getLogger(StoreListingManager.class.getName());
    
    private StoreListingDao dao = NNF.getStoreListingDao();
    
    public void addChannelsToBlackList(List<Long> channelIds, Long msoId) {
        
        if (channelIds == null || msoId == null || channelIds.size() == 0) {
            return ;
        }
        
        List<NnChannel> channels = NNF.getChannelMngr().findByIds(channelIds);
        if (channels == null || channels.size() == 0) {
            return ;
        }
        
        List<Long> channelIdList = new ArrayList<Long>();
        for (NnChannel channel : channels) {
            channelIdList.add(channel.getId());
        }
        
        // find if already exist, update updateDate
        List<StoreListing> storeListing = dao.findByChannelIdsAndMsoId(channelIdList, msoId);
        if (storeListing != null && storeListing.size() > 0) {
            saveAll(storeListing);
            
            for (StoreListing item : storeListing) {
                if (channelIdList.contains(item.getChannelId())) {
                    channelIdList.remove(item.getChannelId());
                }
            }
        }
        if (channelIdList.size() == 0) {
            return ;
        }
        
        // save if not exist in db
        StoreListing item = null;
        List<StoreListing> newStoreListing = new ArrayList<StoreListing>();
        for (Long channelId : channelIdList) {
            item = new StoreListing(channelId, msoId);
            newStoreListing.add(item);
        }
        
        saveAll(newStoreListing);
    }
    
    public void removeChannelsFromBlackList(List<Long> channelIds, Long msoId) {
        
        if (channelIds == null || msoId == null || channelIds.size() == 0) {
            return ;
        }
        
        List<NnChannel> channels = NNF.getChannelMngr().findByIds(channelIds);
        if (channels == null || channels.size() == 0) {
            return ;
        }
        
        List<Long> channelIdList = new ArrayList<Long>();
        for (NnChannel channel : channels) {
            channelIdList.add(channel.getId());
        }
        
        List<StoreListing> storeListing = dao.findByChannelIdsAndMsoId(channelIdList, msoId);
        if (storeListing != null && storeListing.size() > 0) {
            dao.deleteAll(storeListing);
        }
        
    }
    
    private Collection<StoreListing> saveAll(Collection<StoreListing> storeListing) {
        
        if (storeListing == null)
            return null;
        
        if (storeListing.size() == 0)
            return storeListing;
        
        Date now = NnDateUtil.now();
        for (StoreListing item : storeListing)
            item.setUpdateDate(now);
        
        return dao.saveAll(storeListing);
    }
    
    /** call when NnChannel is going to delete **/
    public void deleteByChannelId(Long channelId) {
        
    }
    
    /** call when Mso is going to delete **/
    public void deleteByMsoId(Long msoId) {
        
    }
    
    public List<StoreListing> getBlackListByChannelId(Long channelId) {
        
        if (channelId == null) {
            return new ArrayList<StoreListing>();
        }
        
        List<StoreListing> results = dao.findByChannelId(channelId);
        if (results == null) {
            return new ArrayList<StoreListing>();
        }
        return results;
    }
    
    public List<Long> findChannelIdsByMsoId(long msoId) {
        
        List<Long> results = new ArrayList<Long>();
        List<StoreListing> list = dao.findByMsoId(msoId);
        for (StoreListing item : list)
            results.add(item.getChannelId());
        return results;
    }
    
    /** indicate whether channel is in Mso's black list or not */
    public boolean isChannelInMsoBlackList(Long msoId, Long channelId) {
        
        if (msoId == null || channelId == null) {
            return false;
        }
        
        StoreListing storeListing = dao.findByChannelIdAndMsoId(channelId, msoId);
        if (storeListing == null) {
            return false;
        } else {
            return true;
        }
    }

}
