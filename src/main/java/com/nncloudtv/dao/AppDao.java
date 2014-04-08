package com.nncloudtv.dao;

import java.util.List;
import java.util.logging.Logger;

import com.nncloudtv.model.App;

public class AppDao extends GenericDao<App> {
    protected static final Logger log = Logger.getLogger(AppDao.class.getName());
    
    public AppDao() {
        super(App.class);
    }

    public List<App> findAllBySphere(String sphere, long msoId) {
        String query = " select * from app"
                +       " where sphere = '" + sphere + "'"
                +         " and msoId != " + msoId
                +         " and featured = " + false
                +    " order by position1 asc";
   
        return sql(query);
    }    

    public List<App> findFeaturedBySphere(String sphere, long msoId) {
        String query = " select * from app"
                +       " where sphere = '" + sphere + "'"
                +         " and msoId != " + msoId
                +         " and featured = " + true
                +    " order by position2 asc";
   
        return sql(query);
    }    
    
}
