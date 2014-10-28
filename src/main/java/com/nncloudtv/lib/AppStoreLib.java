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
    
    public static JSONObject getReceipt(NnPurchase purchase, boolean isProduction) throws AppStoreFailedVerifiedException {
        
        String requestUrl = "https://" + (isProduction ? "buy" : "sandbox") + ".itunes.apple.com/verifyReceipt";
        NnItem item = NNF.getItemMngr().findById(purchase.getItemId());
        Mso mso = NNF.getMsoMngr().findById(item.getMsoId());
        
        try {
            
            // doc: https://developer.apple.com/library/ios/releasenotes/General/ValidateAppStoreReceipt/Chapters/ValidateRemotely.html
            Map<String, String> requestObj = new HashMap<String, String>();
            String sharedSecret = MsoConfigManager.getAppStoreSharedSecret(mso);
            requestObj.put("receipt-data", purchase.getPurchaseToken());
            requestObj.put("password", sharedSecret);
            log.info("shared secret = " + sharedSecret);
            log.info("receipt validation url = " + requestUrl);
            
            String jsonStr = NnNetUtil.urlPostWithJson(requestUrl, requestObj);
            if (jsonStr == null) {
                
                log.warning("appstore returns empty");
                return null;
            }
            
            JSONObject json = new JSONObject(jsonStr);
            int status = json.getInt("status");
            log.info("appstore resturn status = " + status);
            if (status != 0) {
                
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
