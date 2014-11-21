package com.nncloudtv.task;

import java.util.HashSet;
import java.util.Map.Entry;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.CounterShard;
import com.nncloudtv.service.CounterFactory;

@Service
@EnableScheduling
public class CounterTask extends CounterFactory {
    
    public static final int CC_INTERVAL = 294001; // clean counter interval (milliseconds)
    
    @Scheduled(fixedDelay = CC_INTERVAL)
    public void cleanDirtyCounter() {
        synchronized (dirtyCounters) {
            long before = NnDateUtil.timestamp();
            HashSet<CounterShard> shardSet = new HashSet<CounterShard>();
            HashSet<Entry<String, Integer>> counterSet = new HashSet<Entry<String,Integer>>(dirtyCounters.entrySet());
            dirtyCounters.clear();
            for (Entry<String, Integer> entry : counterSet) {
                if (entry.getValue() > 1) {
                    shardSet.add(increment(entry.getKey(), entry.getValue()));
                    System.out.println(String.format("[counter] \"%s\" increment %d", entry.getKey(), entry.getValue()));
                } else {
                    dirtyCounters.put(entry.getKey(), entry.getValue()); // put it back
                }
            }
            if (shardSet.size() > 0)
                NNF.getShardDao().saveAll(shardSet);
            System.out.println(String.format("[counter] cleaning dirty counters costs %d milliseconds", NnDateUtil.timestamp() - before));
        }
    }
}
