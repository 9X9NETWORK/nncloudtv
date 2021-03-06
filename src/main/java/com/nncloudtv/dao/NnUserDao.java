package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.nncloudtv.lib.AuthLib;
import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.NnUser;

public class NnUserDao extends ShardedDao<NnUser> {

    protected static final Logger log = Logger.getLogger(NnUserDao.class.getName());
    
    public NnUserDao() {
        super(NnUser.class);
    }
    
    //generic = true is for regular search
    //generic = false means for experimental ios feature
    @SuppressWarnings("unchecked")
    public List<NnUser> search(String email, String name, String generic, long msoId) {
        List<NnUser> detached = new ArrayList<NnUser>();        
        PersistenceManager pm = PMF.getNnUser1().getPersistenceManager(); // FIXME
        try {
            String sql = "";
            if (generic != null) {
            	sql = "select * from nnuser a1 " + 
            			" inner join (" + 
            			  "select distinct u.id " +
            			   " from nnuser_profile p, nnuser u " +
            			  " where p.userId = u.id " +
            			    " and (lower(p.name) like lower(\"%" + generic + "%\") " + 
            			          " or lower(p.intro) like lower(\"%" + generic + "%\")) " + 
            			  ") a2 on a1.id=a2.id";
            	/*
                sql = "select * from nnuser " + 
                       "where id in (select userId from nnuser_profile " +
                                    " where msoId = " + msoId +  
                                    "   and (lower(name) like lower(\"%" + generic + "%\") " +  
                                    "   or lower(intro) like lower(\"%" + generic + "%\")))";
                                    */                              
            } else {
               sql = "select * from nnuser " + "where ";                      
               if (email != null) {
                   sql += " email = '" + email + "'";
               }
               /*
               } else if (name != null) { 
                   sql += " lower(name) like lower('%" + name + "%')";
               */
            }
            log.info("Sql=" + sql);        
            pm = PMF.getNnUser1().getPersistenceManager();
            Query q= pm.newQuery("javax.jdo.query.SQL", sql);
            q.setClass(NnUser.class);
            List<NnUser> results = (List<NnUser>) q.execute();
            detached = (List<NnUser>)pm.detachCopyAll(results);
            //user2
            pm = PMF.getNnUser2().getPersistenceManager();
            q= pm.newQuery("javax.jdo.query.SQL", sql);
            results = (List<NnUser>) q.execute();
            detached.addAll((List<NnUser>)pm.detachCopyAll(results));
        } catch (JDOObjectNotFoundException e) {
        } finally {
            pm.close();
        }
        return detached;
    }
    
    // looks both two shard DB
    public NnUser findById(long id) {
        
        NnUser user = null;
        user = findById(id, NnUser.SHARD_DEFAULT);
        if (user == null) {
            return findById(id, NnUser.SHARD_CHINESE);
        } else {
            return user;
        }
    }
    
    public NnUser findById(long id, short shard) {
        
        return findById(id, getPersistenceManagerFactory(shard, null));
    }
    
    public static PersistenceManagerFactory getPersistenceManagerFactory(short shard, String token) {
        if (shard > 0) {
            if (shard == NnUser.SHARD_DEFAULT) {
                return PMF.getNnUser1();
            } else {
                return PMF.getNnUser2();
            }
        }
        if (token != null) {
            if (token.contains("2-")) {
                return PMF.getNnUser2();
            } else {
                return PMF.getNnUser1();
            }
        }        
        return PMF.getNnUser1();
    }
    
    public static PersistenceManagerFactory getPersistenceManagerFactory(NnUser user) {
        
        return getPersistenceManagerFactory(user.getShard(), user.getToken());
    }
    
    //use either shard or token to determine partition, default shard 1 if nothing
    public static PersistenceManager getPersistenceManager(short shard, String token) {
        
        return getPersistenceManagerFactory(shard, token).getPersistenceManager();
    }
    
    public static PersistenceManager getPersistenceManager(NnUser user) {
        
        return getPersistenceManagerFactory(user).getPersistenceManager();
    }
    
    public NnUser save(NnUser user) {
        
        return save(user, getPersistenceManagerFactory(user));
    }
    
    public NnUser findAuthenticatedUser(String email, String password, short shard) {
        NnUser user = null;
        PersistenceManager pm = getPersistenceManager(shard, null);
        try {
            Query query = pm.newQuery(NnUser.class);
            query.setFilter("email == emailParam");
            query.declareParameters("String emailParam");
            @SuppressWarnings("unchecked")
            List<NnUser> results = (List<NnUser>) query.execute(email);
            if (results.size() > 0) {
                user = results.get(0);
                byte[] proposedDigest = AuthLib.passwordDigest(password, user.getSalt());
                if (!Arrays.equals(user.getCryptedPassword(), proposedDigest)) {
                    user = null;
                }
            }
            user = pm.detachCopy(user);
        } finally {
            pm.close();
        }
        return user;
    }
    // TODO speed me up with cache
    public NnUser findByToken(String token) {
        
        NnUser user = null;
        log.info("token = " + token);
        PersistenceManager pm = getPersistenceManager(NnUser.SHARD_UNKNWON, token);
        try {
            Query query = pm.newQuery(NnUser.class);
            query.setFilter("token == tokenParam");
            query.declareParameters("String tokenParam");
            @SuppressWarnings("unchecked")
            List<NnUser> results = (List<NnUser>) query.execute(token);
            if (results.size() > 0) {
                user = results.get(0);
            }
            user = pm.detachCopy(user);
        } finally {
            pm.close();
        }
        
        return user;
    }
    
    public NnUser findByEmail(String email, short shard) {
        NnUser user = null;
        PersistenceManager pm = getPersistenceManager(shard, null);
        try {
            Query query = pm.newQuery(NnUser.class);
            query.setFilter("email == emailParam");
            query.declareParameters("String emailParam");
            @SuppressWarnings("unchecked")
            List<NnUser> results = (List<NnUser>) query.execute(email);
            if (results.size() > 0) {
                user = results.get(0);
            }
            user = pm.detachCopy(user);
        } finally {
            pm.close();
        }
        return user;
    }    
    
    public NnUser findByProfileUrl(String profileUrl) {
        if (profileUrl == null) return null;
        profileUrl = profileUrl.toLowerCase();
        NnUser user = null;
        PersistenceManager pm = PMF.getNnUser1().getPersistenceManager();
        try {
            for (int i=0;i<2;i++) {
                String sql = "select * from nnuser n " +
                		     " where exists (" +
                		         " select userId " +
                                   "from nnuser_profile p " +
                                 " where p.userId = n.id " +
                                   " and lower(profileUrl) = '" + profileUrl + "')";
                log.info("sql:" + sql);
                Query query = pm.newQuery("javax.jdo.query.SQL", sql);
                query.setClass(NnUser.class);
                @SuppressWarnings("unchecked")
                List<NnUser> results = (List<NnUser>) query.execute();
                if (results.size() > 0) {
                    user = results.get(0);
                    i = 2;
                } else {
                    pm = PMF.getNnUser2().getPersistenceManager();
                }
            }
            user = pm.detachCopy(user);
        } finally {
            pm.close();
        }
        return user;
    }
    
    public NnUser findByFbId(String fbId) {
        NnUser user = null;
        PersistenceManager pm = PMF.getNnUser1().getPersistenceManager();
        try {
            for (int i=0;i<2;i++) {
                Query query = pm.newQuery(NnUser.class);
                query.setFilter("fbId == fbIdParam");
                query.declareParameters("String fbIdParam");
                @SuppressWarnings("unchecked")
                List<NnUser> results = (List<NnUser>) query.execute(fbId);
                if (results.size() > 0) {
                    user = results.get(0);
                    i = 2;
                } else {
                    pm = PMF.getNnUser2().getPersistenceManager();
                }
            }
            user = pm.detachCopy(user);
        } finally {
            pm.close();
        }
        return user;
    }
    
    //TODO merge one and two
    public List<NnUser> findFeatured(long msoId) {
        PersistenceManager pm = PMF.getNnUser1().getPersistenceManager(); 
        List<NnUser> detached = new ArrayList<NnUser>(); 
        try {
            String sql = "select * " +
                          " from nnuser n " + 
                         " where exists " +
                           " (select userId from nnuser_profile p " +
                               " where p.userId = n.id " + 
                                 " and p.msoId = " + msoId +
                                 " and p.featured = true " +
                               " order by rand())" + 
                                       " limit 9";
            log.info("sql:" + sql);
            Query q= pm.newQuery("javax.jdo.query.SQL", sql);
            q.setClass(NnUser.class);
            @SuppressWarnings("unchecked")
            List<NnUser> users = (List<NnUser>) q.execute();
            detached = (List<NnUser>)pm.detachCopyAll(users);
        } finally {
            pm.close();
        }
        return detached;
    }        
}
