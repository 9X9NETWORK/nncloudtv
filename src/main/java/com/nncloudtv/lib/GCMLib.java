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
import com.nncloudtv.model.NnEpisode;
import com.nncloudtv.model.YtProgram;
import com.nncloudtv.service.NnEpisodeManager;
import com.nncloudtv.service.YtProgramManager;

@Service
public class GCMLib {
    
    protected static final Logger log = Logger.getLogger(GCMLib.class.getName());
    private static final Executor threadPool = Executors.newFixedThreadPool(5);
    private static final int MULTICAST_SIZE = 1000;
    
    private NnDeviceDao deviceDao = new NnDeviceDao();
    private NnEpisodeManager episodeMngr = new NnEpisodeManager();
    private YtProgramManager ytProgramMngr = new YtProgramManager();
    
    public void doPost(MsoNotification msoNotification, String apiKey) {
        
        if (msoNotification == null || apiKey == null) {
            return ;
        }
        
        log.info("gcm sender, send to mso id = " + msoNotification.getMsoId());
        
        Sender sender = new Sender(apiKey);
        
        GCMessage gcMessage = buildGCMessage(msoNotification);
        if (gcMessage == null) {
            return ;
        }
        
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
                asyncSend(sender, partialDevices, msoNotification, gcMessage, deviceMap);
                partialDevices.clear();
                tasks++;
            }
        }
        log.info("Asynchronously sending " + tasks + " multicast messages to " + total + " devices");
    }
    
    private void asyncSend(Sender gcmSender, List<String> partialDevices, MsoNotification msoNotification,
            GCMessage gcMessage, Map<String, NnDevice> allDeviceMap) {
        
        // make a copy
        final List<String> devices = new ArrayList<String>(partialDevices);
        final MsoNotification notification = msoNotification;
        final GCMessage gcmessage = gcMessage;
        final Sender sender = gcmSender;
        final Map<String, NnDevice> deviceMap = allDeviceMap;
        
        threadPool.execute(new Runnable() {
            public void run() {
                
                Message message = new Message.Builder()
                    .addData("message", gcmessage.getMessage()) // message
                    .addData("content", gcmessage.getContent()) // content
                    .addData("ts", gcmessage.getTs())  // ts
                    .addData("logo", gcmessage.getLogo()) // logo
                    .addData("title", gcmessage.getTitle()) // title
                    .build();
                MulticastResult multicastResult;
                try {
                    log.info("sending GCM notification with content = " + notification.getContent());
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
                            log.info("Unregistered device : " + regId);
                            log.info("Remove device : " + regId);
                            NnDevice device = deviceMap.get(regId);
                            if (device != null) {
                                deleteDevices.add(device);
                            }
                        } else if (error.equals(Constants.ERROR_INVALID_REGISTRATION)) {
                            NnDevice device = deviceMap.get(regId);
                            if (device != null) {
                                deleteDevices.add(device);
                            }
                            log.severe("Error sending message to " + regId + " : " + error);
                            log.info("Remove device : " + regId);
                        } else {
                            log.severe("Error sending message to " + regId + " : " + error);
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
    
    private class GCMessage {
        
        private String ts;
        private String content;
        private String title;
        private String logo;
        private String message;
        
        public String getTs() {
            return ts;
        }
        public void setTs(String ts) {
            this.ts = ts;
        }
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public String getLogo() {
            return logo;
        }
        public void setLogo(String logo) {
            this.logo = logo;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    private GCMessage buildGCMessage(MsoNotification msoNotification) {
        
        if (msoNotification == null) {
            return null;
        }
        
        GCMessage message = new GCMessage();
        
        Date now = new Date();
        message.setTs(String.valueOf(now.getTime()));
        message.setContent(msoNotification.getContent());
        message.setMessage(NnStringUtil.urlencode(msoNotification.getMessage(), NnStringUtil.UTF8));
        message.setLogo("");
        message.setTitle("");
        
        if (msoNotification.getContent() == null) {
            return message;
        }
        String[] splits = msoNotification.getContent().split(":");
        if (splits.length < 3) {
            return message;
        }
        String idStr = splits[2];
        if (idStr.contains("yt")) { // ex: "cts:1236:ytXXXXXXXXXXX"
            return message;
        }
        if (idStr.contains("e")) { // ex: "cts:1235:e6789", NnEpisode
            
            idStr = idStr.replace("e", "");
            Long episodeId = evaluateLong(idStr);
            if (episodeId == null) {
                return message;
            }
            NnEpisode episode = episodeMngr.findById(episodeId);
            if (episode == null) {
                return message;
            }
            if (episode.getImageUrl() != null) {
                message.setLogo(NnStringUtil.urlencode(episode.getImageUrl(), NnStringUtil.UTF8));
            }
            if (episode.getName() != null) {
                message.setTitle(NnStringUtil.urlencode(episode.getName(), NnStringUtil.UTF8));
            }
        } else { // ex: "cts:1234:5678", YtProgram // ex: "cts:1234:"
            
            Long ytProgramId = evaluateLong(idStr);
            if (ytProgramId == null) {
                return message;
            }
            YtProgram ytProgram = ytProgramMngr.findById(ytProgramId);
            if (ytProgram == null) {
                return message;
            }
            if (ytProgram.getImageUrl() != null) {
                message.setLogo(NnStringUtil.urlencode(ytProgram.getImageUrl(), NnStringUtil.UTF8));
            }
            if (ytProgram.getName() != null) {
                message.setTitle(NnStringUtil.urlencode(ytProgram.getName(), NnStringUtil.UTF8));
            }
        }
        
        return message;
    }
    
    private Long evaluateLong(String stringValue) {
        
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
