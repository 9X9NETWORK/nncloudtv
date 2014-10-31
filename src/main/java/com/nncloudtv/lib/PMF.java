package com.nncloudtv.lib;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import com.nncloudtv.model.BillingOrder;
import com.nncloudtv.model.BillingPackage;
import com.nncloudtv.model.BillingProfile;
import com.nncloudtv.model.NnItem;
import com.nncloudtv.model.NnPurchase;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserSubscribe;
import com.nncloudtv.model.NnUserSubscribeGroup;
import com.nncloudtv.model.Pdr;

public final class PMF {

    //non-asia    
    private static PersistenceManagerFactory pmfInstanceNnUser1 = null;
    //asia
    private static PersistenceManagerFactory pmfInstanceNnUser2 = null;
    //others
    private static PersistenceManagerFactory pmfInstanceContent = null;
    private static PersistenceManagerFactory pmfInstanceAnalytics = null;
    private static PersistenceManagerFactory pmfInstanceRecommend = null;
    private static PersistenceManagerFactory pmfInstanceBilling = null;
    
    public PMF() { }
    
    public static PersistenceManagerFactory get(@SuppressWarnings("rawtypes") Class cls) {
        
        if (cls.equals(Pdr.class)) {
            
            return PMF.getAnalytics();
            
        } else if (cls.equals(BillingOrder.class) || cls.equals(BillingPackage.class) || cls.equals(BillingProfile.class) ||
                   cls.equals(NnPurchase.class)   || cls.equals(NnItem.class)) {
            
            return PMF.getBilling();
            
        } else if (cls.equals(NnUser.class) || cls.equals(NnUserSubscribe.class) || cls.equals(NnUserSubscribeGroup.class)) {
            
            throw new IllegalArgumentException("user related db are sharded.");
        }
        
        return PMF.getContent();
    }
    
    public static PersistenceManagerFactory getNnUser1() {
        if (pmfInstanceNnUser1 == null) {
            pmfInstanceNnUser1 = JDOHelper.getPersistenceManagerFactory("datanucleus_nnuser1.properties");
        }
        return pmfInstanceNnUser1;
    }
    
    public static PersistenceManagerFactory getNnUser2() {
        if (pmfInstanceNnUser2 == null)
            pmfInstanceNnUser2 = JDOHelper.getPersistenceManagerFactory("datanucleus_nnuser2.properties");
        return pmfInstanceNnUser2;
    }
    
    public static PersistenceManagerFactory getContent() {
        if (pmfInstanceContent == null)
            pmfInstanceContent = JDOHelper.getPersistenceManagerFactory("datanucleus_content.properties");
        return pmfInstanceContent;
    }
    
    public static PersistenceManagerFactory getAnalytics() {
        if (pmfInstanceAnalytics == null)
            pmfInstanceAnalytics = JDOHelper.getPersistenceManagerFactory("datanucleus_analytics.properties");
        return pmfInstanceAnalytics;
    }
    
    public static PersistenceManagerFactory getRecommend() {
        if (pmfInstanceRecommend == null)
            pmfInstanceRecommend = JDOHelper.getPersistenceManagerFactory("datanucleus_recommend.properties");
        return pmfInstanceRecommend;
    }
    
    public static PersistenceManagerFactory getBilling() {
        if (pmfInstanceBilling == null)
            pmfInstanceBilling = JDOHelper.getPersistenceManagerFactory("datanucleus_billing.properties");
        return pmfInstanceBilling;
    }
    
}
