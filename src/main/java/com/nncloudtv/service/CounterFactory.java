package com.nncloudtv.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.Counter;
import com.nncloudtv.model.CounterShard;

/**
 * A counter which can be incremented rapidly.
 *
 * Capable of incrementing the counter and increasing the number of shards.
 * When incrementing, a random shard is selected to prevent a single shard
 * from being written to too frequently. If increments are being made too
 * quickly, increase the number of shards to divide the load. Performs
 * datastore operations using JDO.
 *
 */
public class CounterFactory {
    
    protected static Map<String, Integer> dirtyCounters = new HashMap<String, Integer>();
    
    synchronized private static Counter getOrCreateCounter(String counterName) {
        
        if (counterName == null || counterName.isEmpty()) return null;
        
        Counter counter = NNF.getCounterDao().findByCounterName(counterName);
        if (counter == null) {
            counter = new Counter(counterName);
            System.out.println(String.format("[counter] created {%s}", counter.getCounterName()));
        }
        
        return counter;
    }
    
    public static long getCount(String counterName) {
        
        if (counterName == null) return 0;
        
        List<CounterShard> shards = NNF.getShardDao().findByCounterName(counterName);
        long sum = 0;
        for (CounterShard shardCounter : shards) {
            sum += shardCounter.getCount();
        }
        
        return sum;
    }
    
    public static void increment(String counterName) {
        
        synchronized (dirtyCounters) {
            
            if (counterName == null) return;
            Integer count = dirtyCounters.get(counterName);
            dirtyCounters.put(counterName, count == null ? 1 : count + 1);
        }
    }
    
    synchronized protected static CounterShard increment(String counterName, int amount) {
        
        if (counterName == null) return null;
        System.out.println(String.format("[counter] {%s} +%d", counterName, amount));
        List<CounterShard> shards = NNF.getShardDao().findByCounterName(counterName);
        if (shards.isEmpty())
            shards.add(addShard(counterName));
        int index = 0;
        int shardSize = shards.size();
        Random random = new Random(NnDateUtil.timestamp());
        index = random.nextInt(shardSize);
        CounterShard randShard = shards.get(index);
        randShard.increment(amount);
        
        long sum = 0;
        for (CounterShard shardCounter : shards) {
            sum += shardCounter.getCount();
        }
        
        if (sum - index * 10 > shardSize * shardSize * 10000) {
            /**
             * Sharding Formula
             * 
             * To estimate how many shards needed
             * and auto adding shards as well 
             * 
             *      0 ~   9999: 1 shard
             *  10000 ~  39999: 2 shards
             *  40000 ~  89999: 3 shards
             *  90000 ~ 159999: 4 shards
             * 160000 ~ 249999: 5 shards
             * ... etc ...
             * 
             * @author Louis Jeng <louis.jeng@flipr.tv>
             */
            int numShards = (int) Math.sqrt(sum) / 100 + 1; // Sharding Formula
            System.out.println(String.format("[counter] {%s} shard number = %d (%d expected), count = %d",
                                             counterName, shardSize, numShards, sum));
            if (numShards > shardSize)
                addShard(counterName); // auto sharding
        }
        
        return randShard;
    }
    
    synchronized private static CounterShard addShard(String counterName) {
        
        if (counterName == null) return null;
        
        Counter counter = getOrCreateCounter(counterName);
        int shardNum = counter.getNumShards() + 1;
        counter.setNumShards(shardNum);
        CounterShard shard = NNF.getShardDao().save(new CounterShard(counter.getCounterName(), shardNum));
        NNF.getCounterDao().save(counter);
        System.out.println(String.format("[counter] add shard to {%s}, total shard number = %d", counter.getCounterName(), counter.getNumShards()));
        
        return shard;
    }
    
}