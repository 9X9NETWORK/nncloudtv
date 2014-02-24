package com.nncloudtv.lib;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.nncloudtv.dao.NnDeviceDao;
import com.nncloudtv.model.MsoNotification;
import com.nncloudtv.model.NnDevice;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.EnhancedApnsNotification;

public class APNSLib {
    
    protected static final Logger log = Logger.getLogger(APNSLib.class.getName());
    // hard coded for test purpose
    private static final String APNS_KEYFILE_ROOT = "/usr/share/jetty/webapps/bartonAPNS5.p12";
    private static final String APNS_KEYFILE_PASSWORD = "111111";
    
    private NnDeviceDao deviceDao = new NnDeviceDao();
    
    public void doPost(MsoNotification msoNotification) {
        
        log.info("get in apns func ---------------------------------------------------");
        log.info("send to mso id=" + msoNotification.getMsoId());
        
        // TODO find APNS key from given msoNotification.getMsoId()
        String fileRoot = APNS_KEYFILE_ROOT;
        String password = APNS_KEYFILE_PASSWORD;
        
        ApnsService service = APNS.newService()
                .withCert(fileRoot, password)
                .withSandboxDestination() // Specify to use the Apple sandbox servers
                //.withProductionDestination() // Specify to use the Apple Production servers
                //.asNonBlocking() // Constructs non-blocking queues and sockets connections
                //.withDelegate(ApnsDelegate delegate) // TODO Set the delegate to get notified of the status of message delivery
                .build();
        
        // prepare notifications
        List<NnDevice> fetchedDevices = deviceDao.findByMsoAndType(msoNotification.getMsoId(), NnDevice.TYPE_APNS);
        List<EnhancedApnsNotification> notifications = new ArrayList<EnhancedApnsNotification>();
        if (fetchedDevices != null) {
            int count = 1;
            for (NnDevice device : fetchedDevices) {
                EnhancedApnsNotification notification = new EnhancedApnsNotification(count /* Next ID */,
                        (int) new Date().getTime() + 60 * 60 /* Expire in one hour */,
                        device.getToken() /* Device Token */,
                        APNS.newPayload()
                            .alertBody(msoNotification.getMessage())
                            .badge(device.getBadge() + 1)
                            .customField("content", msoNotification.getContent())
                            .build());
                notifications.add(notification);
                count = count + 1;
                
                // update badges
                device.setBadge(device.getBadge() + 1);
            }
            // TODO update all fetchedDevices with new badge
            
            // test notification add
            EnhancedApnsNotification notification = new EnhancedApnsNotification(count /* Next ID */,
                    (int) new Date().getTime() + 60 * 60 /* Expire in one hour */,
                    "d8e8a8c5c4337a7b8b564a1d215d7f3691791597d9e91316413788f93a7dfa67" /* Device Token */,
                    APNS.newPayload()
                        .alertBody(msoNotification.getMessage())
                        .badge(1)
                        .customField("content", msoNotification.getContent())
                        .build());
            notifications.add(notification);
        } else {
            return ;
        }
        
        // TODO performance issue
        for (EnhancedApnsNotification notification : notifications) {
            service.push(notification);
        }
        
        // String payload2 = APNS.newPayload().alertBody("hello 2").badge(1).customField("secret", "what do you think?").build();
        
        //service.push(devices, payload);
        //service.push(token, payload);
        
        //EnhancedApnsNotification notification2 = new EnhancedApnsNotification(1 /* Next ID */,
        //        (int) new Date().getTime() + 60 * 60 /* Expire in one hour */,
        //        token /* Device Token */,
        //        payload2);
        //service.push(notification);
    }

}
