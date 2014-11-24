package com.nncloudtv.dao;

import java.util.List;
import java.util.logging.Logger;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.App;

public class AppDao extends GenericDao<App> {
    protected static final Logger log = Logger.getLogger(AppDao.class.getName());
    
    public AppDao() {
        super(App.class);
    }
    
    public List<App> findAllBySphere(String sphere, long msoId) {
        
        String query = " SELECT * FROM app"
                     + "         WHERE sphere = " + NnStringUtil.escapedQuote(sphere)
                     + "           AND msoId != " + msoId
                     + "           AND featured = " + false
                     + "      ORDER BY position1 ASC";
        
        return sql(query);
    }
    
    public List<App> findFeaturedBySphere(String sphere, long msoId) {
        
        String query = " SELECT * FROM app "
                     + "         WHERE sphere = " + NnStringUtil.escapedQuote(sphere)
                     + "           AND msoId != " + msoId
                     + "           AND featured = " + true
                     + "      ORDER BY position2 ASC";
        
        return sql(query);
    }
    
}
