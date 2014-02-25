package com.nncloudtv.service;

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
        this.msoNotificationMngr = msoNotificationMngr;
        this.msoMngr = msoMngr;
    }
    
    public void sendToGCM(Long msoNotificationId) {
        
        if (msoNotificationId == null) {
            return ;
        }
        
        MsoNotification msoNotification = msoNotificationMngr.findById(msoNotificationId);
        if (msoNotification == null) {
            return ;
        }
        
        Mso mso = msoMngr.findById(msoNotification.getMsoId());
        if (mso == null) {
            return ;
        }
        
        MsoConfig config = msoConfigMngr.findByMsoAndItem(mso, MsoConfig.GCM_API_KEY);
        if (config == null) {
            return ;
        }
        
        gcmLib.doPost(msoNotification, config.getValue());
    }
    
    public void sendToAPNS(Long msoNotificationId) {
        
        if (msoNotificationId == null) {
            return ;
        }
        
        MsoNotification msoNotification = msoNotificationMngr.findById(msoNotificationId);
        if (msoNotification == null) {
            return ;
        }
        
        apnsLib.doPost(msoNotification, false);
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
        
        apnsLib.doPost(msoNotification, true);
    }

}
