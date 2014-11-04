package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.model.NnChannelPref;

public class NnChannelPrefDao extends GenericDao<NnChannelPref> {
    
    protected static final Logger log = Logger.getLogger(NnChannelPref.class.getName());
    
    public NnChannelPrefDao() {
        super(NnChannelPref.class);
    }
    
    @SuppressWarnings("unchecked")
    public List<NnChannelPref> findByChannelId(long channelId) {
        
        List<NnChannelPref> results = new ArrayList<NnChannelPref>();
        PersistenceManager pm = getPersistenceManager();
        
        try {
            Query query = pm.newQuery(NnChannelPref.class);
            query.setFilter("channelId == channelIdParam");
            query.declareParameters("long channelIdParam");
            results = (List<NnChannelPref>) query.execute(channelId);
            results = (List<NnChannelPref>) pm.detachCopyAll(results);
        } catch (JDOObjectNotFoundException e) {
        } finally {
            pm.close();
        }
        return results;
    }
    
    @SuppressWarnings("unchecked")
    public List<NnChannelPref> findByChannelIdAndItem(long channelId, String item) {
        
        List<NnChannelPref> results = new ArrayList<NnChannelPref>();
        PersistenceManager pm = getPersistenceManager();
        
        try {
            Query query = pm.newQuery(NnChannelPref.class);
            query.setFilter("channelId == channelIdParam && item == itemParam");
            query.declareParameters("long channelIdParam, String itemParam");
            results = (List<NnChannelPref>) query.execute(channelId, item);
            results = (List<NnChannelPref>) pm.detachCopyAll(results);
            query.closeAll();
        } catch (JDOObjectNotFoundException e) {
        } finally {
            pm.close();
        }
        return results;
    }
}
