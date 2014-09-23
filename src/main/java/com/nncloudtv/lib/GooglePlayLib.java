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
import com.nncloudtv.model.NnPurchase;
import com.nncloudtv.service.MsoConfigManager;

public class GooglePlayLib {
    
    protected static final Logger log = Logger.getLogger(GooglePlayLib.class.getName());
    
    static final String SERVICE_SCOPE = "https://www.googleapis.com/auth/androidpublisher";
    
    public static GoogleCredential getGoogleCredential() throws GeneralSecurityException, IOException {
        
        return new GoogleCredential
                       .Builder()
                       .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                       .setJsonFactory(JacksonFactory.getDefaultInstance())
                       .setServiceAccountId(MsoConfigManager.getGooglePlayAccountEmail())
                       .setServiceAccountScopes(Collections.singleton(SERVICE_SCOPE))
                       .setServiceAccountPrivateKeyFromPemFile(new File(MsoConfigManager.getGooglePlayPemFilePath()))
                       .build();
    }
    
    public static AndroidPublisher getAndroidPublisher() throws GeneralSecurityException, IOException {
        
        return new Builder(GoogleNetHttpTransport.newTrustedTransport(),
                           JacksonFactory.getDefaultInstance(),
                           getGoogleCredential())
                       .setApplicationName(MsoConfigManager.getGooglePlayAppName())
                       .build();
    }
    
    public static boolean verifyPurcaseSubscription(NnPurchase purchase) {
        
        boolean verified = false;
        
        try {
            AndroidPublisher publisher = getAndroidPublisher();
            
            Purchases purchases = publisher.purchases();
            Subscriptions subscriptions = purchases.subscriptions();
            Get request = subscriptions.get(MsoConfigManager.getGooglePlayPackageName(),
                                            purchase.getSubscriptionIdRef(),
                                            purchase.getPurchaseToken());
            if (request.execute() != null) {
                verified = true;
            }
        } catch (GeneralSecurityException e) {
            log.warning(e.getMessage());
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
        
        return verified;
    }
    
}
