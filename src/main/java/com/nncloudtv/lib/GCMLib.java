package com.nncloudtv.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.nncloudtv.dao.NnDeviceDao;
import com.nncloudtv.model.MsoNotification;
import com.nncloudtv.model.NnDevice;

@Service
public class GCMLib {
    
    protected static final Logger log = Logger.getLogger(GCMLib.class.getName());
    private static final Executor threadPool = Executors.newFixedThreadPool(5);
    private static final int MULTICAST_SIZE = 1000;
    
    private NnDeviceDao deviceDao = new NnDeviceDao();
    
    public void doPost(MsoNotification msoNotification, String apiKey) {
        
        if (msoNotification == null || apiKey == null) {
            return ;
        }
        
        log.info("gcm sender, send to mso id = " + msoNotification.getMsoId());
        
        Sender sender = new Sender(apiKey);
        
        List<NnDevice> fetchedDevices = deviceDao.findByMsoAndType(msoNotification.getMsoId(), NnDevice.TYPE_GCM);
        // used to handle NnDevice if send failed event occur
        Map<String, NnDevice> deviceMap = new HashMap<String, NnDevice>();
        List<String> devices = new ArrayList<String>();
        if (fetchedDevices != null) {
            for (NnDevice device : fetchedDevices) {
                devices.add(device.getToken());
                deviceMap.put(device.getToken(), device);
            }
        }
        
        // send a multicast message using JSON
        // must split in chunks of 1000 devices (GCM limit)
        int total = devices.size();
        List<String> partialDevices = new ArrayList<String>(total);
        int counter = 0;
        int tasks = 0;
        for (String device : devices) {
            counter++;
            partialDevices.add(device);
            int partialSize = partialDevices.size();
            if (partialSize == MULTICAST_SIZE || counter == total) {
                asyncSend(sender, partialDevices, msoNotification, deviceMap);
                partialDevices.clear();
                tasks++;
            }
        }
        log.info("Asynchronously sending " + tasks + " multicast messages to " + total + " devices");
    }
    
    private void asyncSend(Sender gcmSender, List<String> partialDevices, MsoNotification msoNotification,
            Map<String, NnDevice> allDeviceMap) {
        
        // make a copy
        final List<String> devices = new ArrayList<String>(partialDevices);
        final MsoNotification notification = msoNotification;
        final Sender sender = gcmSender;
        final Map<String, NnDevice> deviceMap = allDeviceMap;
        
        threadPool.execute(new Runnable() {
            public void run() {
                
                Date now = new Date();
                Message message = new Message.Builder()
                    .addData("message", notification.getMessage()) // message
                    .addData("content", notification.getContent()) // content
                    .addData("ts", String.valueOf(now.getTime()))  // ts
                    .build();
                MulticastResult multicastResult;
                try {
                    multicastResult = sender.send(message, devices, 5);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error posting messages", e);
                    return;
                }
                
                List<Result> results = multicastResult.getResults();
                List<NnDevice> deleteDevices = new ArrayList<NnDevice>();
                // analyze the results
                for (int i = 0; i < devices.size(); i++) {
                    
                    String regId = devices.get(i);
                    Result result = results.get(i);
                    
                    String messageId = result.getMessageId();
                    if (messageId != null) {
                        log.fine("Succesfully sent message to device: " + regId +
                                "; messageId = " + messageId);
                        
                        String canonicalRegId = result.getCanonicalRegistrationId();
                        if (canonicalRegId != null) {
                            // same device has more than one registration id: update it
                            log.info("canonicalRegId " + canonicalRegId);
                            updateRegistration(regId, canonicalRegId, notification);
                        }
                    } else {
                        String error = result.getErrorCodeName();
                        if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                            // application has been removed from device - unregister it
                            log.info("Unregistered device: " + regId);
                            NnDevice device = deviceMap.get(regId);
                            if (device != null) {
                                deleteDevices.add(device);
                            }
                        } else {
                            log.severe("Error sending message to " + regId + ": " + error);
                        }
                    }
                }
                deviceDao.deleteAll(deleteDevices);
            }
        });
    }
    
    // kill duplicate and keep only one, replace with canonicalRegId
    private void updateRegistration(String regId, String canonicalRegId, MsoNotification notification) {
        
        if (regId == null || canonicalRegId == null || notification == null) {
            return ;
        }
        
        List<NnDevice> fetchedDevices = deviceDao.findByMsoAndType(notification.getMsoId(), NnDevice.TYPE_GCM);
        List<NnDevice> targetDevices = new ArrayList<NnDevice>();
        for (NnDevice device : fetchedDevices) {
            if (regId.equals(device.getToken()) || canonicalRegId.equals(device.getToken())) {
                targetDevices.add(device);
            }
        }
        
        if (targetDevices.size() > 0) {
            NnDevice device = targetDevices.get(0);
            if (canonicalRegId.equals(device.getToken()) == false) {
                device.setToken(canonicalRegId);
                deviceDao.save(device);
            }
            
            if (targetDevices.size() > 1) {
                List<NnDevice> deleteDevices = targetDevices.subList(1, targetDevices.size() - 1);
                deviceDao.deleteAll(deleteDevices);
            }
        }
    }

}
