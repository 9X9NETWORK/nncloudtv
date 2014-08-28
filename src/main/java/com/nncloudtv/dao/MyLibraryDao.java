package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.model.MyLibrary;

public class MyLibraryDao extends GenericDao<MyLibrary> {
    
    protected static final Logger log = Logger.getLogger(MyLibraryDao.class.getName());
    
    public MyLibraryDao() {
        
        super(MyLibrary.class);
    }
    
    @SuppressWarnings("unchecked")
    public List<MyLibrary> findByUserIdStr(String userIdStr) {
        
        List<MyLibrary> results = new ArrayList<MyLibrary>();
        PersistenceManager pm = getPersistenceManager();
        
        try {
            Query query = pm.newQuery(MyLibrary.class);
            
            query.setOrdering("seq asc");
            query.setFilter("userIdStr == userIdStrParam");
            query.declareParameters("String userIdStrParam");
            
            results = (List<MyLibrary>) query.execute(userIdStr);
            results = (List<MyLibrary>) pm.detachCopyAll(results);
            
            query.closeAll();
            
        } finally {
            
            pm.close();
        }
        
        return results;
    }
    
    @SuppressWarnings("unchecked")
    public List<MyLibrary> findByMsoId(long msoId) {
        
        List<MyLibrary> results = new ArrayList<MyLibrary>();
        PersistenceManager pm = getPersistenceManager();
        
        try {
            Query query = pm.newQuery(MyLibrary.class);
            
            query.setOrdering("seq asc");
            query.setFilter("msoId == msoIdParam");
            query.declareParameters("long msoIdParam");
            
            results = (List<MyLibrary>) query.execute(msoId);
            results = (List<MyLibrary>) pm.detachCopyAll(results);
            
            query.closeAll();
            
        } finally {
            
            pm.close();
        }
        
        return results;
    }
    
}
