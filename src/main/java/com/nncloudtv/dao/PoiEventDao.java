package com.nncloudtv.dao;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.PoiEvent;

public class PoiEventDao extends GenericDao<PoiEvent> {

    protected static final Logger log = Logger.getLogger(PoiEventDao.class.getName());
    
    public PoiEventDao() {
        super(PoiEvent.class);
    }
    
    public PoiEvent findByPointId(long pointId) {
        PoiEvent detached = null;
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        try {
            String sql = "select * " +
                         "  from poi_event " +
                         " where id in (select eventId " +
                                        " from poi " +
                                        " where pointId = " + pointId + ")";
            log.info("sql:" + sql);
            Query query = pm.newQuery("javax.jdo.query.SQL", sql);
            query.setClass(PoiEvent.class);
            @SuppressWarnings("unchecked")
            List<PoiEvent> results = (List<PoiEvent>) query.execute();
            if (results.size() > 0) {
                detached = pm.detachCopy(results.get(0));
            }
        } finally {
            pm.close();
        } 
        return detached;
    }
    
    public PoiEvent findByPoiId(long poiId) {
        PoiEvent result = null;
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        try {
            //select * 
            //  from poi_event 
            // where id= (select eventId from poi where id=1);
            String sql = "select * " +
                          " from poi_event " +
                         " where id = (select eventId " +
                                       " from poi " +
                                      " where id = " + poiId + ")";
            log.info("sql:" + sql);
            Query query = pm.newQuery("javax.jdo.query.SQL", sql);
            query.setClass(PoiEvent.class);
            @SuppressWarnings("unchecked")
            List<PoiEvent> poiEvents = (List<PoiEvent>) query.execute();
            if (poiEvents.size() > 0)
                result = pm.detachCopy(poiEvents.get(0));
            
        } finally {
            pm.close();
        } 
        return result;
    }
    
}

