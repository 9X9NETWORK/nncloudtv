package com.nncloudtv.dao;

import java.util.logging.Logger;

import com.nncloudtv.dao.GenericDao;
import com.nncloudtv.model.billing.BillingOrder;

public class BillingProfileDao extends GenericDao<BillingOrder> {
    
    protected static final Logger log = Logger.getLogger(BillingProfileDao.class.getName());
    
    public BillingProfileDao() {
        
        super(BillingOrder.class);
    }
    
}
