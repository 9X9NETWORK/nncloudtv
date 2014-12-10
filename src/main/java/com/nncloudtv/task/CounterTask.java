package com.nncloudtv.task;

import java.util.HashSet;
import java.util.Map.Entry;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.QueueFactory;
import com.nncloudtv.model.CounterShard;
import com.nncloudtv.service.CounterFactory;

@Service
@EnableScheduling
public class CounterTask extends CounterFactory implements ScheduledTask {
    
    @Scheduled(fixedDelay = CC_INTERVAL)
    public void cleanDirtyCounter() {
        synchronized (dirtyCounters) {
            if (dirtyCounters.isEmpty())
                return;
            long before = NnDateUtil.timestamp();
            HashSet<CounterShard> shardSet = new HashSet<CounterShard>();
            HashSet<Entry<String, Integer>> counterSet = new HashSet<Entry<String,Integer>>(dirtyCounters.entrySet());
            dirtyCounters.clear();
            for (Entry<String, Integer> entry : counterSet)
                shardSet.add(increment(entry.getKey(), entry.getValue()));
            NNF.getShardDao().saveAll(shardSet);
            System.out.println(String.format("[counter] %d counters updated", shardSet.size()));
            QueueFactory.publishMessage(String.format("%d counters updated", shardSet.size()));
            System.out.println(String.format("[counter] cleaning dirty counters costs %d milliseconds", NnDateUtil.timestamp() - before));
        }
    }
}
