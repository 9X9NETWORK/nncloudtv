package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.StoreListing;

public class StoreListingDao extends GenericDao<StoreListing> {
    
    protected static final Logger log = Logger.getLogger(StoreListingDao.class.getName());
    
    public StoreListingDao() {
        super(StoreListing.class);
    }
    
    public List<StoreListing> findByChannelIdsAndMsoId(List<Long> channelIds, long msoId) {
        
        List<StoreListing> detached = new ArrayList<StoreListing>();
        
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        try {
            Query query = pm.newQuery(StoreListing.class, ":p.contains(channelId)");
            @SuppressWarnings("unchecked")
            List<StoreListing> results = (List<StoreListing>) query.execute(channelIds);
            if (results.size() > 0)
                detached = (List<StoreListing>) pm.detachCopyAll(results);
            query.closeAll();
        } finally {
            pm.close();
        }
        
        Iterator<StoreListing> it = detached.iterator();
        while (it.hasNext()) {
            StoreListing item = it.next();
            if (item.getMsoId() != msoId) {
                it.remove();
                continue;
            }
        }
        
        return detached;
    }
    
    public StoreListing findByChannelIdAndMsoId(long channelId, long msoId) {
        
        StoreListing detached = null;
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(StoreListing.class);
            query.setFilter("channelId == channelIdParam && msoId == msoIdParam");
            query.declareParameters("long channelIdParam, long msoIdParam");
            @SuppressWarnings("unchecked")
            List<StoreListing> results = (List<StoreListing>) query.execute(channelId, msoId);
            if (results.size() > 0)
                detached = pm.detachCopy(results.get(0));
            query.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public List<StoreListing> findByMsoId(long msoId) {
        
        List<StoreListing> detached = new ArrayList<StoreListing>();
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(StoreListing.class);
            query.setFilter("msoId == msoIdParam");
            query.declareParameters("long msoIdParam");
            @SuppressWarnings("unchecked")
            List<StoreListing> results = (List<StoreListing>)query.execute(msoId);
            if (results.size() > 0)
                detached = (List<StoreListing>) pm.detachCopyAll(results);
            query.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public List<StoreListing> findByChannelId(long channelId) {
        
        List<StoreListing> detached = new ArrayList<StoreListing>();
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(StoreListing.class);
            query.setFilter("channelId == channelIdParam");
            query.declareParameters("long channelIdParam");
            @SuppressWarnings("unchecked")
            List<StoreListing> results = (List<StoreListing>) query.execute(channelId);
            if (results.size() > 0)
                detached = (List<StoreListing>) pm.detachCopyAll(results);
            query.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }
    
}
