package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.datastore.DataStoreCache;

import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.model.PersistentModel;

public class ShardedDao<T extends PersistentModel> {
    
    protected final Class<T> daoClass;
    protected final String daoClassName;
    
    protected static final Logger log = Logger.getLogger(ShardedDao.class.getName());
    
    public ShardedDao(Class<T> daoClass) {
        this.daoClass = daoClass;
        this.daoClassName = daoClass.getSimpleName();
    }
    
    public void evictAll(PersistenceManagerFactory pmf) {
        try {
            DataStoreCache cache = pmf.getDataStoreCache();
            if (cache != null)
                cache.evictAll();
        } catch (IllegalArgumentException e) {
            log.warning(e.getMessage());
        }
    }
    
    public void evict(T dao, PersistenceManagerFactory pmf) {
        
        try {
            DataStoreCache cache = pmf.getDataStoreCache();
            if (cache != null)
                cache.evict(dao);
        } catch (IllegalArgumentException e) {
            log.warning(e.getMessage());
        }
    }
    
    public T save(T dao, PersistenceManagerFactory pmf) {
        
        if (dao == null) return null;
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            dao = pm.detachCopy(pm.makePersistent(dao));
        } finally {
            pm.close();
        }
        System.out.println(String.format("[dao] %s.save(%d)", daoClassName, dao.getId()));
        return dao;
    }
    
    @SuppressWarnings("unchecked")
    public List<T> findAllByIds(Collection<Long> ids, PersistenceManagerFactory pmf) {
        
        List<T> results = new ArrayList<T>();
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            Query query = pm.newQuery(daoClass, ":p.contains(id)");
            results = (List<T>) pm.detachCopyAll((List<T>) query.execute(ids));
            query.closeAll();
        } finally {
            pm.close();
        }
        return results;
    }
    
    protected T findById(long id, PersistenceManagerFactory pmf) {
        
        T dao = null;
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            dao = (T) pm.detachCopy((T) pm.getObjectById(daoClass, id));
        } catch (JDOObjectNotFoundException e) {
        } finally {
            pm.close();
        }
        return dao;
    }
    
    protected List<T> sql(String queryStr, PersistenceManager pm) {
        
        return sql(queryStr, pm, false);
    }
    
    private List<T> sql(String queryStr, PersistenceManager pm, boolean fine) {
        
        synchronized (pm) {
            
            List<T> detached = new ArrayList<T>();
            if (queryStr == null || queryStr.isEmpty()) {
                
                return detached;
            }
            queryStr = queryStr.replaceAll(" +", " ").trim();
            if (queryStr.isEmpty()) {
                
                return detached;
            }
            if (!fine)
                System.out.println(String.format("[sql] %s;", queryStr));
            long before = NnDateUtil.timestamp();
            try {
                Query query = pm.newQuery("javax.jdo.query.SQL", queryStr);
                query.setClass(daoClass);
                @SuppressWarnings("unchecked")
                List<T> results = (List<T>) query.execute();
                detached = (List<T>) pm.detachCopyAll(results);
                query.closeAll();
            } finally {
                pm.flush();
            }
            if (!fine)
                System.out.println(String.format("[sql] %d items returned, costs %d milliseconds", detached.size(), NnDateUtil.timestamp() - before));
            return detached;
        }
        
    }
    
    @Override
    protected void finalize() throws Throwable {
        
        NnLogUtil.logFinalize(daoClassName);
    }
}
