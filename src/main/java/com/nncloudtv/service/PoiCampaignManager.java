package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.Poi;
import com.nncloudtv.model.PoiCampaign;

@Service
public class PoiCampaignManager {
    
    protected static final Logger log = Logger.getLogger(PoiCampaignManager.class.getName());
    
    public List<PoiCampaign> findByUserId(long userId) {
        
        return NNF.getPoiCampaignDao().findByUserId(userId);
    }
    
    public PoiCampaign save(PoiCampaign campaign) {
        
        if (campaign == null) {
            return null;
        }
        
        Date now = NnDateUtil.now();
        if (campaign.getCreateDate() == null) {
            campaign.setCreateDate(now);
        }
        campaign.setUpdateDate(now);
        
        PoiCampaign result = NNF.getPoiCampaignDao().save(campaign);
        
        return result;
    }
    
    // TODO: move
    public List<Poi> findPoisByCampaignId(Long campaignId) {
        
        if (campaignId == null) {
            return new ArrayList<Poi>();
        }
        
        List<Poi> results = NNF.getPoiDao().findByCompaignId(campaignId);
        if (results == null) {
            return new ArrayList<Poi>();
        }
        
        return results;
    }
    
    public PoiCampaign findById(Long campaignId) {
        
        return NNF.getPoiCampaignDao().findById(campaignId);
    }
    
    public Poi findPoiById(Long poiId) {
        
        return NNF.getPoiDao().findById(poiId);
    }
    
    public void delete(PoiCampaign campaign) {
        
        if (campaign == null) {
            return ;
        }
        
        // delete pois
        List<Poi> pois = findPoisByCampaignId(campaign.getId());
        if (pois != null && pois.size() > 0) {
            NNF.getPoiDao().deleteAll(pois);
        }
        
        NNF.getPoiCampaignDao().delete(campaign);
    }

}
