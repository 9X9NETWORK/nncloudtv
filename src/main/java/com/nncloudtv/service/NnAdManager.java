package com.nncloudtv.service;

import java.util.Date;
import java.util.logging.Logger;

import com.nncloudtv.dao.NnAdDao;
import com.nncloudtv.model.NnAd;
import com.nncloudtv.model.NnEpisode;

public class NnAdManager {
    
    protected static final Logger log = Logger.getLogger(NnAdManager.class.getName());
    
    private NnAdDao dao = new NnAdDao();
    
    public void delete(NnAd nnad) {
        
        if (nnad == null) {
            return;
        }
        
        dao.delete(nnad);
    }
    
    public NnAd save(NnAd nnad, NnEpisode episode) {
        
        if (nnad == null || episode == null) {
            return null;
        }
        
        Date now = new Date();
        
        if (nnad.getCreateDate() == null) {
            nnad.setCreateDate(now);
        }
        
        nnad.setUpdateDate(now);
        nnad = dao.save(nnad);
        
        return nnad;
    }
    
}
