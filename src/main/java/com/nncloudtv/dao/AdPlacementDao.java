package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.model.AdPlacement;

public class AdPlacementDao extends GenericDao<AdPlacement> {
    
    protected static final Logger log = Logger.getLogger(AdPlacementDao.class.getName());
    
    public AdPlacementDao() {
        
        super(AdPlacement.class);
    }
    
    @SuppressWarnings("unchecked")
    public List<AdPlacement> findByMso(long msoId) {
        
        List<AdPlacement> detached = new ArrayList<AdPlacement>();
        PersistenceManager pm = getPersistenceManager(); 
        try {
            Query query = pm.newQuery(AdPlacement.class);
            query.setFilter("msoId == msoIdParam && status == 0");
            query.declareParameters("long msoIdParam");
            query.setOrdering("seq asc");
            List<AdPlacement> results = (List<AdPlacement>) query.execute(msoId);
            detached = (List<AdPlacement>) pm.detachCopyAll(results);
        } finally {
            pm.close();
        }
        return detached;
    }
}
