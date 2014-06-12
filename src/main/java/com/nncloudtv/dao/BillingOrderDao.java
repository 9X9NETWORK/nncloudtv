package com.nncloudtv.dao;

import java.util.logging.Logger;

import com.nncloudtv.model.BillingOrder;

public class BillingOrderDao extends GenericDao<BillingOrder> {
    
    protected static final Logger log = Logger.getLogger(BillingOrderDao.class.getName());
    
    public BillingOrderDao() {
        
        super(BillingOrder.class);
    }
    
}
