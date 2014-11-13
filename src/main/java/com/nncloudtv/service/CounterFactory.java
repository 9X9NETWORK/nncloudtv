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

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.Counter;
import com.nncloudtv.model.CounterShard;

/**
 * Finds or creates a sharded counter with the desired name.
 */
public class CounterFactory {
    protected static final Logger log = Logger.getLogger(CounterFactory.class.getName());
    
    public static Counter getOrCreateCounter(String counterName) {
        
        Counter counter = NNF.getCounterDao().findByCounterName(counterName);
        if (counter == null) {
            // Create a counter with 0 shards.
            counter = NNF.getCounterDao().save(new Counter(counterName));
            log.info("created counter " + counterName);
            // Add a first shard to the counter.
            addShard(counter);
            
            return counter;
        }
        
        return populateCount(counter);
    }
    
    private static Counter populateCount(Counter counter) {
        
        if (counter == null) return null;
        
        List<CounterShard> shards = NNF.getShardDao().findByCounterName(counter.getCounterName());
        long sum = 0;
        for (CounterShard shardCounter : shards) {
            sum += shardCounter.getCount();
        }
        counter.setCount(sum);
        
        return counter;
    }
    
    public static void increment(Counter counter) {
        
        increment(counter, 1);
    }
    
    public static void increment(Counter counter, int amount) {
        
        if (counter == null) return;
        int next = 0;
        List<CounterShard> shards = NNF.getShardDao().findByCounterName(counter.getCounterName());
        if (shards.size() > 0) {
            Random random = new Random(NnDateUtil.timestamp());
            next = random.nextInt(shards.size());
            CounterShard shardCounter = shards.get(next);
            shardCounter.increment(amount);
            shardCounter = NNF.getShardDao().save(shardCounter);
        }
        //long sum = 0;
        //for (CounterShard shardCounter : shards) {
        //    sum += shardCounter.getCount();
        //}
        
        
        
//        int numShards = (int) Math.sqrt(sum) / 100 + 1; // Sharding Fomula
//        if (numShards > counter.getNumShards()) {
//            addShard(counter);
//        }
        
        
        
    }
    
    public static void addShard(Counter counter) {
        
        if (counter == null) return;
        
        int shardNum = counter.getNumShards() + 1;
        counter.setNumShards(shardNum);
        NNF.getShardDao().save(new CounterShard(counter.getCounterName(), shardNum));
        NNF.getCounterDao().save(counter);
    }
    
    public static Counter getCounter(String counterName) {
        
        Counter counter = NNF.getCounterDao().findByCounterName(counterName);
        if (counter == null)
            return null;
        
        return populateCount(counter);
    }
    
}