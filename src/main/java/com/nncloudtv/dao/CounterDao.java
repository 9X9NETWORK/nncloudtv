package com.nncloudtv.dao;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.model.Counter;

public class CounterDao extends GenericDao<Counter> {
    
    protected static final Logger log = Logger.getLogger(CounterDao.class.getName());
    
    public CounterDao() {
        super(Counter.class);
    }
    
    public Counter findByCounterName(String counterName) {
        PersistenceManager pm = getPersistenceManager();
        Counter result = null;
        log.info("counterName = " + counterName);
        try {
            Query query = pm.newQuery(Counter.class);
            query.setFilter("counterName == counterNameParam");
            query.declareParameters("String counterNameParam");
            @SuppressWarnings("unchecked")
            List<Counter> counters = (List<Counter>) query.execute(counterName);
            if (counters.size() > 0) {
                result = pm.detachCopy(counters.get(0));
            }
            query.closeAll();
        } finally {
          pm.close();
        }
        return result;
    }
}
