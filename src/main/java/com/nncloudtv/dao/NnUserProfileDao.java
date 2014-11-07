package com.nncloudtv.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.commons.lang.StringEscapeUtils;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserProfile;

public class NnUserProfileDao extends GenericDao<NnUserProfile> {

    protected static final Logger log = Logger.getLogger(NnUserProfileDao.class.getName());
    
    public NnUserProfileDao() {
        super(NnUserProfile.class);
    }
    
    public List<NnUserProfile> findByUserId(long userId, short shard) {
        
        String query = "SELECT * FROM nnuser_profile WHERE userId = " + userId;
        
        return sql(query, NnUserDao.getPersistenceManager(shard, null));
    }
    
    public NnUserProfile findByUser(NnUser user) {
        if (user == null) return null;
        NnUserProfile detached = null;
        PersistenceManager pm = NnUserDao.getPersistenceManager(user.getShard(), null);
        try {
            Query query = pm.newQuery(NnUserProfile.class);
            query.setFilter("userId == userIdParam && msoId == msoIdParam");
            query.declareParameters("long userIdParam, long msoIdParam");
            @SuppressWarnings("unchecked")
            List<NnUserProfile> results = (List<NnUserProfile>) query.execute(user.getId(), user.getMsoId());
            if (results.size() > 0)
                detached = (NnUserProfile) pm.detachCopy(results.get(0));
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public NnUserProfile save(NnUser user, NnUserProfile profile) {
        if (profile == null) {return null;}
        PersistenceManager pm = NnUserDao.getPersistenceManager(user.getShard(), user.getToken());
        try {
            pm.makePersistent(profile);
            profile = pm.detachCopy(profile);
        } finally {
            pm.close();
        }
        return profile;
    }
    
    public Set<NnUserProfile> search(String keyword, int start, int limit, short shard) {
        
        Set<NnUserProfile> results = new HashSet<NnUserProfile>();
        
        keyword = StringEscapeUtils.escapeSql(keyword);
        String query = "SELECT * FROM nnuser_profile "
                     + "        WHERE LOWER(name) LIKE LOWER(" + NnStringUtil.escapedQuote("%" + keyword + "%") + ")"
                     + "     ORDER BY updateDate DESC"
                     + "        LIMIT " + start + ", " + limit;
        
        results.addAll(sql(query, NnUserDao.getPersistenceManager(shard, null)));
        
        return results;
    }
    
}
