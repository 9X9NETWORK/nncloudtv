package com.nncloudtv.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.MsoPromotionDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.MsoPromotion;

@Service
public class MsoPromotionManager {
    
    protected static final Logger log = Logger.getLogger(MsoPromotionManager.class.getName());
    
    protected MsoPromotionDao dao = NNF.getMsoPromotionDao();
    
    public List<MsoPromotion> findByMso(long msoId) {
        
        return dao.findByMso(msoId);
    }
    
    public List<MsoPromotion> findByMsoAndType(long msoId, short type) {
        
        return dao.findByMsoAndType(msoId, type);
    }
    
    public MsoPromotion save(MsoPromotion promotion) {
        
        Date now = NnDateUtil.now();
        
        if (promotion.getCreateDate() == null) {
            
            promotion.setCreateDate(now);
        }
        promotion.setUpdateDate(now);
        
        return dao.save(promotion);
    }
    
    public MsoPromotion findById(String promotionIdStr) {
        
        return dao.findById(promotionIdStr);
    }
    
    public void delete(MsoPromotion promotion) {
        
        dao.delete(promotion);
    }
}
