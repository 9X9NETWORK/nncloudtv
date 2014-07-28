package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.model.MsoPromotion;

public class MsoPromotionDao extends GenericDao<MsoPromotion> {
    
    protected static final Logger log = Logger.getLogger(MsoPromotion.class.getName());
    
    public MsoPromotionDao() {
        
        super(MsoPromotion.class);
    }
    
    @SuppressWarnings("unchecked")
    public List<MsoPromotion> findByMso(long msoId) {
        
        List<MsoPromotion> detached = new ArrayList<MsoPromotion>();
        PersistenceManager pm = getPersistenceManager(); 
        try {
            Query query = pm.newQuery(MsoPromotion.class);
            query.setFilter("msoId == msoIdParam");
            query.declareParameters("long msoIdParam");
            query.setOrdering("seq asc");
            List<MsoPromotion> results = (List<MsoPromotion>) query.execute(msoId);
            detached = (List<MsoPromotion>) pm.detachCopyAll(results);
        } finally {
            pm.close();
        }
        return detached;
    }
    
    @SuppressWarnings("unchecked")
    public List<MsoPromotion> findByMsoAndType(long msoId, short type) {
        
        List<MsoPromotion> detached = new ArrayList<MsoPromotion>();
        PersistenceManager pm = getPersistenceManager(); 
        try {
            Query query = pm.newQuery(MsoPromotion.class);
            query.setFilter("msoId == msoIdParam && type == typeParam");
            query.declareParameters("long msoIdParam, short typeParam");
            query.setOrdering("seq asc");
            List<MsoPromotion> results = (List<MsoPromotion>) query.execute(msoId, type);
            detached = (List<MsoPromotion>) pm.detachCopyAll(results);
        } finally {
            pm.close();
        }
        return detached;
    }
}
