package com.nncloudtv.lib;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.nncloudtv.dao.NnDeviceDao;
import com.nncloudtv.model.MsoNotification;
import com.nncloudtv.model.NnDevice;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsDelegate;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.DeliveryError;
import com.notnoop.apns.EnhancedApnsNotification;

public class APNSLib {
    
    protected static final Logger log = Logger.getLogger(APNSLib.class.getName());
    // hard coded for test purpose
    private static final String APNS_KEYFILE_ROOT = "/usr/share/jetty/webapps/bartonAPNS5.p12";
    private static final String APNS_KEYFILE_PASSWORD = "111111";
    
    // current for log purpose
    private static final ApnsDelegate delegate = new ApnsDelegate() {
        
        public void messageSent(ApnsNotification notification, boolean isRetry) {
            
            if (isRetry) {
                log.info("Retry send ID=" + notification.getIdentifier() + " , token=" + notification.getDeviceToken());
            } else {
                log.info("Send ID=" + notification.getIdentifier() + " , token=" + notification.getDeviceToken());
            }
        }

        public void messageSendFailed(ApnsNotification notification, Throwable t) {
            log.info("Send failed ID=" + notification.getIdentifier() + " , token=" + notification.getDeviceToken());
            NnLogUtil.logThrowable(t);
        }

        public void connectionClosed(DeliveryError e, int messageIdentifier) {
            log.info("Connection closed due to ID=" + messageIdentifier);
            log.info("Delivery error code : " + e.code());
            if (e.code() == DeliveryError.INVALID_PAYLOAD_SIZE.code()) {
                log.info("INVALID_PAYLOAD_SIZE");
            }
            if (e.code() == DeliveryError.INVALID_TOKEN.code()) {
                log.info("INVALID_TOKEN"); // TODO remove token in database
            }
            if (e.code() == DeliveryError.INVALID_TOKEN_SIZE.code()) {
                log.info("INVALID_TOKEN_SIZE");
            }
            if (e.code() == DeliveryError.INVALID_TOPIC_SIZE.code()) {
                log.info("INVALID_TOPIC_SIZE");
            }
            if (e.code() == DeliveryError.MISSING_DEVICE_TOKEN.code()) {
                log.info("MISSING_DEVICE_TOKEN");
            }
            if (e.code() == DeliveryError.MISSING_PAYLOAD.code()) {
                log.info("MISSING_PAYLOAD");
            }
            if (e.code() == DeliveryError.MISSING_TOPIC.code()) {
                log.info("MISSING_TOPIC");
            }
            if (e.code() == DeliveryError.NO_ERROR.code()) {
                log.info("NO_ERROR");
            }
            if (e.code() == DeliveryError.NONE.code()) {
                log.info("NONE");
            }
            if (e.code() == DeliveryError.PROCESSING_ERROR.code()) {
                log.info("PROCESSING_ERROR");
            }
            if (e.code() == DeliveryError.UNKNOWN.code()) {
                log.info("UNKNOWN");
            }
        }

        public void cacheLengthExceeded(int newCacheLength) {
            log.info("Cache length exceeded, new cache length : " + newCacheLength);
        }

        public void notificationsResent(int resendCount) {
            log.info("Notifications resent, resend count : " + resendCount);
        }
    };
    
    private NnDeviceDao deviceDao = new NnDeviceDao();
    
    public void doPost(MsoNotification msoNotification) {
        
        log.info("get in apns func ---------------------------------------------------");
        log.info("send to mso id=" + msoNotification.getMsoId());
        
        // TODO find APNS key from given msoNotification.getMsoId()
        String fileRoot = APNS_KEYFILE_ROOT;
        String password = APNS_KEYFILE_PASSWORD;
        
        ApnsService service = APNS.newService()
                .withCert(fileRoot, password)
                .asPool(15)
                .withSandboxDestination() // Specify to use the Apple sandbox servers
                //.withProductionDestination() // Specify to use the Apple Production servers
                //.asNonBlocking() // Constructs non-blocking queues and sockets connections
                .withDelegate(delegate) // Set the delegate to get notified of the status of message delivery
                .build();
        
        //service.testConnection(); TODO if not available ?
        //service.getInactiveDevices(); TODO update inactive tokens, do it every call ?
        
        // prepare notifications
        List<NnDevice> fetchedDevices = deviceDao.findByMsoAndType(msoNotification.getMsoId(), NnDevice.TYPE_APNS);
        if (fetchedDevices == null) {
            return ;
        }
        
        List<EnhancedApnsNotification> notifications = new ArrayList<EnhancedApnsNotification>();
        int count = 1;
        for (NnDevice device : fetchedDevices) {
            EnhancedApnsNotification notification = new EnhancedApnsNotification(
                    count /* Next ID */,
                    (int) new Date().getTime() + 60 * 60 /* Expire in one hour */,
                    device.getToken() /* Device Token */,
                    APNS.newPayload()
                        .alertBody(msoNotification.getMessage())
                        .badge(device.getBadge() + 1)
                        .customField("content", msoNotification.getContent())
                        .build());
            // TODO check size 256 bytes
            notifications.add(notification);
            count = count + 1;
            
            // update badges
            device.setBadge(device.getBadge() + 1);
        }
        // TODO update all fetchedDevices with new badge
        
        // test notification add
        EnhancedApnsNotification testNotification = new EnhancedApnsNotification(
                count /* Next ID */,
                (int) new Date().getTime() + 60 * 60 /* Expire in one hour */,
                "d8e8a8c5c4337a7b8b564a1d215d7f3691791597d9e91316413788f93a7dfa67" /* Device Token */,
                APNS.newPayload()
                    .alertBody(msoNotification.getMessage())
                    .badge(1)
                    .customField("content", msoNotification.getContent())
                    .build());
        notifications.add(testNotification);
        
        // TODO performance issue
        for (EnhancedApnsNotification notification : notifications) {
            service.push(notification);
        }
    }

}
