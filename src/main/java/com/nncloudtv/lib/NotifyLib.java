package com.nncloudtv.lib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.EnhancedApnsNotification;

public class NotifyLib {

    protected static final Logger log = Logger.getLogger(NotifyLib.class.getName());
    protected static String GCM_SENDER_KEY = "AIzaSyBm6bSOwftRRCX3i47gqPLPKalE3E0x6UI";
    
	public void gcmSend(String regId, String msg) {
    	log.info("gcm send:" + regId + ";msg:" + msg);
        String output = "";
        try {
            Sender sender = new Sender(GCM_SENDER_KEY);            
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Calendar cal = Calendar.getInstance();
            Message message = new Message.Builder().addData(
            		msg, dateFormat.format(cal.getTime())).build();
            output += "deviceToken " + regId + "\n";
            Result result = sender.send(message, regId, 1);
            
            /*
            Message message = new Message.Builder().build();
            Result result = sender.send(message, regId, 5);
            output += "Sent message to one device: " + result;            
            */
            
            output += result.toString();
            log.info("gcm output result:" + output);
        } catch (Exception e) {
            e.printStackTrace();
        }		
	}
	
	public static void apnsSend() {
	    log.info("get in apns func ---------------------------------------------------");
	    
        ApnsService service = APNS.newService().
                withCert("/usr/share/jetty/webapps/bartonAPNS5.p12", "111111")
                .withSandboxDestination() // Specify to use the Apple sandbox servers
                //.withProductionDestination() // Specify to use the Apple Production servers
                //.asNonBlocking() // Constructs non-blocking queues and sockets connections
                //.withDelegate(ApnsDelegate delegate)
                .build();
        
	    //String payload = APNS.newPayload().alertBody(args[3]).build();
        String token = "d8e8a8c5c4337a7b8b564a1d215d7f3691791597d9e91316413788f93a7dfa67";
        
        String payload = APNS.newPayload().alertBody("hello 1").build();
        
        EnhancedApnsNotification notification = new EnhancedApnsNotification(0 /* Next ID */,
                (int) new Date().getTime() + 60 * 60 /* Expire in one hour */,
                token /* Device Token */,
                payload);
        
        service.push(notification);
        //service.push(token, payload);
        
        String payload2 = APNS.newPayload().alertBody("hello 2").badge(1).customField("secret", "what do you think?").build();
        
        EnhancedApnsNotification notification2 = new EnhancedApnsNotification(1 /* Next ID */,
                (int) new Date().getTime() + 60 * 60 /* Expire in one hour */,
                token /* Device Token */,
                payload2);
        service.push(notification2);
        //service.push(token, payload);
	}
}
