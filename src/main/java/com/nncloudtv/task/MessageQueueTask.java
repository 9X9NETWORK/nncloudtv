package com.nncloudtv.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.QueueFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

@Service
@EnableScheduling
public class MessageQueueTask extends QueueFactory implements ScheduledTask {
    
    protected static Logger log = Logger.getLogger(MessageQueueTask.class.getName());
    static List<String> messages = new ArrayList<String>();
    final static int MESSAGE_ARRAY_SIZE = 10;
    
    static Channel rabbit = null;
    
    @Scheduled(fixedRate = MQ_INTERVAL)
    synchronized public static void receiveMessage() {
        
        try {
            long born = NnDateUtil.timestamp();
            if (rabbit == null || rabbit.isOpen() == false)
                rabbit = getConnection().createChannel();
            String queueName = rabbit.queueDeclare().getQueue();
            System.out.println("[mq] queueName = " + queueName);
            rabbit.queueBind(queueName, MESSAGE_EXCHANGE, "");
            QueueingConsumer consumer = new QueueingConsumer(rabbit);
            rabbit.basicConsume(queueName, consumer);
            
            while(NnDateUtil.timestamp() - born < MQ_INTERVAL) {
                
                QueueingConsumer.Delivery delivery = consumer.nextDelivery(MQ_INTERVAL);
                if (delivery == null) break;
                String message = new String(delivery.getBody());
                if (!messages.contains(message)) {
                    
                    messages.add(message);
                    System.out.println(String.format((char)27 + "[2;36m[mq]" + (char)27 + "[0m received {%s}", message));
                }
            }
            
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
            
        } finally {
            
            System.out.println("[mq] finished");
            int fromIndex = messages.size() - MESSAGE_ARRAY_SIZE;
            if (fromIndex < 0) fromIndex = 0;
            messages = messages.subList(fromIndex, messages.size());
        }
    }
    
}
