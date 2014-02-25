package com.nncloudtv.service;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.APNSLib;
import com.nncloudtv.lib.GCMLib;
import com.nncloudtv.model.MsoNotification;

@Service
public class NotifyService {
    
    protected static final Logger log = Logger.getLogger(NotifyService.class.getName());
    
    private MsoNotificationManager msoNotificationMngr;
    private GCMLib gcmLib;
    private APNSLib apnsLib;
    
    @Autowired
    public NotifyService(MsoNotificationManager msoNotificationMngr, GCMLib gcmLib, APNSLib apnsLib) {
        this.msoNotificationMngr = msoNotificationMngr;
        this.gcmLib = gcmLib;
        this.apnsLib = apnsLib;
    }
    
    public void sendToGCM(Long msoNotificationId) {
        
        if (msoNotificationId == null) {
            return ;
        }
        
        MsoNotification msoNotification = msoNotificationMngr.findById(msoNotificationId);
        if (msoNotification == null) {
            return ;
        }
        
        gcmLib.doPost(msoNotification, false);
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
        
        gcmLib.doPost(msoNotification, true);
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
