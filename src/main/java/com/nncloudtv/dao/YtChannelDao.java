package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.YtChannel;

public class YtChannelDao extends GenericDao<YtChannel> {
    
    protected static final Logger log = Logger.getLogger(YtProgramDao.class.getName());    
        
    public YtChannelDao() {
        super(YtChannel.class);
    }    

    public YtChannel findById(long id) {
        PersistenceManager pm = PMF.getAnalytics().getPersistenceManager();        
        YtChannel channel = null;
        try {
            channel = pm.getObjectById(YtChannel.class, id);
            channel = pm.detachCopy(channel);
        } catch (JDOObjectNotFoundException e) {
        } finally {
            pm.close();            
        }
        return channel;        
    }    

    public List<YtChannel> findRandomTen(String lang) {
        List<YtChannel> detached = new ArrayList<YtChannel>();
        PersistenceManager pm = PMF.getAnalytics().getPersistenceManager();
        try {
            String sql = "select * from ytchannel " + 
                          "where lang = '" + lang + "'" +
            		       " and contentType = 3 " + 
            		     " order by rand() " +
                         " limit 10";
            log.info("Sql=" + sql);            
            Query q= pm.newQuery("javax.jdo.query.SQL", sql);
            q.setClass(YtChannel.class);
            @SuppressWarnings("unchecked")
			List<YtChannel> results = (List<YtChannel>) q.execute();
            detached = (List<YtChannel>)pm.detachCopyAll(results);
        } finally {
            pm.close();
        }
                
        return detached;        
    }
    
}
