package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.App;
<<<<<<< HEAD
=======
import com.nncloudtv.model.NnChannel;
>>>>>>> 3be3f72fd247f0b0fd20d02e22003200b3016818

public class AppDao extends GenericDao<App> {
    protected static final Logger log = Logger.getLogger(AppDao.class.getName());
    
    public AppDao() {
        super(App.class);
    }

<<<<<<< HEAD
    public List<App> findAllBySphere(String sphere) {
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        List<App> detached = new ArrayList<App>(); 
        try {
            Query q = pm.newQuery(App.class);
            q.setFilter("sphere == sphereParam && featured == featuredParam");
            q.declareParameters("String sphereParam, boolean featuredParam");
            q.setOrdering("position1 asc");
            @SuppressWarnings("unchecked")
            List<App> apps = (List<App>) q.execute(sphere, false);            
            detached = (List<App>)pm.detachCopyAll(apps);
=======
    public List<App> findAllByOsAndSphere(short type, String sphere) {
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        List<App> detached = new ArrayList<App>(); 
        try {
        	Query q = pm.newQuery(App.class);
        	q.setFilter("type == typeParam && sphere == sphereParam && featured == featuredParam");
        	q.declareParameters("short typeParam, String sphereParam, boolean featuredParam");
        	q.setOrdering("position1 asc");
        	@SuppressWarnings("unchecked")
        	List<App> apps = (List<App>) q.execute(type, sphere, false);            
        	detached = (List<App>)pm.detachCopyAll(apps);
>>>>>>> 3be3f72fd247f0b0fd20d02e22003200b3016818
        } finally {
            pm.close();
        }
        return detached;
    }    

<<<<<<< HEAD
    public List<App> findFeaturedBySphere(String sphere) {
=======
    public List<App> findFeaturedByOsAndSphere(short type, String sphere) {
>>>>>>> 3be3f72fd247f0b0fd20d02e22003200b3016818
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        List<App> detached = new ArrayList<App>(); 
        try {
            Query q = pm.newQuery(App.class);
<<<<<<< HEAD
            q.setFilter("sphere == sphereParam && featured == featuredParam");
            q.declareParameters("String sphereParam, boolean featuredParam");
            q.setOrdering("position1 asc");
            @SuppressWarnings("unchecked")
            List<App> apps = (List<App>) q.execute(sphere, true);            
=======
        	q.setFilter("type == typeParam && sphere == sphereParam && featured == featuredParam");
        	q.declareParameters("short typeParam, String sphereParam, boolean featuredParam");
            q.setOrdering("position1 asc");
            @SuppressWarnings("unchecked")
        	List<App> apps = (List<App>) q.execute(type, sphere, true);            
>>>>>>> 3be3f72fd247f0b0fd20d02e22003200b3016818
            detached = (List<App>)pm.detachCopyAll(apps);
        } finally {
            pm.close();
        }
        return detached;
    }    
    
}
