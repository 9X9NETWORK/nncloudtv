package com.nncloudtv.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnEpisode;

public class NnEpisodeDao extends GenericDao<NnEpisode> {
    protected static final Logger log = Logger.getLogger(NnEpisodeDao.class.getName());
    
    public static final String V2_LINEAR_SORTING = "isPublic ASC, CASE WHEN isPublic = TRUE THEN publishDate ELSE scheduleDate END DESC";
    
    public NnEpisodeDao() {
        super(NnEpisode.class);
    }
    
    public List<NnEpisode> findByChannelId(long channelId) {
    
        List<NnEpisode> detached = new ArrayList<NnEpisode>();
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        
        try {
            Query query = pm.newQuery(NnEpisode.class);
            query.setFilter("channelId == channelIdParam");
            query.declareParameters("long channelIdParam");
            query.setOrdering("seq asc");
            @SuppressWarnings("unchecked")
            List<NnEpisode> episodes = (List<NnEpisode>)query.execute(channelId);
            if (episodes.size() > 0) {
                detached = (List<NnEpisode>) pm.detachCopyAll(episodes);
            }
            query.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public List<NnEpisode> listV2(long start, long limit, String sorting, String filter) {
        
        String query = "SELECT * FROM nnepisode "
                     + "        WHERE " + filter
                     + "     ORDER BY " + sorting
                     + "        LIMIT " + start + ", " + limit;
        
        return sql(query, true);
    }
    
    public List<NnEpisode> findPlayerEpisode(long channelId, short sort, long start, long end) {
        
        String filtering = "isPublic = TRUE AND channelId = " + channelId;
        String ordering = "SEQ ASC";
        String limit = "";
        
        if (sort == NnChannel.SORT_POSITION_REVERSE) {
            
            ordering = "SEQ DESC";
            
        } else if (sort == NnChannel.SORT_TIMED_LINEAR) {
            
            filtering = "scheduleDate IS NOT NULL && channelId = " + channelId;
            ordering = V2_LINEAR_SORTING;
        }
        
        if (start < 1000000000000L) {
            
            limit = " LIMIT " + start + ", " + (end - start);
            
        } else {
            
            SimpleDateFormat from = new SimpleDateFormat("yyyyMMdd 00:00:00");
            SimpleDateFormat to = new SimpleDateFormat("yyyyMMdd 23:59:59");
            
            filtering += " AND scheduleDate >= " + NnStringUtil.escapedQuote(from.format(new Date(start)));
            filtering += " AND scheduleDate < "  + NnStringUtil.escapedQuote(to.format(new Date(start)));
        }
        
        String query = "SELECT * FROM nnepisode WHERE " + filtering
                     + "     ORDER BY " + ordering
                     + "              " + limit;
        
        return sql(query);
    }
    
    public List<NnEpisode> findPlayerLatestEpisode(long channelId, short sort) {
        List<NnEpisode> detached = new ArrayList<NnEpisode>();
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(NnEpisode.class);
            query.setFilter("channelId == channelIdParam && isPublic == isPublicParam");
            query.declareParameters("long channelIdParam, boolean isPublicParam");
            query.setRange(0, 1);
            if (sort == NnChannel.SORT_POSITION_REVERSE)
            	query.setOrdering("seq desc");
            else 
                query.setOrdering("seq asc");
            @SuppressWarnings("unchecked")
            List<NnEpisode> episodes = (List<NnEpisode>)query.execute(channelId, true);
            if (episodes.size() > 0) {
                detached = (List<NnEpisode>) pm.detachCopyAll(episodes);
            }
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public List<NnEpisode> findAllBySysTag(long categoryId) {
        
        String query = "    select * from nnepisode where channelId in"
                     + "        (select channelId from systag_map where systagId = " + categoryId + ")"
                     + "    and isPublic = true"
                     + "    order by publishDate desc limit 1000";
        
        return sql(query);
    }
    
}
