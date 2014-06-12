package com.nncloudtv.dao;

import java.util.logging.Logger;

import com.nncloudtv.dao.GenericDao;
import com.nncloudtv.model.billing.BillingOrder;

public class BillingPackageDao extends GenericDao<BillingOrder> {
    
    protected static final Logger log = Logger.getLogger(BillingPackageDao.class.getName());
    
    public BillingPackageDao() {
        
        super(BillingOrder.class);
    }
    
}
