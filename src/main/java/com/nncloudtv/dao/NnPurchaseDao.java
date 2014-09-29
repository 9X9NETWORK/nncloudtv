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
            query.setFilter("userIdStr == userIdStrParam && status == " + NnPurchase.ACTIVE);
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
    
    public NnPurchase findBySubscriptionIdRef(String subscriptionIdRef) {
        
        NnPurchase detached = null;
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(NnPurchase.class);
            query.setFilter("subscriptionIdRef == subscriptionIdRefParam");
            query.declareParameters("String subscriptionIdRefParam");
            query.setOrdering("updateDate desc");
            @SuppressWarnings("unchecked")
            List<NnPurchase> results = (List<NnPurchase>) query.execute(subscriptionIdRef);
            if (results.size() > 0) {
                detached = results.get(0);
            }
            query.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }
}
