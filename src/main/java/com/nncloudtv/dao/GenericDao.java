package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.DataStoreCache;

import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.PersistentModel;
import com.nncloudtv.service.CounterFactory;

public class GenericDao<T extends PersistentModel> {
    
    protected final Class<T> daoClass;
    protected final String daoClassName;
        
    public GenericDao(Class<T> daoClass) {
        this.daoClass = daoClass;
        this.daoClassName = daoClass.getSimpleName();
    }
    
    public void evictAll() {
        DataStoreCache cache = PMF.get(daoClass).getDataStoreCache();
        if (cache != null) {
            cache.evictAll();
        }
    }
    
    public void evict(T dao) {
        DataStoreCache cache = PMF.get(daoClass).getDataStoreCache();
        if (cache != null) {
            cache.evict(dao);
        }
    }
    
    protected PersistenceManager getPersistenceManager() {
        
        return PMF.get(daoClass).getPersistenceManager();
    }
    
    public T save(T dao) {
        
        return save(dao, getPersistenceManager());
    }
    
    public T save(T dao, PersistenceManager pm) {
        
        if (dao == null) return null;
        try {
            pm.makePersistent(dao);
            dao = pm.detachCopy(dao);
        } finally {
            pm.close();
        }
        String cacheKey = CacheFactory.getFindByIdKey(daoClassName, dao.getId());
        CacheFactory.delete(cacheKey);
        String msg = String.format("[dao] %s", cacheKey);
        System.out.println(msg);
        CounterFactory.increment(msg);
        return dao;
    }
    
    public Collection<T> saveAll(Collection<T> list) {
        
        if (list == null) return null;
        long before = NnDateUtil.timestamp();
        PersistenceManager pm = getPersistenceManager();
        //List<String> cacheKeys = new ArrayList<String>();
        //for (T dao : list) {
        //    if (dao.getId() > 0)
        //        cacheKeys.add(CacheFactory.getFindByIdKey(daoClassName, dao.getId()));
        //}
        //CacheFactory.delete(cacheKeys);
        String msg = String.format("[dao] %s.saveAll()", daoClassName);
        System.out.println(msg);
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            list = pm.makePersistentAll(list);
            list = (Collection<T>) pm.detachCopyAll(list);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
            System.out.println(String.format("[dao] saveAll() costs %d miliseconds", NnDateUtil.timestamp() - before));
        }
        CounterFactory.increment(msg);
        return list;
    }
    
    public void delete(T dao) {
        
        if (dao == null) { return; }
        PersistenceManager pm = getPersistenceManager();
        String cacheKey = CacheFactory.getFindByIdKey(daoClassName, dao.getId());
        CacheFactory.delete(cacheKey);
        String msg = String.format("[dao] %s", cacheKey);
        System.out.println(msg);
        try {
            pm.deletePersistent(dao);
        } finally {
            pm.close();
        }
        CounterFactory.increment(msg);
    }
    
    public void deleteAll(Collection<T> list) {
        
        if (list == null || list.isEmpty()) return;
        
        PersistenceManager pm = getPersistenceManager();
//        List<String> cacheKeys = new ArrayList<String>();
//        for (T dao : list)
//            cacheKeys.add(CacheFactory.getFindByIdKey(daoClassName, dao.getId()));
//        CacheFactory.delete(cacheKeys);
        Transaction tx = pm.currentTransaction();
        String msg = String.format("[dao] %s.deleteAll()", daoClassName);
        System.out.println(msg);
        try {
            tx.begin();
            pm.deletePersistentAll(list);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                System.out.println("[dao] rolling back");
                tx.rollback();
            }
            pm.close();
        }
        CounterFactory.increment(msg);
    }
    
    /**
     * Get total number of objects
     */
    
    public int total() {
        
        return total(null);
    }
    
    @SuppressWarnings("unchecked")
    public int total(String filter) {
        PersistenceManager pm = getPersistenceManager();
        String msg = String.format("[dao] %s.total(\"%s\")", daoClassName, filter);
        System.out.println(msg);
        int result = 0;
        try {
            Query query = pm.newQuery(daoClass);
            if (filter != null && !filter.isEmpty())
                query.setFilter(filter);
            result = ((List<T>) query.execute()).size();
            query.closeAll();
        } finally {
            pm.close();
        }
        CounterFactory.increment(msg);
        return result;
    }
    
    /**
     * List objects by specified criteria
     *
     * @param page   the page number (start from 1)
     * @param limit  number of items per page
     * @param sort   sorting field
     */
    public List<T> list(int page, int limit, String sort) {
        PersistenceManager pm = getPersistenceManager();
        String msg = String.format("[dao] %s.list(%d, %d, \"%s\")", daoClassName, page, limit, sort);
        System.out.println(msg);
        List<T> results;
        try {
            Query query = pm.newQuery(daoClass);
            if (sort != null)
                query.setOrdering(sort);
            query.setRange((page - 1) * limit, page * limit);
            @SuppressWarnings("unchecked")
            List<T> tmp = (List<T>) query.execute();
            results = (List<T>) pm.detachCopyAll(tmp);
            query.closeAll();
        } finally {
            pm.close();
        }
        CounterFactory.increment(msg);
        return results;
    }
    
    public List<T> list(long page, long limit, String sort, String filter) {
        PersistenceManager pm = getPersistenceManager();
        String msg = String.format("[dao] %s.list(%d, %d, \"%s\", \"%s\")", daoClassName, page, limit, sort, filter);
        System.out.println(msg);
        List<T> results;
        try {
            Query query = pm.newQuery(daoClass);
            if (filter != null)
                query.setFilter(filter);
            if (sort != null)
                query.setOrdering(sort);
            query.setRange((page - 1) * limit, page * limit);
            @SuppressWarnings("unchecked")
            List<T> tmp = (List<T>) query.execute();
            results = (List<T>) pm.detachCopyAll(tmp);
            query.closeAll();
        } finally {
            pm.close();
        }
        CounterFactory.increment(msg);
        return results;
    }
    
    public List<T> findAllByIds(Collection<Long> ids) {
        
        return findAllByIds(ids, getPersistenceManager());
    }
    
    @SuppressWarnings("unchecked")
    public List<T> findAllByIds(Collection<Long> ids, PersistenceManager pm) {
        
        List<T> results = new ArrayList<T>();
        try {
            Query query = pm.newQuery(daoClass, ":p.contains(id)");
            results = (List<T>) pm.detachCopyAll((List<T>) query.execute(ids));
            query.closeAll();
        } finally {
            pm.close();
        }
        return results;
    }
    
    public T findById(String idStr) {
        
        if (idStr == null || !NnStringUtil.isDigits(idStr)) {
            
            return null;
        }
        
        long id = 0;
        try {
            id = Long.valueOf(idStr);
            
        } catch(NumberFormatException e) {
            
            return null;
        }
        return findById(id);
    }
    
    public T findById(long id) {
        
        return findById(id, getPersistenceManager());
    }
    
    @SuppressWarnings("unchecked")
    public T findById(long id, PersistenceManager pm) {
        
        T dao = null;
        String cacheKey = CacheFactory.getFindByIdKey(daoClassName, id);
        CounterFactory.increment(String.format("[dao] %s", cacheKey));
        dao = (T) CacheFactory.get(cacheKey);
        if (dao != null) // hit
            return dao;
        try {
            dao = (T) pm.detachCopy((T) pm.getObjectById(daoClass, id));
        } catch (JDOObjectNotFoundException e) {
        } finally {
            pm.close();
        }
        CacheFactory.set(cacheKey, dao);
        return dao;
    }
    
    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        
        PersistenceManager pm = getPersistenceManager();
        List<T> results = new ArrayList<T>();
        
        try {
            Query query = pm.newQuery(daoClass);
            results = (List<T>) pm.detachCopyAll((List<T>) query.execute());
        } catch (JDOObjectNotFoundException e) {
        } finally {
            pm.close();
        }
        return results;
    }
    
    public List<T> sql(String queryStr, boolean fine) {
        
        return sql(queryStr, getPersistenceManager(), fine);
    }
    
    public List<T> sql(String queryStr) {
        
        return sql(queryStr, getPersistenceManager(), false);
    }
    
    public List<T> sql(String queryStr, PersistenceManager pm) {
        
        return sql(queryStr, pm, false);
    }
    
    public List<T> sql(String queryStr, PersistenceManager pm, boolean fine) {
        
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
            if (!fine)
                System.out.println(String.format("[sql] %d items returned, costs %d milliseconds", detached.size(), NnDateUtil.timestamp() - before));
            
        } finally {
            
            pm.close();
        }
        CounterFactory.increment("[dao] sql_query");
        return detached;
    }
    
    @Override
    protected void finalize() throws Throwable {
        
        NnLogUtil.logFinalize(getClass().getName());
    }
}
