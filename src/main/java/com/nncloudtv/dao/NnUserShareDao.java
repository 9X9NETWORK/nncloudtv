package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserShare;

public class NnUserShareDao extends GenericDao<NnUserShare> {
    
    public NnUserShareDao() {
        super(NnUserShare.class);
    }
    
    protected static final Logger log = Logger.getLogger(NnUserShare.class.getName());
    
    public List<NnUserShare> findByUser(NnUser user) {
        List<NnUserShare> detached = new ArrayList<NnUserShare>();
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        try {
            Query query = pm.newQuery(NnUserShare.class);
            query.setFilter("userId == userIdParam");
            query.declareParameters("long userIdParam");
            @SuppressWarnings("unchecked")
            List<NnUserShare> results = (List<NnUserShare>)query.execute(user.getId());
            log.info("ipg count = " + results.size());
            detached = (List<NnUserShare>)pm.detachCopyAll(results);
        } finally {
            pm.close();
        }
        return detached;
    }
    
}
