package com.nncloudtv.lib;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.logging.Logger;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisher.Builder;
import com.google.api.services.androidpublisher.AndroidPublisher.Purchases;
import com.google.api.services.androidpublisher.AndroidPublisher.Purchases.Subscriptions;
import com.google.api.services.androidpublisher.AndroidPublisher.Purchases.Subscriptions.Get;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnPurchase;
import com.nncloudtv.service.MsoConfigManager;

public class GooglePlayLib {
    
    protected static final Logger log = Logger.getLogger(GooglePlayLib.class.getName());
    
    static final String SERVICE_SCOPE = "https://www.googleapis.com/auth/androidpublisher";
    
    private static GoogleCredential getGoogleCredential() throws GeneralSecurityException, IOException {
        
        GoogleCredential.Builder builder = new GoogleCredential.Builder();
        
        String path = MsoConfigManager.getGooglePlayPemFilePath();
        log.info("read pem file from " + path);
        File p12 = new File(path);
        if (!p12.canRead()) {
            log.severe("can not read p12 file from " + path);
            return null;
        }
        String accountEmail = MsoConfigManager.getGooglePlayAccountEmail();
        log.info("google account email = " + accountEmail);
        
        builder = builder.setServiceAccountPrivateKeyFromP12File(p12);
        builder = builder.setTransport(GoogleNetHttpTransport.newTrustedTransport());
        builder = builder.setJsonFactory(JacksonFactory.getDefaultInstance());
        builder = builder.setServiceAccountId(accountEmail);
        builder = builder.setServiceAccountScopes(Collections.singleton(SERVICE_SCOPE));
        
        return builder.build();
    }
    
    private static AndroidPublisher getAndroidPublisher(Mso mso) throws GeneralSecurityException, IOException {
        
        return new Builder(GoogleNetHttpTransport.newTrustedTransport(),
                           JacksonFactory.getDefaultInstance(),
                           getGoogleCredential())
                       .setApplicationName(MsoConfigManager.getGooglePlayAppName(mso))
                       .build();
    }
    
    public static SubscriptionPurchase getSubscriptionPurchase(NnPurchase purchase) throws GeneralSecurityException, IOException {
        
        if (purchase == null) { return null; }
        
        Mso mso = NNF.getMsoMngr().findByPurchase(purchase);
        String subscriptionIdRef = purchase.getSubscriptionIdRef();
        String purchaseToken = purchase.getPurchaseToken();
        String packageName = MsoConfigManager.getGooglePlayPackageName(mso);
        
        log.info("packageName = " + packageName);
        log.info("subscriptionId = " + subscriptionIdRef);
        log.info("purchaseToken = " + purchaseToken);
        
        AndroidPublisher publisher = getAndroidPublisher(mso);
        Purchases purchases = publisher.purchases();
        Subscriptions subscriptions = purchases.subscriptions();
        Get request = subscriptions.get(packageName, subscriptionIdRef, purchaseToken);
        
        return request.execute();
    }
}
