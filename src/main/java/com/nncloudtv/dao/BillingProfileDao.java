package com.nncloudtv.dao;

import java.util.logging.Logger;

import com.nncloudtv.dao.GenericDao;
import com.nncloudtv.model.BillingProfile;

public class BillingProfileDao extends GenericDao<BillingProfile> {
    
    protected static final Logger log = Logger.getLogger(BillingProfileDao.class.getName());
    
    public BillingProfileDao() {
        
        super(BillingProfile.class);
    }
    
}
