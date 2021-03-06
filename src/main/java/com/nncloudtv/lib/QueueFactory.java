package com.nncloudtv.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.logging.Logger;


import com.nncloudtv.service.MsoConfigManager;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class QueueFactory {

    protected static final Logger log = Logger.getLogger(QueueFactory.class.getName());
    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";
    public static final String CONTENTTYPE_JSON = "json";
    public static final String CONTENTTYPE_TEXT = "text";
    
    public final static String QUEUE_NNCLOUDTV = "QUEUE_NNCLOUDTV";
    
    public static byte[] toByteArray (Object obj)
    {
       byte[] bytes = null;
       ByteArrayOutputStream bos = new ByteArrayOutputStream();
       try {
          ObjectOutputStream oos = new ObjectOutputStream(bos); 
          oos.writeObject(obj);
          oos.flush(); 
          oos.close(); 
          bos.close();
          bytes = bos.toByteArray ();
       } catch (IOException e) {
           e.printStackTrace();
       }
      return bytes;
    }
    
    public static void add(String url, String method, String contentType, Object data) {
        ConnectionFactory factory = new ConnectionFactory();
        try {
            Properties properties = new Properties();
            properties.load(CacheFactory.class.getClassLoader().getResourceAsStream("queue.properties"));
            String server = properties.getProperty("server");
            log.info("queue server:" + server);
            factory.setHost(server);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(QueueFactory.QUEUE_NNCLOUDTV, true, false, false, null);
            
            Object[] obj = new Object[4];
            String root = "http://localhost:8080";
            if (!server.equals("localhost")) {
                root = "http://" + MsoConfigManager.getServerDomain(); // configurable
            }
            log.info("queue root:" + root);
            String msgUrl = root.concat(url);
            obj[0] = msgUrl;
            obj[1] = method;
            obj[2] = contentType;
            obj[3] = data;
            channel.basicPublish( "", QueueFactory.QUEUE_NNCLOUDTV, 
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        QueueFactory.toByteArray(obj));
            channel.close();
            connection.close();        
            log.info(" [x] Sent '" + msgUrl.toString() + "'");
        } catch (IOException e) {
            e.printStackTrace();
        }        
        
    }
    
    //for now, assuming if obj is not null, then it's a json request
    public static void add(String url, Object json) {
        ConnectionFactory factory = new ConnectionFactory();
        try {
            Properties properties = new Properties();
            properties.load(CacheFactory.class.getClassLoader().getResourceAsStream("queue.properties"));
            String server = properties.getProperty("server");
            log.info("queue server:" + server);
            factory.setHost(server);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(QueueFactory.QUEUE_NNCLOUDTV, true, false, false, null);
            
            Object[] obj = new Object[4];
            String root = "http://localhost:8080";
            if (!server.equals("localhost")) {
                root = "http://" + MsoConfigManager.getServerDomain(); // configurable
            }
            log.info("queue root:" + root);
            String msg = root.concat(url);
            
            obj[0] = msg;
            if (json == null) {
                obj[1] = QueueFactory.METHOD_GET;
                obj[2] = QueueFactory.CONTENTTYPE_TEXT;
            } else {
                obj[1] = QueueFactory.METHOD_POST;
                obj[2] = QueueFactory.CONTENTTYPE_JSON;
            }
            obj[3] = json; 
            
            channel.basicPublish( "", QueueFactory.QUEUE_NNCLOUDTV, 
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        QueueFactory.toByteArray(obj));                        
            channel.close();
            connection.close();        
            log.info(" [x] Sent '" + msg.toString() + "'");
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    
}
