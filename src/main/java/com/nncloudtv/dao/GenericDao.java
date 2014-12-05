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
import com.nncloudtv.model.PersistentBaseModel;
import com.nncloudtv.service.CounterFactory;

public class GenericDao<T extends PersistentBaseModel> {
    
    protected final Class<T> daoClass;
    protected final String daoClassName;
    
    private PersistenceManager sharedPersistenceMngr = null; // shared, not closed
    
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
    
    private PersistenceManager getSharedPersistenceMngr() {
        
        if (sharedPersistenceMngr == null) {
            sharedPersistenceMngr = getPersistenceManager();
            System.out.println(String.format("[dao] create sharedPersistenceMngr (%s)", daoClassName));
        }
        return sharedPersistenceMngr;
    }
    
    protected PersistenceManager getPersistenceManager() {
        
        return PMF.get(daoClass).getPersistenceManager();
    }
    
    public void resetCache(T dao) {
        
        if (dao == null) return;
        CacheFactory.delete(CacheFactory.getDaoFindByIdKey(daoClassName, dao.getId()));
    }
    
    public void resetCacheAll(Collection<T> list) {
        
        List<String> cacheKeys = new ArrayList<String>();
        for (T dao : list) {
            cacheKeys.add(CacheFactory.getDaoFindByIdKey(daoClassName, dao.getId()));
        }
        CacheFactory.deleteAll(cacheKeys);
    }
    
    public T save(T dao) {
        
        return save(dao, getSharedPersistenceMngr());
    }
    
    protected T save(T dao, PersistenceManager pm) {
        
        if (dao == null) return null;
        dao = pm.detachCopy(pm.makePersistent(dao));
        System.out.println(String.format("[dao] %s.save(%d)", daoClassName, dao.getId()));
        if (dao.isCachable())
            resetCache(dao);
        return dao;
    }
    
    public Collection<T> saveAll(Collection<T> list) {
        
        if (list == null || list.isEmpty()) return null;
        long before = NnDateUtil.timestamp();
        System.out.println(String.format("[dao] %s.saveAll()", daoClassName));
        PersistenceManager pm = getPersistenceManager();
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
        }
        
        if (list.iterator().next().isCachable())
            resetCacheAll(list);
        System.out.println(String.format("[dao] saving %d objects costs %d miliseconds", list.size(), NnDateUtil.timestamp() - before));
        return list;
    }
    
    public void delete(T dao) {
        
        if (dao == null) { return; }
        System.out.println(String.format("[dao] %s.delete(%d)", daoClassName, dao.getId()));
        if (dao.isCachable())
            resetCache(dao);
        PersistenceManager pm = getPersistenceManager();
        try {
            pm.deletePersistent(dao);
        } finally {
            pm.close();
        }
    }
    
    public void deleteAll(Collection<T> list) {
        
        if (list == null || list.isEmpty()) return;
        long before = NnDateUtil.timestamp();
        System.out.println(String.format("[dao] %s.deleteAll()", daoClassName));
        if (list.iterator().next().isCachable())
            resetCacheAll(list);
        PersistenceManager pm = getPersistenceManager();
        Transaction tx = pm.currentTransaction();
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
        System.out.println(String.format("[dao] deleteAll() costs %d miliseconds", NnDateUtil.timestamp() - before));
    }
    
    /**
     * Get total number of objects w/o filter
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
        System.out.println(String.format("[dao] %s.list(%d, %d, \"%s\")", daoClassName, page, limit, sort));
        PersistenceManager pm = getPersistenceManager();
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
        return results;
    }
    
    public List<T> list(long page, long limit, String sort, String filter) {
        System.out.println(String.format("[dao] %s.list(%d, %d, \"%s\", \"%s\")", daoClassName, page, limit, sort, filter));
        PersistenceManager pm = getPersistenceManager();
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
        return results;
    }
    
    public List<T> findAllByIds(Collection<Long> ids) {
        
        return findAllByIds(ids, getSharedPersistenceMngr());
    }
    
    @SuppressWarnings("unchecked")
    public List<T> findAllByIds(Collection<Long> ids, PersistenceManager pm) {
        
        List<T> results = new ArrayList<T>();
        Query query = pm.newQuery(daoClass, ":p.contains(id)");
        results = (List<T>) pm.detachCopyAll((List<T>) query.execute(ids));
        query.closeAll();
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
        return findById(id, getSharedPersistenceMngr());
    }
    
    public T findById(long id) {
        
        return findById(id, getSharedPersistenceMngr());
    }
    
    @SuppressWarnings("unchecked")
    protected T findById(long id, PersistenceManager pm) {
        
        T dao = null;
        String cacheKey = CacheFactory.getDaoFindByIdKey(daoClassName, id);
        dao = (T) CacheFactory.get(cacheKey);
        if (dao != null) { // hit
            CounterFactory.increment("HIT " + cacheKey);
            return dao;
        }
        CounterFactory.increment("MISS " + cacheKey);
        try {
            dao = (T) pm.detachCopy((T) pm.getObjectById(daoClass, id));
        } catch (JDOObjectNotFoundException e) {
        }
        if (dao != null && dao.isCachable())
            CacheFactory.set(cacheKey, dao, CacheFactory.EXP_ONE_DAY);
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
    
    public List<T> sql(String queryStr) {
        
        return sql(queryStr, false);
    }
    
    public List<T> sql(String queryStr, boolean fine) {
        
        return sql(queryStr, getSharedPersistenceMngr(), fine);
    }
    
    protected List<T> sql(String queryStr, PersistenceManager pm) {
        
        return sql(queryStr, pm, false);
    }
    
    private List<T> sql(String queryStr, PersistenceManager pm, boolean fine) {
        
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
        Query query = pm.newQuery("javax.jdo.query.SQL", queryStr);
        query.setClass(daoClass);
        @SuppressWarnings("unchecked")
        List<T> results = (List<T>) query.execute();
        detached = (List<T>) pm.detachCopyAll(results);
        query.closeAll();
        if (!fine)
            System.out.println(String.format("[sql] %d items returned, costs %d milliseconds", detached.size(), NnDateUtil.timestamp() - before));
        return detached;
    }
    
    @Override
    protected void finalize() throws Throwable {
        
        NnLogUtil.logFinalize(getClass().getName());
    }
}
