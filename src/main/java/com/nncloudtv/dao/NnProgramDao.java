package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnProgram;

public class NnProgramDao extends GenericDao<NnProgram> {
    
    protected static final Logger log = Logger.getLogger(NnProgramDao.class.getName());    
        
    public NnProgramDao() {
        super(NnProgram.class);
    }
    
    public NnProgram findByChannelAndFileUrl(long channelId, String fileUrl) {
        NnProgram detached = null;
        PersistenceManager pm = PMF.getContent().getPersistenceManager();        
        try {
            Query query = pm.newQuery(NnProgram.class);
            query.setFilter("channelId == " + channelId + " & fileUrl == '" + fileUrl + "'");
            @SuppressWarnings("unchecked")
            List<NnProgram> results = (List<NnProgram>) query.execute(channelId, fileUrl);
            if (results.size() > 0) {
                detached = pm.detachCopy(results.get(0));
            }
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public NnProgram findByStorageId(String storageId) {
        NnProgram detached = null;
        PersistenceManager pm = PMF.getContent().getPersistenceManager();        
        try {
            Query query = pm.newQuery(NnProgram.class);
            query.setFilter("storageId == '" + storageId + "'");
            @SuppressWarnings("unchecked")
            List<NnProgram> results = (List<NnProgram>) query.execute();
            if (results.size() > 0) {
                detached = pm.detachCopy(results.get(0));
            }
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public List<NnProgram> findByYtVideoId(String videoId) {
        if (videoId == null) {return null;}
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        List<NnProgram> programs = new ArrayList<NnProgram>();
        try {
            String sql = 
                "select * from nnprogram " +
                 "where lower(fileUrl) like lower('%" + videoId + "%')";
            
            log.info("Sql=" + sql);
            Query q= pm.newQuery("javax.jdo.query.SQL", sql);
            
            q.setClass(NnProgram.class);
            @SuppressWarnings("unchecked")
            List<NnProgram> results = (List<NnProgram>) q.execute();                                
            programs = (List<NnProgram>) pm.detachCopyAll(results);
        } finally {
            pm.close();
        }
        return programs;                
    }
    
    //IMPORTANT: applies to 9x9 channel only. otherwise ordering could be wrong
    public List<NnProgram> findByChannelAndSeq(long channelId, String seq) {
        
        List<NnProgram> detached = new ArrayList<NnProgram>();
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        
        try {
            Query query = pm.newQuery(NnProgram.class);
            query.setFilter("channelId == channelIdParam && seq == seqParam");
            query.declareParameters("long channelIdParam, String seqParam");
            query.setOrdering("seq asc, subSeq asc");
            @SuppressWarnings("unchecked")
            List<NnProgram> results = (List<NnProgram>) query.execute(
                    channelId, seq);
            detached = (List<NnProgram>) pm.detachCopyAll(results);
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public List<NnProgram> findProgramByChannelByChannelAndSeq(long channelId, String seq) {
        List<NnProgram> detached = new ArrayList<NnProgram>();
        PersistenceManager pm = PMF.getContent().getPersistenceManager();        
        try {
            Query query = pm.newQuery(NnProgram.class);
            query.setFilter("channelId == " + channelId + " & seq == '" + seq + "'");
            log.info("ordering by seq, subSeq asc");
            
            @SuppressWarnings("unchecked")
            List<NnProgram> results = (List<NnProgram>) query.execute(channelId, seq);
            detached = (List<NnProgram>)pm.detachCopyAll(results);
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public NnProgram findByChannelAndStorageId(long channelId, String storageId) {
        NnProgram detached = null;
        PersistenceManager pm = PMF.getContent().getPersistenceManager();        
        try {
            Query query = pm.newQuery(NnProgram.class);
            query.setFilter("channelId == " + channelId + " && storageId == '" + storageId + "'");
            @SuppressWarnings("unchecked")
            List<NnProgram> results = (List<NnProgram>) query.execute();
            if (results.size() > 0) {
                detached = pm.detachCopy(results.get(0));
            }
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public NnProgram findFavorite(long channelId, String fileUrl) {
        NnProgram detached = null;
        PersistenceManager pm = PMF.getContent().getPersistenceManager();        
        try {
            Query query = pm.newQuery(NnProgram.class);
            query.setFilter("channelId == " + channelId + " && fileUrl == '" + fileUrl + "'");
            @SuppressWarnings("unchecked")
            List<NnProgram> results = (List<NnProgram>) query.execute();
            if (results.size() > 0) {
                detached = pm.detachCopy(results.get(0));
            }
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public List<NnProgram> findPlayerProgramsByChannels(List<Long> channelIds) {
        List<NnProgram> good = new ArrayList<NnProgram>();
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        try {
            Query q = pm.newQuery(NnProgram.class, ":p.contains(channelId)");
            q.setOrdering("channelId asc");
            @SuppressWarnings("unchecked")
            List<NnProgram> programs = ((List<NnProgram>) q.execute(channelIds));        
            good = (List<NnProgram>) pm.detachCopyAll(programs);
            for (NnProgram p : programs) {
                  if (p.isPublic() && p.getStatus() != NnProgram.STATUS_OK) {
                      good.add(p);
                  }            
            }
        } finally {
            pm.close();
        }
        return good;
    }
        
    public List<NnProgram> findPlayerProgramsByChannel(NnChannel channel) {
        
        String ordering = "seq ASC, subSeq ASC"; 
        if (channel.getContentType() == NnChannel.CONTENTTYPE_MAPLE_SOAP) {
            ordering = "seq ASC, subSeq ASC"; 
        } else if (channel.getContentType() == NnChannel.CONTENTTYPE_MAPLE_VARIETY) {
            ordering = "seq DESC, subSeq ASC";    
        } else if (channel.getContentType() == NnChannel.CONTENTTYPE_MIXED) {
            ordering = "seq ASC, subSeq ASC";
        } else if (channel.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_SPECIAL_SORTING) {
            ordering = "seq DESC, subSeq ASC";
        } else {
            ordering = "updateDate DESC";
        }
        log.info("ordering = " + ordering);
        String query = "SELECT * FROM nnprogram "
                     + "        WHERE channelId = " + channel.getId() 
                     + "          AND isPublic = true "
                     + "          AND status != " + NnProgram.STATUS_ERROR
                     + "     ORDER BY " + ordering;
        
        return sql(query);
    }
    
    public List<NnProgram> findByChannel(long channelId) {
        
        List<NnProgram> detached = new ArrayList<NnProgram>();
        PersistenceManager pm = getPersistenceManager();
        
        try {
            Query query = pm.newQuery(NnProgram.class);
            query.setFilter("channelId == channelIdParam");
            query.declareParameters("long channelIdParam");
            query.setOrdering("seq, subSeq asc");
            @SuppressWarnings("unchecked")
            List<NnProgram> programs = (List<NnProgram>) query.execute(channelId);
            detached = (List<NnProgram>) pm.detachCopyAll(programs);
        } finally {
            pm.close();
        }
        
        return detached;
    }
    
    public List<NnProgram> findPlayerNnProgramsByChannel(long channelId) {
        String query = "SELECT * FROM nnprogram a1 "
                     + "   INNER JOIN ( " 
                     + "               SELECT p.id "
                     + "                 FROM nnprogram p, nnepisode e, nnchannel c "
                     + "                WHERE c.id = " + channelId
                     + "                  AND p.channelId = c.id "
                     + "                  AND e.id = p.episodeId "
                     + "                  AND p.status != " + NnProgram.STATUS_ERROR
                     + "             ORDER BY e.seq, p.seq, p.subSeq "
                     + "              ) a2 "
                     + "           ON a1.id = a2.id";
        
        return sql(query);
    }
    
    //based on one program id to find all sub-episodes belong to the same episode
    //use scenario: such as "reference" lookup
    public List<NnProgram> findProgramsByEpisode(long episodeId) {
        
        String query = "SELECT * FROM nnprogram "
                     + "        WHERE episodeId = " + episodeId
                     + "     ORDER BY subSeq"; // seq is not maintained anymore
        
        return sql(query);
    }
    
    public List<NnProgram> findAllByEpisodeId(long episodeId) {
        
        List<NnProgram> detached = new ArrayList<NnProgram>();
        PersistenceManager pm = getPersistenceManager();
        
        try {
            Query query = pm.newQuery(NnProgram.class);
            query.setFilter("episodeId == episodeIdParam");
            query.declareParameters("long episodeIdParam");
            query.setOrdering("seq, subSeq asc");
            @SuppressWarnings("unchecked")
            List<NnProgram> programs = (List<NnProgram>) query.execute(episodeId);
            detached = (List<NnProgram>) pm.detachCopyAll(programs);
        } finally {
            pm.close();
        }
        
        return detached;
    }
}
