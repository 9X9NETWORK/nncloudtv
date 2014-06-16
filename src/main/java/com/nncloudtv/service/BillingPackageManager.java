package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.BillingPackageDao;
import com.nncloudtv.model.BillingPackage;

@Service
public class BillingPackageManager {
    
    protected static final Logger log = Logger.getLogger(BillingPackageManager.class.getName());
    
    BillingPackageDao dao;
    
    public BillingPackageManager() {
        
        dao = new BillingPackageDao();
    }
    
    public List<BillingPackage> findAll() {
        
        return dao.findAll();
    }
    
    public List<BillingPackage> findByIds(String[] packageIds) {
        
        List<BillingPackage> results = new ArrayList<BillingPackage>();
        
        if (packageIds == null) {
            return results;
        }
        
        for (String packageId : packageIds) {
            
            BillingPackage billingPackage = findById(packageId);
            if (billingPackage != null) {
                
                results.add(billingPackage);
            }
        }
        
        return results;
    }
    
    public BillingPackage findById(String idStr) {
        
        if (idStr == null) return null;
        
        long packageId = 0;
        try {
            packageId = Long.valueOf(idStr);
            
        } catch(NumberFormatException e) {
        }
        
        return dao.findById(packageId);
    }
    
    public BillingPackage save(BillingPackage billingPackage) {
        
        Date now = new Date();
        if (billingPackage.getCreateDate() == null) {
            
            billingPackage.setCreateDate(now);
        }
        billingPackage.setUpdateDate(now);
        
        return dao.save(billingPackage);
    }
}
