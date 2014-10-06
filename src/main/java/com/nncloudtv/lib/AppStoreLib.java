package com.nncloudtv.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.api.client.util.IOUtils;
import com.nncloudtv.model.NnPurchase;
import com.nncloudtv.task.PipingTask;
import com.nncloudtv.web.api.ApiGeneric;

public class AppStoreLib {
    
    protected static final Logger log = Logger.getLogger(AppStoreLib.class.getName());
    
    public static void verifyReceipt(NnPurchase purchase, boolean isProduction) {
        
        String requestUrl = "https://" + (isProduction ? "buy" : "sandbox") + ".itunes.apple.com/verifyReceipt";
        
        try {
            
            Map<String, String> requestObj = new HashMap<String, String>();
            requestObj.put("receipt-data", new String( Base64.encodeBase64(purchase.getPurchaseToken().getBytes(NnStringUtil.UTF8)), NnStringUtil.UTF8));
            
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", ApiGeneric.APPLICATION_JSON_UTF8);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(writer, requestObj);
            writer.flush();
            
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                
                log.warning("appstore returns not ok, " + conn.getResponseCode() + " " + conn.getResponseMessage());
                
                // log more when not ok (pipe inputstream to stdout)
                (new PipingTask(conn.getInputStream(), System.out)).start();
                
                return;
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(conn.getInputStream(), baos);
            JSONObject json = new JSONObject(new String(baos.toByteArray(), NnStringUtil.UTF8));
            int status = json.getInt("status");
            JSONObject receipt = json.getJSONObject("receipt");
            if (status != 0) {
                
                log.info("appstore resturns status = " + status);
                if (receipt != null) {
                    
                    System.out.println(receipt.toString());
                }
            }
            purchase.setExpireDate(new Date(receipt.getLong("expires_date")));
            purchase.setVerified(true);
            if (purchase.getExpireDate().before(NnDateUtil.now())) {
                purchase.setStatus(NnPurchase.INACTIVE);
            } else {
                purchase.setStatus(NnPurchase.ACTIVE);
            }
            NNF.getPurchaseMngr().save(purchase);
            
        } catch (JSONException e) {
            
            log.warning(e.getMessage());
            return;
            
        } catch (UnsupportedEncodingException e) {
            
            log.warning(e.getMessage());
            return;
            
        } catch (MalformedURLException e) {
            
            log.warning(e.getMessage());
            return;
            
        } catch (IOException e) {
            
            log.warning(e.getMessage());
            return;
        }
        
    }
}
