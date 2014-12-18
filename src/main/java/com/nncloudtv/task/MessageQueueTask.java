package com.nncloudtv.task;

import java.io.IOException;
import java.util.logging.Logger;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.QueueFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

@Service
@EnableScheduling
public class MessageQueueTask extends QueueFactory implements ScheduledTask {
    
    protected static Logger log = Logger.getLogger(MessageQueueTask.class.getName());
    static final String LOG_PREFIX = (char)27 + "[2;36m[mq]" + (char)27 + "[0m";
    
    @Scheduled(fixedRate = MQ_INTERVAL)
    synchronized public static void receiveMessage() {
        
        try {
            Connection connection = getConnection();
            Channel rabbit = connection.createChannel();
            String queueName = rabbit.queueDeclare().getQueue();
            System.out.println(LOG_PREFIX + " " + queueName);
            rabbit.queueBind(queueName, MESSAGE_EXCHANGE, "");
            QueueingConsumer consumer = new QueueingConsumer(rabbit);
            rabbit.basicConsume(queueName, consumer);
            while(true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery(MQ_INTERVAL);
                if (delivery == null) break;
                String message = new String(delivery.getBody());
                System.out.println(String.format("%s received {%s}", LOG_PREFIX, message));
            }
            rabbit.close();
            connection.close();
            
        } catch (IOException e) {
            
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
            
        } catch (ShutdownSignalException e) {
            
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
            
        } catch (ConsumerCancelledException e) {
            
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
            
        } catch (InterruptedException e) {
            
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
        }
    }
}
