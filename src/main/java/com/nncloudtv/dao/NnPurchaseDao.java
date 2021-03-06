package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.model.NnPurchase;

public class NnPurchaseDao extends GenericDao<NnPurchase> {
    
    protected static final Logger log = Logger.getLogger(NnPurchaseDao.class.getName());
    
    public NnPurchaseDao() {
        
        super(NnPurchase.class);
    }
    
    public List<NnPurchase> findByUserIdStr(String userIdStr) {
        
        List<NnPurchase> detached = new ArrayList<NnPurchase>();
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(NnPurchase.class);
            query.setFilter("userIdStr == userIdStrParam");
            query.declareParameters("String userIdStrParam");
            query.setOrdering("updateDate desc");
            @SuppressWarnings("unchecked")
            List<NnPurchase> results = (List<NnPurchase>) query.execute(userIdStr);
            detached = (List<NnPurchase>) pm.detachCopyAll(results);
            query.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public NnPurchase findByUserAndItem(String userIdStr, long itemId) {
        
        NnPurchase detached = null;
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(NnPurchase.class);
            query.setFilter("userIdStr == userIdStrParam && itemId == itemIdParam");
            query.declareParameters("String userIdStrParam, long itemIdParam");
            query.setOrdering("updateDate desc");
            @SuppressWarnings("unchecked")
            List<NnPurchase> results = (List<NnPurchase>) query.execute(userIdStr, itemId);
            if (results.size() > 0) {
                detached = pm.detachCopy(results.get(0));
            }
            query.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public List<NnPurchase> findAllActive() {
        
        List<NnPurchase> detached = new ArrayList<NnPurchase>();
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(NnPurchase.class);
            query.setFilter("verified == verifiedParam && status == statusParam");
            query.declareParameters("boolean verifiedParam, short statusParam");
            query.setOrdering("updateDate desc");
            @SuppressWarnings("unchecked")
            List<NnPurchase> results = (List<NnPurchase>) query.execute(true, NnPurchase.ACTIVE);
            detached = (List<NnPurchase>) pm.detachCopyAll(results);
            query.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public List<NnPurchase> findByItemId(long itemId) {
        
        List<NnPurchase> detached = new ArrayList<NnPurchase>();
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(NnPurchase.class);
            query.setFilter("itemId == itemIdParam");
            query.declareParameters("String itemIdParam");
            @SuppressWarnings("unchecked")
            List<NnPurchase> results = (List<NnPurchase>) query.execute(itemId);
            detached = (List<NnPurchase>) pm.detachCopyAll(results);
            query.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }
}
