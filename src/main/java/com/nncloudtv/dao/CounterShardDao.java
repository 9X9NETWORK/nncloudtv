package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.model.CounterShard;

public class CounterShardDao extends GenericDao<CounterShard> {
    
    protected static final Logger log = Logger.getLogger(CounterShardDao.class.getName());
    
    public CounterShardDao() {
        super(CounterShard.class);
    }
    
    public List<CounterShard> findByCounterName(String counterName) {
        
        List<CounterShard> results = new ArrayList<CounterShard>();
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(CounterShard.class);
            query.setFilter("counterName == counterNameParam");
            query.declareParameters("String counterNameParam");
            @SuppressWarnings("unchecked")
            List<CounterShard> counterShards = (List<CounterShard>) query.execute(counterName);
            if (counterShards.size() > 0) {
                results = (List<CounterShard>) pm.detachCopyAll(counterShards);
            }
            query.closeAll();
        } finally {
            pm.close();
        }
        return results;
    }
}