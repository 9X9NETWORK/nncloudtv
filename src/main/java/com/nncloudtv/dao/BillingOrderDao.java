package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.model.BillingOrder;

public class BillingOrderDao extends GenericDao<BillingOrder> {
    
    protected static final Logger log = Logger.getLogger(BillingOrderDao.class.getName());
    
    public BillingOrderDao() {
        
        super(BillingOrder.class);
    }
    
    public List<BillingOrder> findByType(short type) {
        
        List<BillingOrder> detached = new ArrayList<BillingOrder>();
        PersistenceManager pm = getPersistenceManager();
        Query q = null;
        try {
            q = pm.newQuery(BillingOrder.class);
            q.setFilter("type == typeParam");
            q.declareParameters("short typeParam");
            @SuppressWarnings("unchecked")
            List<BillingOrder> orders = (List<BillingOrder>) q.execute(type);
            detached = (List<BillingOrder>) pm.detachCopyAll(orders);
            q.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }
    
}
