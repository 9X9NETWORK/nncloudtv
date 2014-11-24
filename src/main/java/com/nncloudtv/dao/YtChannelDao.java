package com.nncloudtv.dao;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.YtChannel;

public class YtChannelDao extends GenericDao<YtChannel> {
    
    protected static final Logger log = Logger.getLogger(YtProgramDao.class.getName());    
        
    public YtChannelDao() {
        super(YtChannel.class);
    }    

    public YtChannel findById(long id) {
        PersistenceManager pm = PMF.getRecommend().getPersistenceManager();        
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
    
    public List<YtChannel> findRandomTen(String sphere) {
        
        String query = "SELECT * FROM ytchannel " 
                     + "        WHERE lang = " + NnStringUtil.escapedQuote(sphere) 
                     + "          AND contentType = 3 " 
                     + "     ORDER BY rand() "
                     + "        LIMIT 10 ";
        
        return sql(query);
    }
    
}
