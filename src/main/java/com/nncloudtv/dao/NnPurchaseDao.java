package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.model.NnPurchase;
import com.nncloudtv.model.NnUser;

public class NnPurchaseDao extends GenericDao<NnPurchase> {
    
    protected static final Logger log = Logger.getLogger(NnPurchaseDao.class.getName());
    
    public NnPurchaseDao() {
        
        super(NnPurchase.class);
    }
    
    public List<NnPurchase> findByUserIdStr(String userIdStr) {
        List<NnPurchase> detached = new ArrayList<NnPurchase>();
        
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(NnUser.class);
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
}
