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
import com.nncloudtv.model.MsoNotification;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnDevice;
import com.nncloudtv.model.NnDeviceNotification;
import com.nncloudtv.model.NnEpisode;
import com.nncloudtv.model.YtProgram;

@Service
public class GCMLib {
    
    protected static final Logger log = Logger.getLogger(GCMLib.class.getName());
    
    private static final Executor threadPool = Executors.newFixedThreadPool(5);
    private static final int MULTICAST_SIZE  = 1000;
    
    public static void doPost(MsoNotification msoNotification, String apiKey) {
        
        if (msoNotification == null || apiKey == null) {
            return ;
        }
        
        log.info("gcm sender, send to mso id = " + msoNotification.getMsoId());
        
        Sender sender = new Sender(apiKey);
        
        NnDeviceNotification msg = buildDeviceNotification(msoNotification);
        if (msg == null) {
            return;
        }
        
        List<NnDevice> fetchedDevices = NNF.getDeviceMngr()
                                           .findByMsoAndType(msoNotification.getMsoId(), NnDevice.TYPE_GCM);
        // used to handle NnDevice if send failed event occur
        Map<String, NnDevice> deviceMap = new HashMap<String, NnDevice>();
        List<String> devices = new ArrayList<String>();
        
        List<NnDeviceNotification> notifications = new ArrayList<NnDeviceNotification>();
        
        if (fetchedDevices != null) {
            for (NnDevice device : fetchedDevices) {
                devices.add(device.getToken());
                deviceMap.put(device.getToken(), device);
                
                NnDeviceNotification notification = new NnDeviceNotification(device.getId(), msoNotification.getMessage());
                notification.setContent(msg.getContent());
                notification.setTitle(msg.getTitle());
                notification.setLogo(msg.getLogo());
                notifications.add(notification);
            }
            NNF.getDeviceNotiMngr().save(notifications);
        }
        
        // send a multicast message using JSON
        // must split in chunks of 1000 devices (due to GCM limitation)
        int total = devices.size();
        List<String> partialDevices = new ArrayList<String>();
        int counter = 0;
        int tasks = 0;
        for (String device : devices) {
            counter++;
            partialDevices.add(device);
            if (partialDevices.size() == MULTICAST_SIZE || counter == total) {
                asyncSend(sender, partialDevices, msoNotification, msg, deviceMap);
                partialDevices = new ArrayList<String>();
                tasks++;
            }
        }
        log.info("asynchronously sending " + tasks + " multicast messages to " + total + " devices");
    }
    
    private static void asyncSend(final Sender sender,
            final List<String> devices,
            final MsoNotification msoNotification, final NnDeviceNotification msg,
            final Map<String, NnDevice> deviceMap) {
        
        threadPool.execute(new Runnable() {
            public void run() {
                Message message = new Message.Builder()
                    .addData("content", msg.getContent())    // content
                    .addData("ts",      msg.getTimeStamp())  // timestamp
                    .addData("message", NnStringUtil.urlencode(msg.getMessage())) // message
                    .addData("logo",    NnStringUtil.urlencode(msg.getLogo()))    // logo
                    .addData("title",   NnStringUtil.urlencode(msg.getTitle()))   // title
                    .build();
                MulticastResult multicastResult;
                try {
                    log.info("sending GCM notification with content = " + msoNotification.getContent());
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
                            updateRegistration(regId, canonicalRegId, msoNotification);
                        }
                    } else {
                        String error = result.getErrorCodeName();
                        if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                            // application has been removed from device - unregister it
                            log.info("unregistered device : " + regId);
                            log.info("remove device : " + regId);
                            NnDevice device = deviceMap.get(regId);
                            if (device != null) {
                                deleteDevices.add(device);
                            }
                        } else if (error.equals(Constants.ERROR_INVALID_REGISTRATION)) {
                            NnDevice device = deviceMap.get(regId);
                            if (device != null) {
                                deleteDevices.add(device);
                            }
                            log.info("error sending message to " + regId + " : " + error);
                            log.info("remove device : " + regId);
                        } else {
                            log.severe("unknown error sending message to " + regId + " : " + error);
                        }
                    }
                }
                NNF.getDeviceMngr().delete(deleteDevices);
            }
        });
    }
    
    // kill duplicate and keep only one, replace with canonicalRegId
    private static void updateRegistration(String regId, String canonicalRegId, MsoNotification notification) {
        
        if (regId == null || canonicalRegId == null || notification == null) {
            return ;
        }
        
        List<NnDevice> fetchedDevices = NNF.getDeviceMngr().findByMsoAndType(notification.getMsoId(), NnDevice.TYPE_GCM);
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
                NNF.getDeviceMngr().save(device);
            }
            
            if (targetDevices.size() > 1) {
                NNF.getDeviceMngr().delete(targetDevices.subList(1, targetDevices.size()));
            }
        }
    }
    
    static public NnDeviceNotification buildDeviceNotification(MsoNotification msoNotification) {
        
        if (msoNotification == null) {
            return null;
        }
        NnDeviceNotification message = new NnDeviceNotification(0, msoNotification.getMessage());
        
        message.setCreateDate(new Date());
        message.setContent(msoNotification.getContent());
        
        if (msoNotification.getContent() == null) {
            return message;
        }
        String[] splits = msoNotification.getContent().split(":");
        if (splits.length < 2) {
            
            return message;
        }
        if (splits.length == 2 || splits[2] == null || splits[2].isEmpty()) {
            
            if (splits[1].matches("^[0-9]+$")) {
                
                NnChannel channel = NNF.getChannelMngr().findById(Long.valueOf(splits[1]));
                if (channel == null)
                    return message;
                String logo = null;
                if (channel.getImageUrl() != null) {
                    logo = (channel.getImageUrl().split("\\|"))[0];
                }
                if (logo != null)
                    message.setLogo(logo);
                if (channel.getName() != null)
                    message.setTitle(channel.getName());
            }
            return message;
        }
        
        String idStr = splits[2];
        if (idStr.matches("^e[0-9]+$")) { // ex: "cts:1235:e6789", NnEpisode
            
            idStr = idStr.replaceAll("^e", "");
            Long episodeId = evaluateLong(idStr);
            if (episodeId == null) {
                return message;
            }
            NnEpisode episode = NNF.getEpisodeMngr().findById(episodeId);
            if (episode == null) {
                return message;
            }
            if (episode.getImageUrl() != null) {
                message.setLogo(episode.getImageUrl());
            }
            if (episode.getName() != null) {
                message.setTitle(episode.getName());
            }
        } else if (idStr.matches("^[0-9]+$")) { // ex: "cts:1234:5678", YtProgram
            
            Long ytProgramId = evaluateLong(idStr);
            if (ytProgramId == null) {
                return message;
            }
            YtProgram ytProgram = NNF.getYtProgramMngr().findById(ytProgramId);
            if (ytProgram == null) {
                return message;
            }
            if (ytProgram.getImageUrl() != null) {
                message.setLogo(ytProgram.getImageUrl());
            }
            if (ytProgram.getName() != null) {
                message.setTitle(ytProgram.getName());
            }
        }
        
        return message;
    }
    
    static private Long evaluateLong(String stringValue) {
        
        if (stringValue == null) {
            return null;
        }
        
        Long longValue = null;
        try {
            longValue = Long.valueOf(stringValue);
        } catch (NumberFormatException e) {
            log.info("String value \"" + stringValue + "\" can't evaluate to type Long.");
            return null;
        }
        
        return longValue;
    }
}
