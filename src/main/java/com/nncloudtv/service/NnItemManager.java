package com.nncloudtv.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnItemDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnItem;
import com.nncloudtv.model.NnPurchase;
import com.nncloudtv.web.api.ApiContext;

@Service
public class NnItemManager {
    
    protected static final Logger log = Logger.getLogger(NnItemManager.class.getName());
    
    protected NnItemDao dao = NNF.getItemDao();
    
    public NnItem findById(long id) {
        
        return dao.findById(id);
    }
    
    public NnItem findByProductIdRef(String productIdRef) {
        
        return dao.findByProductIdRef(productIdRef);
    }
    
    public NnItem save(NnItem item) {
        
        Date now = NnDateUtil.now();
                
        if (item.getCreateDate() == null) {
            item.setCreateDate(now);
        }
        item.setUpdateDate(now);
        
        return dao.save(item);
    }
    
    public NnItem findByPurchase(NnPurchase purchase) {
        
        return dao.findById(purchase.getItemId());
    }
    
    public Object composeEachItem(NnItem item) {
        
        String[] obj = {
                String.valueOf(item.getChannelId()),
                item.getProductIdRef(),
                String.valueOf(item.getBillingPlatform()),
        };
        
        return NnStringUtil.getDelimitedStr(obj);
    }
    
    private short os2Platform(String os) {
        
        short platform = NnItem.UNKNOWN;
        if (os == null) {
            
        } else if (os.equals(ApiContext.OS_ANDROID)) {
            
            platform = NnItem.GOOGLEPLAY;
            
        } else if (os.equals(ApiContext.OS_IOS)) {
            
            platform = NnItem.APPSTORE;
        }
        
        return platform;
    }
    
    public List<NnItem> findByMsoAndOs(Mso mso, String os) {
        
        return dao.findByMsoIdAndPlatform(mso.getId(), os2Platform(os));
    }
    
    public List<NnItem> findByChannelId(long channelId) {
        
        return dao.findByChannelId(channelId);
    }
    
    public NnItem findOne(Mso mso, NnChannel channel, String os) {
        
        if (mso == null || channel == null || os == null) { return null; }
        
        return dao.findOne(mso.getId(), os2Platform(os), channel.getId());
    }
}
