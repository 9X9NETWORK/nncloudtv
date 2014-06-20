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
    
    public List<BillingOrder> findByStatus(short status) {
        
        List<BillingOrder> detached = new ArrayList<BillingOrder>();
        PersistenceManager pm = getPersistenceManager();
        Query q = null;
        try {
            q = pm.newQuery(BillingOrder.class);
            q.setFilter("status == statusParam");
            q.declareParameters("short statusParam");
            @SuppressWarnings("unchecked")
            List<BillingOrder> orders = (List<BillingOrder>) q.execute(status);
            detached = (List<BillingOrder>) pm.detachCopyAll(orders);
            q.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }
    
}
