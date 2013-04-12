package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nncloudtv.dao.StoreListingDao;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.StoreListing;

@Service
public class StoreListingManager {
    
    protected static final Logger log = Logger.getLogger(StoreListingManager.class.getName());
    
    private StoreListingDao dao = new StoreListingDao();
    private NnChannelManager channelMngr;
    
    @Autowired
    public StoreListingManager(NnChannelManager channelMngr) {
        this.channelMngr = channelMngr;
    }
    
    public List<NnChannel> findByChannelIdsAndMsoId(List<Long> channelIds, Long msoId) {
        
        if (channelIds == null || channelIds.size() == 0 || msoId == null) {
            return new ArrayList<NnChannel>();
        }
        
        List<StoreListing> storeListing = dao.findByChannelIdsAndMsoId(channelIds, msoId);
        if ((storeListing == null) || (storeListing.size() == 0)) {
            return new ArrayList<NnChannel>();
        }
        
        List<Long> channelIdList = new ArrayList<Long>();
        for (StoreListing item : storeListing) {
            channelIdList.add(item.getChannelId());
        }
        List<NnChannel> results = channelMngr.findByIds(channelIdList);
        if (results == null) {
            return new ArrayList<NnChannel>();
        }
        
        return results;
    }
    
    public List<NnChannel> findByPaging(long page, long rows, Long msoId) {
        
        if (page <= 0 || rows <= 0 || msoId == null) {
            return new ArrayList<NnChannel>();
        }
        
        String filter = "msoId == " + msoId;
        List<StoreListing> storeListing = dao.list(page, rows, "updateDate", "desc", filter);
        if ((storeListing == null) || (storeListing.size() == 0)) {
            return new ArrayList<NnChannel>();
        }
        
        List<Long> channelIdList = new ArrayList<Long>();
        for (StoreListing item : storeListing) {
            channelIdList.add(item.getChannelId());
        }
        List<NnChannel> results = channelMngr.findByIds(channelIdList);
        if (results == null) {
            return new ArrayList<NnChannel>();
        }
        
        return results;
    }
    
    public void addChannelsToStore(List<Long> channelIds, Long msoId) {
        
        if (channelIds == null || msoId == null || channelIds.size() == 0) {
            return ;
        }
        
        List<NnChannel> channels = channelMngr.findByIds(channelIds);
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
    
    public void removeChannelsFromStore(List<Long> channelIds, Long msoId) {
        
        if (channelIds == null || msoId == null || channelIds.size() == 0) {
            return ;
        }
        
        List<NnChannel> channels = channelMngr.findByIds(channelIds);
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
    
    private List<StoreListing> saveAll(List<StoreListing> storeListing) {
        
        if (storeListing == null) {
            return null;
        }
        if (storeListing.size() == 0) {
            return storeListing;
        }
        
        Date now = new Date();
        for (StoreListing item : storeListing) {
            item.setUpdateDate(now);
        }
        
        storeListing = dao.saveAll(storeListing);
        
        return storeListing;
    }
    
    /** call when NnChannel is going to delete **/
    public void deleteByChannelId(Long channelId) {
        
    }
    
    /** call when Mso is going to delete **/
    public void deleteByMsoId(Long msoId) {
        
    }

}