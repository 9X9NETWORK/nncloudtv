package com.nncloudtv.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.api.client.util.IOUtils;
import com.nncloudtv.exception.AppStoreFailedVerifiedException;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnItem;
import com.nncloudtv.model.NnPurchase;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.web.api.ApiGeneric;

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
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", ApiGeneric.APPLICATION_JSON_UTF8);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), NnStringUtil.UTF8);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(writer, requestObj);
            writer.flush();
            
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                
                log.warning(String.format("appstore returns not ok, %d %s", conn.getResponseCode(), conn.getResponseMessage()));
                return null;
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(conn.getInputStream(), baos);
            JSONObject json = new JSONObject(new String(baos.toByteArray(), NnStringUtil.UTF8));
            int status = json.getInt("status");
            if (status != 0) {
                
                log.info("appstore resturn status = " + status);
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
            
        } catch (UnsupportedEncodingException e) {
            
            log.warning(e.getMessage());
            return null;
            
        } catch (MalformedURLException e) {
            
            log.warning(e.getMessage());
            return null;
            
        } catch (IOException e) {
            
            log.warning(e.getMessage());
            return null;
        }
    }
}
