package com.nncloudtv.service;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.AdPlacementDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.AdPlacement;

@Service
public class AdPlacementManager {
    
    protected static final Logger log = Logger.getLogger(AdPlacementManager.class.getName());
    
    protected AdPlacementDao dao = NNF.getAdDao();
    
    public List<AdPlacement> findByMso(long msoId) {
        
        return dao.findByMso(msoId);
    }
}
