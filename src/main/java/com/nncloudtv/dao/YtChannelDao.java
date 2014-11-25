package com.nncloudtv.dao;

import java.util.List;
import java.util.logging.Logger;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.YtChannel;

public class YtChannelDao extends GenericDao<YtChannel> {
    
    protected static final Logger log = Logger.getLogger(YtProgramDao.class.getName());    
        
    public YtChannelDao() {
        super(YtChannel.class);
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
