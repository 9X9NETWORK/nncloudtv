package com.nncloudtv.service;

import java.io.File;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.APNSLib;
import com.nncloudtv.lib.GCMLib;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.MsoNotification;

@Service
public class NotifyService {
    
    protected static final Logger log = Logger.getLogger(NotifyService.class.getName());
    
    private MsoNotificationManager msoNotificationMngr;
    private GCMLib gcmLib;
    private APNSLib apnsLib;
    private MsoConfigManager msoConfigMngr;
    private MsoManager msoMngr;
    
    @Autowired
    public NotifyService(MsoNotificationManager msoNotificationMngr, GCMLib gcmLib, APNSLib apnsLib,
                MsoConfigManager msoConfigMngr, MsoManager msoMngr) {
        this.msoNotificationMngr = msoNotificationMngr;
        this.gcmLib = gcmLib;
        this.apnsLib = apnsLib;
        this.msoConfigMngr = msoConfigMngr;
        this.msoMngr = msoMngr;
    }
    
    public void sendToGCM(Long msoNotificationId) {
        
        if (msoNotificationId == null) {
            return ;
        }
        
        MsoNotification msoNotification = msoNotificationMngr.findById(msoNotificationId);
        if (msoNotification == null) {
            log.info("notificationId not found! " + msoNotificationId);
            return ;
        }
        
        Mso mso = msoMngr.findById(msoNotification.getMsoId());
        if (mso == null) {
            return ;
        }
        
        MsoConfig config = msoConfigMngr.findByMsoAndItem(mso, MsoConfig.GCM_API_KEY);
        if (config == null || config.getValue() == null || config.getValue().isEmpty()) {
            log.info("GCM api key not approrite set for mso : " + mso.getName());
            return ;
        }
        
        gcmLib.doPost(msoNotification, config.getValue());
    }
    
    public void sendToAPNS(Long msoNotificationId, boolean isProduction) {
        
        if (msoNotificationId == null) {
            return ;
        }
        
        MsoNotification msoNotification = msoNotificationMngr.findById(msoNotificationId);
        if (msoNotification == null) {
            log.info("notificationId not found! " + msoNotificationId);
            return ;
        }
        
        Mso mso = msoMngr.findById(msoNotification.getMsoId());
        if (mso == null) {
            return ;
        }
        
        String fileRoot = MsoConfigManager.getP12FilePath(mso);
        File p12 = new File(fileRoot);
        if (p12.exists() == false) {
            log.info("APNS p12 file not approrite set for mso : " + mso.getName());
            return ;
        }
        
        /*
         * There's also convention of password to follow:

            contact mso name with "9x9"
            padding underscore to reach 8 characters
            translate the second character to uppercase

            ex. cTs9x9__, cRashcourse9x9, dDtv9x9_, ...
         */
        String password = "";
        String msoName = mso.getName();
        for (int count = 0; count < msoName.length(); count++) {
            if (count == 1) {
                password = password + (msoName.charAt(count) + "").toUpperCase();
            } else {
                password = password + msoName.charAt(count);
            }
        }
        password = password + "9x9";
        for (int count = password.length(); count < 8; count++) {
            password = password + "_";
        }
        
        log.info("fileRoot=" + fileRoot);
        log.info("password=" + password);
        
        apnsLib.doPost(msoNotification, fileRoot, password, isProduction);
    }
    
    public void sendToGCM_debug(Long msoNotificationId) {
        
        if (msoNotificationId == null) {
            return ;
        }
        
        MsoNotification msoNotification = msoNotificationMngr.findById(msoNotificationId);
        if (msoNotification == null) {
            return ;
        }
        
        // hard coded for test purpose
        String GCM_SENDER_KEY = "AIzaSyAXlEvWnLCNF0yL1GnZb-U0YRxG2WRvAc4";
        
        gcmLib.doPost(msoNotification, GCM_SENDER_KEY);
    }
    
    public void sendToAPNS_debug(Long msoNotificationId) {
        
        if (msoNotificationId == null) {
            return ;
        }
        
        MsoNotification msoNotification = msoNotificationMngr.findById(msoNotificationId);
        if (msoNotification == null) {
            return ;
        }
        
        // hard coded for test purpose
        String APNS_KEYFILE_ROOT = "/usr/share/jetty/webapps/bartonAPNS5.p12";
        String APNS_KEYFILE_PASSWORD = "111111";
        
        apnsLib.doPost(msoNotification, APNS_KEYFILE_ROOT, APNS_KEYFILE_PASSWORD, false);
    }

}