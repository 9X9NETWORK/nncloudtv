package com.nncloudtv.service;

import java.io.File;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.lib.APNSLib;
import com.nncloudtv.lib.GCMLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.MsoNotification;

@Service
public class NotificationService {
    
    protected static final Logger log = Logger.getLogger(NotificationService.class.getName());
    
    public void sendToGCM(Long msoNotificationId) {
        
        if (msoNotificationId == null) {
            return ;
        }
        
        MsoNotification msoNotification = NNF.getMsoNotiMngr().findById(msoNotificationId);
        if (msoNotification == null) {
            log.info("notificationId not found! " + msoNotificationId);
            return ;
        }
        
        Mso mso = NNF.getMsoMngr().findById(msoNotification.getMsoId());
        if (mso == null) {
            return ;
        }
        
        MsoConfig config = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.GCM_API_KEY);
        if (config == null || config.getValue() == null || config.getValue().isEmpty()) {
            log.warning("GCM api key not set for mso " + mso.getName());
            return;
        }
        
        GCMLib.doPost(msoNotification, config.getValue());
    }
    
    public void sendToAPNS(Long msoNotificationId, boolean isProduction) {
        
        if (msoNotificationId == null) {
            return ;
        }
        
        MsoNotification msoNotification = NNF.getMsoNotiMngr().findById(msoNotificationId);
        if (msoNotification == null) {
            log.info("notificationId not found! " + msoNotificationId);
            return ;
        }
        
        Mso mso = NNF.getMsoMngr().findById(msoNotification.getMsoId());
        if (mso == null) {
            return ;
        }
        
        String fileRoot = MsoConfigManager.getP12FilePath(mso, isProduction);
        File p12 = new File(fileRoot);
        if (p12.exists() == false) {
            log.info("APNS p12 file not approrite set for mso : " + mso.getName());
            return ;
        }
        
        /**
         * There's also convention of password to follow:
         *  
         * contact mso name with "9x9"
         * padding underscore to reach 8 characters
         * translate the second character to uppercase
         *  
         * ex. cTs9x9__, cRashcourse9x9, dDtv9x9_, ...
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
        
        APNSLib.doPost(msoNotification, fileRoot, password, isProduction);
    }
}
