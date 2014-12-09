package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.YtProgram;

public class YtProgramDao extends GenericDao<YtProgram> {
    
    protected static final Logger log = Logger.getLogger(YtProgramDao.class.getName());
    
    public YtProgramDao() {
        super(YtProgram.class);
    }
    
    public List<YtProgram> findOneLatestByChannelStr(String channelIdStr) {
        String query = "SELECT * FROM ytprogram a "
                     + "   INNER JOIN (" 
                     + "               SELECT channelId, max(updateDate) max_date "
                     + "                 FROM ytprogram "
                     + "                WHERE channelId IN (" + channelIdStr + ") "
                     + "             GROUP BY channelId "
                     + "              ) b "
                     + "           ON a.channelId = b.channelId AND a.updateDate = b.max_date";
        
        return sql(query);
    }
    
    public YtProgram findLatestByChannel(long id) {
        String query = "SELECT * FROM ytprogram a "
                     + "   INNER JOIN (" 
                     + "               SELECT channelId, max(updateDate) max_date "
                     + "                 FROM ytprogram "
                     + "                WHERE channelId = " + id
                     + "             GROUP BY channelId "
                     + "              ) b "
                     + "           ON a.channelId = b.channelId AND a.updateDate = b.max_date";
        
        List<YtProgram> results = sql(query);
        if (results.size() > 0)
            return results.get(0);
        else
            return null;
    }
    
    public List<YtProgram> findByChannels(List<NnChannel> channels) {
        List<YtProgram> detached = new ArrayList<YtProgram>();
        String ids = "";
        for (NnChannel c : channels) {
            ids += "," + String.valueOf(c.getId());
        }
        if (ids.length() == 0) return detached;
        if (ids.length() > 0) ids = ids.replaceFirst(",", "");
        log.info("find in these channels:" + ids);
        String query = "SELECT * FROM ytprogram "
                     + "        WHERE channelId in (" + ids + ") " 
                     + "     ORDER BY updateDate DESC "
                     + "        LIMIT 50";
        
        return sql(query);
    }
    
    public YtProgram findByVideo(String video) { 
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        YtProgram detached = null; 
        try {
            Query q = pm.newQuery(YtProgram.class);
            q.setFilter("ytVideoId == videoParam");
            q.declareParameters("String videoParam");
            @SuppressWarnings("unchecked")
            List<YtProgram> programs = (List<YtProgram>) q.execute(video);
            if (programs.size() > 0) {
                detached = pm.detachCopy(programs.get(0));
            }            
        } finally {
            pm.close();
        }
        return detached;
    }
    
}
