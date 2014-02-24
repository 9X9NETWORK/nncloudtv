package com.nncloudtv.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    // hard coded for test purpose
    private static final String GCM_SENDER_KEY = "AIzaSyAXlEvWnLCNF0yL1GnZb-U0YRxG2WRvAc4";
    
    private NnDeviceDao deviceDao = new NnDeviceDao();
    
    public void doPost(MsoNotification msoNotification) {
        
        log.info("send to mso id=" + msoNotification.getMsoId());
        
        // TODO find GCM key from given msoNotification.getMsoId()
        Sender sender = new Sender(GCM_SENDER_KEY);
        
        List<NnDevice> fetchedDevices = deviceDao.findByMsoAndType(msoNotification.getMsoId(), NnDevice.TYPE_GCM);
        List<String> devices = new ArrayList<String>();
        if (fetchedDevices != null) {
            for (NnDevice device : fetchedDevices) {
                devices.add(device.getToken());
            }
        }
        devices.add("APA91bGsJpHEvbL5GvWDjghV8O06E9U0JbTsi5-cScMaOUxtFkDUOYMtzYXstmZ3MX_ZHhAgM3lo" +
        		"1iS9VI-l8XIS6mzZcLf4TrFy4cLnCsW8uzSVTZ9Fl1tlmxcgqDnIGeymnisAVZH76ux_f0VURedHbQiwdxIVzg");
        
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
                asyncSend(sender, partialDevices, msoNotification);
                partialDevices.clear();
                tasks++;
            }
        }
        log.info("Asynchronously sending " + tasks + " multicast messages to " + total + " devices");
    }
    
    private void asyncSend(Sender gcmSender, List<String> partialDevices, MsoNotification msoNotification) {
        
        // make a copy
        final List<String> devices = new ArrayList<String>(partialDevices);
        final MsoNotification notification = msoNotification;
        final Sender sender = gcmSender;
        
        threadPool.execute(new Runnable() {
            public void run() {
                
                Message message = new Message.Builder()
                    .addData("message", notification.getMessage()) // message
                    .addData("content", notification.getContent()) // content
                    .build();
                MulticastResult multicastResult;
                try {
                    multicastResult = sender.send(message, devices, 5);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error posting messages", e);
                    return;
                }
                
                List<Result> results = multicastResult.getResults();
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
                            // same device has more than on registration id: update it
                            log.info("canonicalRegId " + canonicalRegId);
                            // TODO Datastore.updateRegistration(regId, canonicalRegId);
                        }
                    } else {
                        String error = result.getErrorCodeName();
                        if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                            // application has been removed from device - unregister it
                            log.info("Unregistered device: " + regId);
                            // TODO Datastore.unregister(regId);
                        } else {
                            log.severe("Error sending message to " + regId + ": " + error);
                        }
                    }
                }
            }
        });
    }

}
