package com.nncloudtv.dao;

import java.util.logging.Logger;

import com.nncloudtv.dao.GenericDao;
import com.nncloudtv.model.billing.BillingPackage;

public class BillingPackageDao extends GenericDao<BillingPackage> {
    
    protected static final Logger log = Logger.getLogger(BillingPackageDao.class.getName());
    
    public BillingPackageDao() {
        
        super(BillingPackage.class);
    }
    
}
