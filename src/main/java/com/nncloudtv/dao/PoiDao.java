package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.Poi;
import com.nncloudtv.model.PoiPoint;

public class PoiDao extends GenericDao<Poi> {
    
    protected static final Logger log = Logger.getLogger(PoiDao.class.getName());
    
    public PoiDao() {
        super(Poi.class);
    }
    
    public List<Poi> findByPointId(long pointId) {
        
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        
        List<Poi> results = new ArrayList<Poi>();
        try {
            Query query = pm.newQuery(Poi.class);
            query.setFilter("pointId == pointIdParam");
            query.declareParameters("long pointIdParam");
            query.setOrdering("updateDate desc");
            @SuppressWarnings("unchecked")
            List<Poi> pois = (List<Poi>) query.execute(pointId);
            if (pois != null && pois.size() > 0) {
                results = (List<Poi>) pm.detachCopyAll(pois);
            }
            query.closeAll();
        } finally {
            pm.close();
        }
        return results;
    }
    
    // NOTE: not used
    public List<Poi> findCurrentByChannel(long channelId) {
        
        String query = "SELECT * FROM poi a1 "
                     + "   INNER JOIN (SELECT DISTINCT pp.id "
                     + "                 FROM poi_point pp, poi poi "
                     + "                WHERE pp.active = true "
                     + "                  AND pp.targetId = " + channelId
                     + "                  AND poi.pointId = pp.id "
                     + "                  AND pp.type = " + PoiPoint.TYPE_CHANNEL
                     + "                  AND poi.pointId = pp.id"
////                 + "                  AND now() > poi.startDate "
////                 + "                  AND now() < poi.endDate"
                     + "              ) a2"
                     + "           ON a1.id = a2.id";
        
        return sql(query);
    }
    
    // NOTE: not used
    public List<Poi> findCurrentByProgram(long programId) {
        
        String query = "SELECT * FROM poi a1 "
                     + "   INNER JOIN (SELECT DISTINCT pp.id "
                     + "                 FROM poi_point pp, poi poi " 
                     + "                WHERE pp.active = true "
                     + "                  AND pp.targetId = " + programId
                     + "                  AND poi.pointId = pp.id "
                     + "                  AND pp.type = " + PoiPoint.TYPE_SUBEPISODE
                     + "                  AND poi.pointId = pp.id "
////                 + "                  AND now() > poi.startDate "
////                 + "                  AND now() < poi.endDate "
                     + "              ) a2"
                     + "           ON a1.id = a2.id";
        
        return sql(query);
    }
    
    public List<Poi> findByCompaignId(long campaignId) {
        
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        
        List<Poi> results = new ArrayList<Poi>();
        try {
            Query query = pm.newQuery(Poi.class);
            query.setFilter("campaignId == campaignIdParam");
            query.declareParameters("long campaignIdParam");
            query.setOrdering("updateDate desc");
            @SuppressWarnings("unchecked")
            List<Poi> pois = (List<Poi>) query.execute(campaignId);
            if (pois != null && pois.size() > 0) {
                results = (List<Poi>) pm.detachCopyAll(pois);
            }
        } finally {
            pm.close();
        }
        return results;
    }

}