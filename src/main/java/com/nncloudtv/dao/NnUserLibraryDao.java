package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.model.NnUserLibrary;

public class NnUserLibraryDao extends GenericDao<NnUserLibrary> {
    
    protected static final Logger log = Logger.getLogger(NnUserLibraryDao.class.getName());
    
    public NnUserLibraryDao() {
        
        super(NnUserLibrary.class);
    }
    
    @SuppressWarnings("unchecked")
    public List<NnUserLibrary> findByUserIdStr(String userIdStr) {
        
        List<NnUserLibrary> results = new ArrayList<NnUserLibrary>();
        PersistenceManager pm = getPersistenceManager();
        
        try {
            Query query = pm.newQuery(NnUserLibrary.class);
            
            query.setOrdering("seq asc");
            query.setFilter("userIdStr == userIdStrParam");
            query.declareParameters("String userIdStrParam");
            
            results = (List<NnUserLibrary>) query.execute(userIdStr);
            results = (List<NnUserLibrary>) pm.detachCopyAll(results);
            
            query.closeAll();
            
        } finally {
            
            pm.close();
        }
        
        return results;
    }
    
}
