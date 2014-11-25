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
    
    @Scheduled(fixedDelay = 108301)
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
            if (shardSet.size() > 0)
                NNF.getShardDao().saveAll(shardSet);
            System.out.println(String.format("[counter] cleaning dirty counters costs %d milliseconds", NnDateUtil.timestamp() - before));
        }
    }
}
