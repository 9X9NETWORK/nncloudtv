package com.nncloudtv.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.nncloudtv.dao.NnPurchaseDao;
import com.nncloudtv.lib.AppStoreLib;
import com.nncloudtv.lib.GooglePlayLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnItem;
import com.nncloudtv.model.NnPurchase;
import com.nncloudtv.model.NnUser;

@Service
public class NnPurchaseManager {
    
    protected static final Logger log = Logger.getLogger(NnPurchaseManager.class.getName());
    
    protected NnPurchaseDao dao = NNF.getPurchaseDao();
    
    public List<NnPurchase> findByUser(NnUser user) {
        
        if (user == null) {
            return null;
        }
        
        return dao.findByUserIdStr(user.getIdStr());
    }
    
    public void verifyPurchase(NnPurchase purchase, boolean isProduction) {
        
        if (purchase == null) { return; }
        log.info("verify purchase, purchaseId = " + purchase.getId());
        NnItem item = NNF.getItemMngr().findById(purchase.getItemId());
        
        if (item.getBillingPlatform() == NnItem.GOOGLEPLAY) {
            
            try {
                
                purchase.setVerified(false);
                SubscriptionPurchase subscription = GooglePlayLib.getSubscriptionPurchase(purchase);
                if (subscription == null) {
                    
                    log.warning("fail to get subscriptionPurchase");
                    return;
                }
                purchase.setExpireDate(new Date(subscription.getExpiryTimeMillis()));
                purchase.setVerified(true);
                if (purchase.getExpireDate().before(NnDateUtil.now())) {
                    purchase.setStatus(NnPurchase.INACTIVE);
                } else {
                    purchase.setStatus(NnPurchase.ACTIVE);
                }
                
            } catch (GeneralSecurityException e) {
                
                log.warning("GeneralSecurityException");
                log.warning(e.getMessage());
                
            } catch (IOException e) {
                
                log.warning("IOException");
                log.warning(e.getMessage());
                
            } finally {
                
                save(purchase);
            }
            
        } else if (item.getBillingPlatform() == NnItem.APPSTORE) {
            
            JSONObject receipt = AppStoreLib.getReceipt(purchase, isProduction);
            if (receipt == null) {
                
                log.warning("fail to get receipt");
                return;
            }
            
            try {
                
                purchase.setExpireDate(new Date(receipt.getLong("expires_date")));
                String productIdRef = receipt.getString("product_id");
                // check productId
                if (item.getProductIdRef().equals(productIdRef) == false) {
                    
                    log.warning("productIdRef not match");
                    purchase.setVerified(false);
                    purchase.setStatus(NnPurchase.INVALID);
                    save(purchase);
                    
                    return;
                }
                // check bundleId
                Mso mso = NNF.getMsoMngr().findById(item.getMsoId());
                String bundleId = MsoConfigManager.getAppStoreBundleId(mso);
                if (bundleId.equals(receipt.getString("app_item_id")) == false) {
                    
                    log.warning("bundleId not match");
                    purchase.setVerified(false);
                    purchase.setStatus(NnPurchase.INACTIVE);
                    save(purchase);
                    
                    return;
                }
                
                purchase.setVerified(true);
                if (purchase.getExpireDate().before(NnDateUtil.now())) {
                    purchase.setStatus(NnPurchase.INACTIVE);
                } else {
                    purchase.setStatus(NnPurchase.ACTIVE);
                }
                
            } catch (JSONException e) {
                
            } finally {
                
                save(purchase);
            }
            
        } else {
            
            log.warning("not supported platform");
            // unknown platform - do nothing
        }
        
    }
    
    public NnPurchase save(NnPurchase purchase) {
        
        Date now = NnDateUtil.now();
        
        if (purchase.getCreateDate() == null) {
            purchase.setCreateDate(now);
        }
        purchase.setUpdateDate(now);
        
        return dao.save(purchase);
    }
    
    public NnPurchase findByUserAndItem(NnUser user, NnItem item) {
        
        return dao.findOne(user.getIdStr(), item.getId());
    }
    
    // TODO: rewrite
    public boolean isPurchased(NnUser user, NnChannel channel) {
        
        if (user == null || channel == null) { return false; }
        
        List<NnPurchase> purchases = findByUser(user);
        
        for (NnPurchase purchase : purchases) {
            
            NnItem item = NNF.getItemMngr().findById(purchase.getItemId());
            
            if (item.getChannelId() == channel.getId()) {
                
                return true;
            }
        }
        
        return false;
    }
}
