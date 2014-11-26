/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Finds or creates a sharded counter with the desired name.
 */
public class CounterFactory {
    
    protected static Map<String, Integer> dirtyCounters = new HashMap<String, Integer>();
    
    synchronized private static Counter getOrCreateCounter(String counterName) {
        
        if (counterName == null || counterName.isEmpty()) return null;
        
        Counter counter = NNF.getCounterDao().findByCounterName(counterName);
        if (counter == null) {
            // Create a counter with 0 shards.
            counter = NNF.getCounterDao().save(new Counter(counterName));
            System.out.println(String.format("[counter] created {%s}, id = %d", counter.getCounterName(), counter.getId()));
            
            return counter;
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
        
        List<CounterShard> shards = NNF.getShardDao().findByCounterName(counterName);
        if (shards.isEmpty())
            shards.add(addShard(getOrCreateCounter(counterName)));
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
             * 
             *      0 ~   9999: 1 shard
             *  10000 ~  39999: 2 shards
             *  40000 ~  89999: 3 shards
             *  90000 ~ 159999: 4 shards
             * 160000 ~ 249999: 5 shards
             * ... etc
             * 
             * @author Louis Jeng <louis.jeng@flipr.tv>
             */
            int numShards = (int) Math.sqrt(sum) / 100 + 1; // Sharding Formula
            Counter counter = getOrCreateCounter(counterName);
            System.out.println(String.format("[counter] {%s} shard number = %d (%d expected), count = %d",
                                   counter.getCounterName(), counter.getNumShards(), numShards, sum));
            if (numShards > shardSize) {
                counter.setNumShards(shardSize);
                addShard(counter); // auto sharding
            }
        }
        
        return randShard;
    }
    
    synchronized private static CounterShard addShard(Counter counter) {
        
        if (counter == null) return null;
        
        int shardNum = counter.getNumShards() + 1;
        counter.setNumShards(shardNum);
        CounterShard shard = NNF.getShardDao().save(new CounterShard(counter.getCounterName(), shardNum));
        NNF.getCounterDao().save(counter);
        System.out.println(String.format("[counter] add shard to {%s}, total shard number = %d", counter.getCounterName(), counter.getNumShards()));
        
        return shard;
    }
    
}