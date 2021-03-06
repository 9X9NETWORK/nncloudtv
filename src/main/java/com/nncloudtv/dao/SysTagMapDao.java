package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagMap;

public class SysTagMapDao extends GenericDao<SysTagMap> {
    
    protected static final Logger log = Logger.getLogger(SysTagMapDao.class.getName());
    
    public SysTagMapDao() {
        super(SysTagMap.class);
    }
    
    public SysTagMap findBySysTagIdAndChannelId(long sysTagId, long channelId) {
        
        List<SysTagMap> detached = sql("select * from systag_map where sysTagId = " + sysTagId
                                     + " and channelId = " + channelId);
        
        return detached.size() > 0 ? detached.get(0) : null;
    }
    
    // see SysTagDao.findCategoriesByChannelId
    public List<SysTagMap> findCategoryMaps(long channelId, long msoId) {
        
        String query = " select * from systag_map a1"
                     + " inner join (select m.id from systag s, systag_map m"
                     + "             where s.type = "      + SysTag.TYPE_CATEGORY
                     + "               and s.msoId = "     + msoId
                     + "               and m.channelId = " + channelId
                     + "               and s.id = m.systagId) a2 on a1.id = a2.id";
        
        return sql(query);
    }
    
    public List<SysTagMap> findBySysTagId(long sysTagId) {
        
        return sql("select * from systag_map where sysTagId = " + sysTagId + " order by seq asc");
    }
    
    @SuppressWarnings("unchecked")
    public List<SysTagMap> findByChannelIds(List<Long> channelIds) {
        
        List<SysTagMap> detached = new ArrayList<SysTagMap>();
        PersistenceManager pm = getPersistenceManager();
        
        try {
            Query q = pm.newQuery(SysTagMap.class, ":p.contains(channelId)");
            List<SysTagMap> results = ((List<SysTagMap>) q.execute(channelIds));
            if (results != null && results.size() > 0) {
                detached = (List<SysTagMap>) pm.detachCopyAll(results);
            }
        } finally {
            pm.close();
        }
        
        return detached;
    }

}
