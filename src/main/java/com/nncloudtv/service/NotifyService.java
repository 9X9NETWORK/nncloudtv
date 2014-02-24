package com.nncloudtv.service;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.GCMLib;
import com.nncloudtv.model.MsoNotification;

@Service
public class NotifyService {
    
    protected static final Logger log = Logger.getLogger(NotifyService.class.getName());
    
    private MsoNotificationManager msoNotificationMngr;
    private GCMLib gcmLib;
    
    @Autowired
    public NotifyService(MsoNotificationManager msoNotificationMngr, GCMLib gcmLib) {
        this.msoNotificationMngr = msoNotificationMngr;
        this.gcmLib = gcmLib;
    }
    
    public void sendMsoNotification(Long msoNotificationId) {
        
        if (msoNotificationId == null) {
            return ;
        }
        
        MsoNotification msoNotification = msoNotificationMngr.findById(msoNotificationId);
        if (msoNotification == null) {
            return ;
        }
        
        gcmLib.doPost(msoNotification);
        // TODO APNS POST
    }

}
