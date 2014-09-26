package com.nncloudtv.dao;

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
    
}
