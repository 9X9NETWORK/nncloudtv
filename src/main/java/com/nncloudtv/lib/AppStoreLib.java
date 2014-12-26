package com.nncloudtv.lib;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.nncloudtv.exception.AppStoreFailedVerifiedException;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnItem;
import com.nncloudtv.model.NnPurchase;
import com.nncloudtv.service.MsoConfigManager;

public class AppStoreLib {
    
    protected static final Logger log = Logger.getLogger(AppStoreLib.class.getName());
    
    public static String verifyReceipt(String receiptData, String sharedSecret, boolean sandbox) {
        
        String requestUrl = "https://buy.itunes.apple.com/verifyReceipt";
        String sandboxUrl = "https://sandbox.itunes.apple.com/verifyReceipt";
        
        Map<String, String> requestObj = new HashMap<String, String>();
        requestObj.put("receipt-data", receiptData);
        requestObj.put("password", sharedSecret);
        log.info("shared secret = " + sharedSecret);
        if (sandbox)
            requestUrl = sandboxUrl;
        log.info("receipt validation url = " + requestUrl);
        
        return NnNetUtil.urlPostWithJson(requestUrl, requestObj);
    }
    
    public static JSONObject getReceipt(NnPurchase purchase) throws AppStoreFailedVerifiedException {
        
        NnItem item = NNF.getItemMngr().findById(purchase.getItemId());
        Mso mso = NNF.getMsoMngr().findById(item.getMsoId());
        
        try {
            
            // doc: https://developer.apple.com/library/ios/releasenotes/General/ValidateAppStoreReceipt/Chapters/ValidateRemotely.html
            String sharedSecret = MsoConfigManager.getAppStoreSharedSecret(mso);
            String jsonStr = verifyReceipt(purchase.getPurchaseToken(), sharedSecret, false);
            if (jsonStr == null) {
                log.warning("appstore return empty");
                return null;
            }
            JSONObject json = new JSONObject(jsonStr);
            int status = json.getInt("status");
            purchase.setStatus((short) status);
            log.info("appstore resturn status = " + status);
            if (status == 21007) {
                log.info("try sandbox");
                jsonStr = verifyReceipt(purchase.getPurchaseToken(), sharedSecret, true);
                if (jsonStr == null) {
                    log.warning("sanbox return empty");
                    return null;
                }
                json = new JSONObject(jsonStr);
                status = json.getInt("status");
                log.info("sandbox resturn status = " + status);
            }
            if (status != 0) {
                purchase.setStatus((short) status);
                throw new AppStoreFailedVerifiedException();
            }
            JSONObject receipt = null;
            if (!json.isNull("latest_receipt_info")) {
                receipt = json.getJSONObject("latest_receipt_info");
            } else {
                receipt = json.getJSONObject("receipt");
            }
            if (receipt == null) {
                
                log.warning("receipt is null");
                return null;
            }
            
            // receipt format: http://stackoverflow.com/questions/15255564/ios-in-app-purchases-receipt-string-explained
            System.out.println(receipt.toString());
            return receipt;
            
        } catch (JSONException e) {
            
            log.warning(e.getMessage());
            return null;
            
        }
    }
}
