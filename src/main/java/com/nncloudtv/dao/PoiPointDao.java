package com.nncloudtv.dao;

import java.util.List;
import java.util.logging.Logger;

import com.nncloudtv.model.PoiPoint;

public class PoiPointDao extends GenericDao<PoiPoint> {

    protected static final Logger log = Logger.getLogger(PoiPointDao.class.getName());
    
    public PoiPointDao() {
        super(PoiPoint.class);
    }
    
    public List<PoiPoint> findByChannel(long channelId) {
        
        return sql(String.format("SELECT * FROM poi_point WHERE targetId = %d AND type = %d", channelId, PoiPoint.TYPE_CHANNEL));
    }
    
    public List<PoiPoint> findByProgramId(long programId) {
        
        return sql(String.format("SELECT * FROM poi_point WHERE targetId = %d AND type = %d", programId, PoiPoint.TYPE_SUBEPISODE));
    }

    //current: poi_point is active and poi is within date range
    public List<PoiPoint> findCurrentByChannelId(long channelId) {
        
        String query = "SELECT * FROM poi_point a1 "
                     + "   INNER JOIN ( "
                     + "               SELECT DISTINCT pp.id "
                     + "                 FROM poi_point pp, poi poi, poi_event e " 
                     + "                WHERE pp.active = TRUE "
                     + "                  AND pp.targetId = " + channelId
                     + "                  AND poi.eventId = e.id "
                     + "                  AND poi.pointId = pp.id "
                     + "                  AND pp.type = " + PoiPoint.TYPE_CHANNEL
                     + "                  AND poi.pointId = pp.id "   
////                 + "                  AND NOW() > poi.startDate "
////                 + "                  AND NOW() < poi.endDate "
                     + "              ) a2 "
                     + "           ON a1.id = a2.id";
        
        return sql(query);
    }
    
    //current: poi_point is active and poi is within date range
    public List<PoiPoint> findCurrentByProgramId(long programId) {
        
        String query = "SELECT * FROM poi_point a1 "
                     + "   INNER JOIN ( "  
                     + "               SELECT DISTINCT pp.id "
                     + "                 FROM poi_point pp, poi poi, poi_event e " 
                     + "                WHERE pp.active = TRUE "
                     + "                  AND pp.targetId = " + programId
                     + "                  AND poi.eventId = e.id "
                     + "                  AND poi.pointId = pp.id "
                     + "                  AND pp.type = " + PoiPoint.TYPE_SUBEPISODE
                     + "                  AND poi.pointId = pp.id "   
////                 + "                  AND NOW() > poi.startDate "
////                 + "                  AND NOW() < poi.endDate "
                     + "              ) a2"
                     + "           ON a1.id = a2.id";
        
        return sql(query);
    }
    
}
