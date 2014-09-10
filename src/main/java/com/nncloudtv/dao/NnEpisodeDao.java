package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnEpisode;
import com.nncloudtv.service.PlayerApiService;

public class NnEpisodeDao extends GenericDao<NnEpisode> {
    protected static final Logger log = Logger.getLogger(NnEpisodeDao.class.getName());
    
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
    
    public List<NnEpisode> findPlayerEpisode(long channelId, short sort, int start, int end) {
        List<NnEpisode> detached = new ArrayList<NnEpisode>();
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(NnEpisode.class);
            
            query.declareParameters("long channelIdParam, boolean isPublicParam");
            if (sort == NnChannel.SORT_SCHEDULED_FIRST) {
                
                query.setFilter("channelId == channelIdParam && (publishDate != null || scheduleDate != null)");
                query.setOrdering("scheduleDate desc, publishDate desc");
                
            } else {
                
                query.setFilter("channelId == channelIdParam && isPublic == isPublicParam");
                if (sort == NnChannel.SORT_POSITION_REVERSE) {
                    query.setOrdering("seq desc");
                } else {
                    query.setOrdering("seq asc");
                }
            }
            query.setRange(start, end);
            @SuppressWarnings("unchecked")
            List<NnEpisode> episodes = (List<NnEpisode>) query.execute(channelId, true);
            if (episodes.size() > 0) {
                detached = (List<NnEpisode>) pm.detachCopyAll(episodes);
            }
            query.closeAll();
        } finally {
            pm.close();
        }
        return detached;
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
            query.setRange(0, PlayerApiService.PAGING_ROWS);
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
