package com.nncloudtv.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.Poi;

@Service
public class PoiManager {
    
    protected static final Logger log = Logger.getLogger(PoiManager.class.getName());
    
    public Poi save(Poi poi) {
        
        if (poi == null) { return null; }
        
        poi.setUpdateDate(new Date());
        
        return NNF.getPoiDao().save(poi);
    }
    
    public void delete(Poi poi) {
        
        NNF.getPoiDao().delete(poi);
    }
    
    public List<Poi> findByPointId(long pointId) {
        
        return NNF.getPoiDao().findByPointId(pointId);
    }
    
}
