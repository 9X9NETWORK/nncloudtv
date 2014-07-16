package com.nncloudtv.dao;

import java.util.logging.Logger;

import com.nncloudtv.model.AdPlacement;

public class AdPlacementDao extends GenericDao<AdPlacement> {
    
    protected static final Logger log = Logger.getLogger(AdPlacementDao.class.getName());
    
    public AdPlacementDao() {
        
        super(AdPlacement.class);
    }
    
}
