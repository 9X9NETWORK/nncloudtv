package task;

import java.util.logging.Logger;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
public class ScheduledTask {
    
    protected static Logger log = Logger.getLogger(ScheduledTask.class.getName());
    
    @Scheduled(fixedDelay = 100)
    public void checkingMemcacheServer() {
        
        log.info("Checking memcache server");
    }
    
}
