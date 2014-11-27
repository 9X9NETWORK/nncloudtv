package com.nncloudtv.lib;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Hex;

import com.nncloudtv.model.MsoNotification;
import com.nncloudtv.model.NnDevice;
import com.nncloudtv.model.NnDeviceNotification;
import com.nncloudtv.service.NnDeviceManager;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsDelegate;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.DeliveryError;
import com.notnoop.apns.EnhancedApnsNotification;
import com.notnoop.apns.PayloadBuilder;

public class APNSLib {
    
    protected static final Logger log = Logger.getLogger(APNSLib.class.getName());
    
    public static void doPost(MsoNotification msoNotification, String fileRoot, String password, boolean isProduction) {
        
        if (msoNotification == null || fileRoot == null || password == null) {
            return ;
        }
        
        log.info("apns sender, send to mso id = " + msoNotification.getMsoId());
         
        class NnDelegate implements ApnsDelegate {
            
            private Map<Integer, NnDevice> messageIdMap;
            
            public void setMessageIdMap(Map<Integer, NnDevice> messageIdMap) {
                this.messageIdMap = messageIdMap;
            }
            
            private void removeToken(int messageId) {
                
                if (messageIdMap == null) {
                    return ;
                }
                NnDevice device = messageIdMap.get(messageId);
                NNF.getDeviceMngr().delete(device);
            }
            
            public void messageSent(ApnsNotification notification, boolean isRetry) {
                
                if (isRetry) {
                    log.info("Retry send ID = " + notification.getIdentifier() +
                            " , token = " + Hex.encodeHexString(notification.getDeviceToken()));
                } else {
                    log.info("Send ID = " + notification.getIdentifier() +
                             ", token = " + Hex.encodeHexString(notification.getDeviceToken()));
                }
            }
            
            public void messageSendFailed(ApnsNotification notification, Throwable t) {
                log.info("Send failed ID = " + notification.getIdentifier() +
                            " , token = " + Hex.encodeHexString(notification.getDeviceToken()));
                log.info(t.toString());
            }
            
            public void connectionClosed(DeliveryError e, int messageIdentifier) {
                
                log.info("Connection closed due to ID=" + messageIdentifier);
                log.info("Delivery error code : " + e.code());
                if (e.code() == DeliveryError.INVALID_PAYLOAD_SIZE.code()) {
                    log.info("INVALID_PAYLOAD_SIZE");
                }
                if (e.code() == DeliveryError.INVALID_TOKEN.code()) {
                    log.info("INVALID_TOKEN");
                    removeToken(messageIdentifier);
                }
                if (e.code() == DeliveryError.INVALID_TOKEN_SIZE.code()) {
                    log.info("INVALID_TOKEN_SIZE");
                    removeToken(messageIdentifier);
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
                log.info("Cache length exceeded, new cache length = " + newCacheLength);
            }
            
            public void notificationsResent(int resendCount) {
                log.info("Notifications resent, resend count = " + resendCount);
            }
        };
        NnDelegate delegate = new NnDelegate();
        
        ApnsService service = null;
        try {
            if (isProduction) {
                service = APNS.newService()
                        .withCert(fileRoot, password)
                        .asPool(15)
                        .withProductionDestination() // Specify to use the Apple Production servers
                        .withDelegate(delegate) // Set the delegate to get notified of the status of message delivery
                        .build();
            } else {
                log.info("developement provisioning profile is used (developement site)");
                service = APNS.newService()
                        .withCert(fileRoot, password)
                        .asPool(15)
                        .withSandboxDestination() // Specify to use the Apple sandbox servers
                        //.asNonBlocking() // Constructs non-blocking queues and sockets connections
                        .withDelegate(delegate) // Set the delegate to get notified of the status of message delivery
                        .build();
            }
        } catch (Exception e) {
            log.warning(e.getMessage());
            return ;
        }
        
        try {
            service.testConnection();
        } catch (Exception e) {
            log.warning("APNS service not reachable");
            log.warning(e.getMessage());
            return ;
        }
        
        // remove inactive devices in database before push, reduce invalid token event occur when pushing.
        removeInactiveDevices(service, msoNotification.getMsoId());
        
        // prepare notifications
        List<NnDevice> fetchedDevices = NNF.getDeviceMngr().findByMsoAndType(msoNotification.getMsoId(), NnDevice.TYPE_APNS);
        if (fetchedDevices == null) {
            log.info("fetchedDevices=null");
            return ;
        }
        
        // used to handle NnDevice if send failed event occur
        Map<Integer, NnDevice> messageIdMap = new TreeMap<Integer, NnDevice>();
        
        List<EnhancedApnsNotification> notifications = new ArrayList<EnhancedApnsNotification>();
        List<NnDeviceNotification> deviceNotifications = new ArrayList<NnDeviceNotification>();
        int count = 1;
        log.info("sending APNs notification with content = " + msoNotification.getContent());
        for (NnDevice device : fetchedDevices) {
            
            NnDeviceNotification deviceNotification = GCMLib.buildDeviceNotification(msoNotification);
            deviceNotification.setDeviceId(device.getId());
            deviceNotifications.add(deviceNotification);
            try {
                PayloadBuilder payloadBuilder = APNS.newPayload()
                        .alertBody(msoNotification.getMessage())
                        .badge(device.getBadge() + 1)
                        .sound("default")
                        .customField("content", msoNotification.getContent())
                        .customField("ts", NnDateUtil.now());
                // check size 256 bytes
                if (payloadBuilder.isTooLong()) {
                    log.info("Payload is too long, shrinking it");
                    payloadBuilder = payloadBuilder.shrinkBody();
                }
                
                EnhancedApnsNotification notification = new EnhancedApnsNotification(
                        count,                                 /* Next ID */
                        (int) (NnDateUtil.timestamp() / 1000 + 60 * 60 * 24 * 7),  /* Expire in one week */
                        device.getToken(),                     /* Device Token */
                        payloadBuilder.build());
                
                notifications.add(notification);
                messageIdMap.put(count, device);
                
                count = count + 1;
                
                // update badges
                device.setBadge(device.getBadge() + 1);
            } catch (Exception e) {
                log.warning(e.getMessage());
            }
        }
        delegate.setMessageIdMap(messageIdMap);
        
        // update all fetchedDevices with new badge
        NNF.getDeviceMngr().save(fetchedDevices);
        NNF.getDeviceNotiMngr().save(deviceNotifications);
        
        // TODO performance issue
        for (EnhancedApnsNotification notification : notifications) {
            service.push(notification);
        }
    }
    
    private static void removeInactiveDevices(ApnsService service, Long msoId) {
        
        if (service == null || msoId == null) {
            return ;
        }
        
        Map<String,Date> inactiveDevices = null;
        try {
            inactiveDevices = service.getInactiveDevices();
        } catch (Exception e) {
            log.info(e.getMessage());
            return ;
        }
        
        NnDeviceManager deviceMngr = NNF.getDeviceMngr();
        List<NnDevice> deleteDevices = new ArrayList<NnDevice>();
        if (inactiveDevices != null) {
            for(String inactiveDevice : inactiveDevices.keySet()) {
                log.info("inactiveDevice = " + inactiveDevice.toLowerCase());
                List<NnDevice> devices = deviceMngr.findByToken(inactiveDevice.toLowerCase());
                if (devices != null) {
                    for (NnDevice device : devices) {
                        if (device.getMsoId() == msoId
                                && NnDevice.TYPE_APNS.equals(device.getType())
                                // if install app after uninstall app,
                                // the old inactiveDevice record should not kick the new activeDevice out
                                // which they have same device token
                                && inactiveDevices.get(inactiveDevice).after(device.getUpdateDate())) {
                            deleteDevices.add(device);
                        }
                    }
                }
            }
        }
        deviceMngr.delete(deleteDevices);
    }
}
