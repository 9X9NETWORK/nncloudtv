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

package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.CacheFactory;
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
public class CounterShardDao extends GenericDao<CounterShard> {
    
    protected static final Logger log = Logger.getLogger(CounterShardDao.class.getName());
    
    @Override
    public CounterShard save(CounterShard dao) {
        
        if (dao == null) return null;
        
        // cache delete
        String cacheKey = CacheFactory.getCounterShardKey(dao.getCounterName());
        CacheFactory.delete(cacheKey);
        
        return super.save(dao);
    }
    
    @Override
    public Collection<CounterShard> saveAll(Collection<CounterShard> list) {
        
        List<String> cacheKeyList = new ArrayList<String>();
        
        // cache delete
        for (CounterShard shard : list)
            cacheKeyList.add(shard.getCounterName());
        CacheFactory.deleteAll(cacheKeyList);
        
        return super.saveAll(list);
    }
    
    public CounterShardDao() {
        super(CounterShard.class);
    }
    
    @SuppressWarnings("unchecked")
    public List<CounterShard> findByCounterName(String counterName) {
        
        List<CounterShard> results = new ArrayList<CounterShard>();
        // cache get
        String cacheKey = CacheFactory.getCounterShardKey(counterName);
        results = (List<CounterShard>) CacheFactory.get(cacheKey);
        if (results != null)
            return results;
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(CounterShard.class);
            query.setFilter("counterName == counterNameParam");
            query.declareParameters("String counterNameParam");
            List<CounterShard> counterShards = (List<CounterShard>) query.execute(counterName);
            if (counterShards.size() > 0) {
                results = (List<CounterShard>) pm.detachCopyAll(counterShards);
            }
            query.closeAll();
        } finally {
            pm.close();
        }
        // cache set
        CacheFactory.set(cacheKey, results);
        return results;
    }
}