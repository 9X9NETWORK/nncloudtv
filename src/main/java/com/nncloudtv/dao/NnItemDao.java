package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.model.NnItem;

public class NnItemDao extends GenericDao<NnItem> {
    
    protected static final Logger log = Logger.getLogger(NnItemDao.class.getName());
    
    public NnItemDao() {
        
        super(NnItem.class);
    }
    
    @SuppressWarnings("unchecked")
    public List<NnItem> findByMsoIdAndPlatform(long msoId, short platform) {
        
        List<NnItem> detached = new ArrayList<NnItem>();
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(NnItem.class);
            query.setFilter("msoId == msoIdParam && billingPlatform == billingPlatformParam");
            query.declareParameters("long msoIdParam, short billingPlatformParam");
            detached = (List<NnItem>) query.execute(msoId, platform);
            detached = (List<NnItem>) pm.detachCopyAll(detached);
            query.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }
    
    @SuppressWarnings("unchecked")
    public NnItem findOne(long msoId, short platform, long channelId) {
        
        NnItem detached = null;
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(NnItem.class);
            query.setFilter("msoId == msoIdParam && billingPlatform == billingPlatformParam && channelId == channelIdParam");
            query.declareParameters("long msoIdParam, short billingPlatformParam, long channelIdParam");
            List<NnItem> results = (List<NnItem>) query.execute(msoId, platform, channelId);
            if (results.size() > 0) {
                detached = pm.detachCopy(results.get(0));
            }
            query.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }
    
    @SuppressWarnings("unchecked")
    public NnItem findByProductIdRef(String productIdRef) {
        
        NnItem detached = null;
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(NnItem.class);
            query.setFilter("productIdRef == productIdRefParam");
            query.declareParameters("String productIdRefParam");
            List<NnItem> results = (List<NnItem>) query.execute(productIdRef);
            if (results.size() > 0) {
                detached = pm.detachCopy(results.get(0));
            }
            query.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public List<NnItem> findByChannelId(long channelId) {
        
        List<NnItem> detached = new ArrayList<NnItem>();
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(NnItem.class);
            query.setFilter("channelId == channelIdParam");
            query.declareParameters("long channelIdParam");
            query.setOrdering("billingPlatform desc");
            @SuppressWarnings("unchecked")
            List<NnItem> results = (List<NnItem>) query.execute(channelId);
            detached = (List<NnItem>) pm.detachCopyAll(results);
            query.closeAll();
        } finally {
            pm.close();
        }
        
        return detached;
    }
    
    public List<NnItem> findByChannelIdAndMsoId(long channelId, long msoId) {
        
        List<NnItem> detached = new ArrayList<NnItem>();
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(NnItem.class);
            query.setFilter("channelId == channelIdParam && msoId == msoIdParam");
            query.declareParameters("long channelIdParam, long msoIdParam");
            query.setOrdering("billingPlatform desc");
            @SuppressWarnings("unchecked")
            List<NnItem> results = (List<NnItem>) query.execute(channelId, msoId);
            detached = (List<NnItem>) pm.detachCopyAll(results);
            query.closeAll();
        } finally {
            pm.close();
        }
        
        return detached;
    }
    
    public List<NnItem> findTerminateItems() {
        
        return sql("SELECT * FROM NnItem WHERE status = 0 AND terminateDate IS NOT NULL");
    }
    
}
