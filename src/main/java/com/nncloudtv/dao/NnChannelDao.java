package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.PMF;
import com.nncloudtv.lib.SearchLib;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.NnChannel;

public class NnChannelDao extends GenericDao<NnChannel> {
    
    protected static final Logger log = Logger.getLogger(NnChannelDao.class.getName());
    
    public NnChannelDao() {
        super(NnChannel.class);
    }    
        
    public List<NnChannel> findByContentType(short type) {
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        List<NnChannel> detached = new ArrayList<NnChannel>(); 
        try {
            Query q = pm.newQuery(NnChannel.class);
            q.setFilter("contentType == contentTypeParam");
            q.declareParameters("short contentTypeParam");
            @SuppressWarnings("unchecked")
            List<NnChannel> channels = (List<NnChannel>) q.execute(type);
            detached = (List<NnChannel>)pm.detachCopyAll(channels);
        } finally {
            pm.close();
        }
        return detached;
    }    
    
    public NnChannel save(NnChannel channel) {
        if (channel == null) {return null;}
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        try {
            pm.makePersistent(channel);            
            channel = pm.detachCopy(channel);
        } finally {
            pm.close();
        }
        return channel;
    }
    
    //find good channels, for all needs to be extended
    public List<NnChannel> findChannelsByTag(String name) {
        
        String sql = "select * from nnchannel where id in ( " + 
                "select distinct map.channelId " + 
                   "from ytprogram yt, tag_map map " + 
                  "where yt.channelId = map.channelId " +
                    "and map.tagId = (select id from tag where name= '" + name + "')) " +
                    "order by rand() limit 9;";
        
        return sql(sql);
    }
    
    public static long searchSize(String queryStr, boolean all) {
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        long size = 0;
        try {
            String sql = "select count(*) from nnchannel " + 
                          "where (lower(name) like lower(\"%" + queryStr + "%\")" +
                              "|| lower(intro) like lower(\"%" + queryStr + "%\"))";
            if (!all) {
              sql += " and (status = " + NnChannel.STATUS_SUCCESS + " or status = " + NnChannel.STATUS_WAIT_FOR_APPROVAL + ")";
              sql += " and isPublic = true";
            }
            Query query = pm.newQuery("javax.jdo.query.SQL", sql);
            @SuppressWarnings("rawtypes")
            List results = (List) query.execute();
            size = (Long)results.iterator().next();
        } finally {
            pm.close();
        }
        return size;
    }

    @SuppressWarnings("unchecked")
    public static List<NnChannel> searchTemp(String queryStr, boolean all, int start, int limit) {
        log.info("start:" + start + ";end:" + limit);
        if (start == 0) start = 0;            
        if (limit == 0) limit = 9;
        
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        List<NnChannel> detached = new ArrayList<NnChannel>();
        try {
            String sql = "select * from nnchannel " + 
                          "where (lower(name) like lower(" + NnStringUtil.escapedQuote("%" + queryStr + "%") + ")" +
                              "|| lower(intro) like lower(" + NnStringUtil.escapedQuote("%" + queryStr + "%") + "))";
            sql += " limit " + start + ", " + limit;
            log.info("Sql=" + sql);
            
            Query q= pm.newQuery("javax.jdo.query.SQL", sql);
            q.setClass(NnChannel.class);
            List<NnChannel> results = (List<NnChannel>) q.execute();
            detached = (List<NnChannel>)pm.detachCopyAll(results);
        } finally {
            pm.close();
        }

        return detached;     
    	
    }
    
    //replaced with Apache Lucene
    @SuppressWarnings("unchecked")
    public static List<NnChannel> search(String keyword, String content, String extra, boolean all, int start, int limit) {
        log.info("start:" + start + ";end:" + limit);
        if (start == 0) start = 0;            
        if (limit == 0) limit = 9;
        
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        List<NnChannel> detached = new ArrayList<NnChannel>();
        try {
            String sql = "select * from nnchannel where ("
                       + "lower(name) like lower(" + NnStringUtil.escapedQuote("%" + keyword + "%") + ")";
            if (all || content == null || !content.equals(SearchLib.STORE_ONLY)) { // PCS
                sql += " || lower(intro) like lower(" + NnStringUtil.escapedQuote("%" + keyword + "%") + ")";
            }
            sql += ")";
            if (!all) {
                sql += " and (status = " + NnChannel.STATUS_SUCCESS + " or status = " + NnChannel.STATUS_WAIT_FOR_APPROVAL + ")";
                if (content != null) {
                    if (content.equals(SearchLib.YOUTUBE_ONLY)) {
                        sql += " and contentType = "
                                + NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL;
                    } else if (content.equals(SearchLib.STORE_ONLY)) {
                        // store only
                        sql += " and (status = " + NnChannel.STATUS_SUCCESS + ")";
                    }
                }
                sql += " and isPublic = true";
            }
            if (extra != null) {
                sql += " and (" + extra + ")";
            }
            sql += " limit " + start + ", " + limit;
            log.info("Sql=" + sql);
            
            Query q= pm.newQuery("javax.jdo.query.SQL", sql);
            q.setClass(NnChannel.class);
            List<NnChannel> results = (List<NnChannel>) q.execute();
            detached = (List<NnChannel>)pm.detachCopyAll(results);
        } finally {
            pm.close();
        }
        return detached;     
    }
        
    public List<NnChannel> findAllByStatus(short status) {
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        List<NnChannel> detached = new ArrayList<NnChannel>(); 
        try {
            Query q = pm.newQuery(NnChannel.class);
            q.setFilter("status == statusParam");
            q.declareParameters("short statusParam");
            q.setOrdering("createDate asc");
            @SuppressWarnings("unchecked")
            List<NnChannel> channels = (List<NnChannel>) q.execute(status);
            detached = (List<NnChannel>)pm.detachCopyAll(channels);
        } finally {
            pm.close();
        }
        return detached;
    }    

    //select id from nnchannel where poolType > 10 order by rand() limit 10;
    public List<NnChannel> findSpecial(short type, String lang, int limit) {
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        List<NnChannel> detached = new ArrayList<NnChannel>(); 
        try {
            String sql = "select * " +
                          " from nnchannel " + 
            		     " where poolType >= " + type + 
                           " and (sphere = '" + lang + " 'or sphere = 'other')" + 
                           " order by rand()";
            if (limit > 0) 
                sql += " limit " + limit;
            log.info("sql:" + sql);
            Query q= pm.newQuery("javax.jdo.query.SQL", sql);
            q.setClass(NnChannel.class);
            @SuppressWarnings("unchecked")
            List<NnChannel> channels = (List<NnChannel>) q.execute(type);
            detached = (List<NnChannel>)pm.detachCopyAll(channels);
        } finally {
            pm.close();
        }
        return detached;
    }
    
    @SuppressWarnings("unchecked")
    public List<NnChannel> findByUser(String userIdStr, int limit, boolean isAll) {
        PersistenceManager pm = getPersistenceManager();
        List<NnChannel> channels = new ArrayList<NnChannel>(); 
        try {
            if (isAll) {
                Query q = pm.newQuery(NnChannel.class);
                q.setOrdering("seq asc, contentType asc");
                q.setFilter("userIdStr == userIdStrParam");
                q.declareParameters("String userIdStrParam");
                if (limit != 0)
                    q.setRange(0, limit);            
                channels = (List<NnChannel>) q.execute(userIdStr);
            } else {                
                String sql = 
                    "select * from nnchannel " +
                     "where  userIdStr = '" + userIdStr + "' " +
                       " and isPublic=true " +
                       " and (status=0 or status=3) " +
                       " order by seq, contentType "; 
                if (limit != 0)
                    sql += " limit " + limit;
                log.info("Sql=" + sql);
                Query q= pm.newQuery("javax.jdo.query.SQL", sql);
                q.setClass(NnChannel.class);
                channels = (List<NnChannel>) q.execute();                                
            }
            
            channels = (List<NnChannel>)pm.detachCopyAll(channels);
        } finally {
            pm.close();
        }
        return channels;
    }    
    
    public NnChannel findBySourceUrl(String url) {
        if (url == null) {return null;}
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        NnChannel channel = null;
        try {
            String sql = 
                "select * from nnchannel " +
                 "where lower(sourceUrl) = lower(?)";

            log.info("Sql=" + sql);
            Query q= pm.newQuery("javax.jdo.query.SQL", sql);
            
            q.setClass(NnChannel.class);
            @SuppressWarnings("unchecked")
            List<NnChannel> channels = (List<NnChannel>) q.execute(url);                                
            if (channels.size() > 0) {
                channel = pm.detachCopy(channels.get(0));
            }
            
        } finally {
            pm.close();
        }
        return channel;                
    }        

    public NnChannel findFavorite(String userIdStr) {
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        NnChannel channel = null;
        try {
            Query q = pm.newQuery(NnChannel.class);
            q.setFilter("userIdStr == userIdStrParam && contentType == contentTypeParam");
            q.declareParameters("String userIdStrParam, short contentTypeParam");
            q.setOrdering("cntSubscribe");
            @SuppressWarnings("unchecked")
            List<NnChannel> channels = (List<NnChannel>) q.execute(userIdStr, NnChannel.CONTENTTYPE_FAVORITE);
            if (channels.size() > 0) {
                channel = pm.detachCopy(channels.get(0));
            }
        } finally {
            pm.close();
        }
        return channel;                
    }        
    
    public List<NnChannel> findPersonalHistory(long userId, long msoId) {
        
        String query = "select * from nncloudtv_content.nnchannel c "
                     + "where c.id in "
                     + "        (select channelId from nncloudtv_nnuser1.nnuser_watched "
                     + "         where userId = " + userId
                     + "         and msoId = " + msoId
                     + "         and channelId not in "
                     + "                (select channelId from nncloudtv_nnuser1.nnuser_subscribe "
                     + "                 where userId = " + userId + " and msoId = " + msoId + ")) "
                     + "order by updateDate desc";
        
        return sql(query);
    }
    
    public List<NnChannel> getCategoryChannels(long categoryId, List<String> spheres) {
        
        String filter = "";
        if (spheres != null && spheres.size() > 0) {
            filter = " and ( c.sphere = " + NnStringUtil.escapedQuote(LangTable.OTHER);
            for (String sphere : spheres) {
                filter = filter + " or c.sphere = " + NnStringUtil.escapedQuote(sphere);
            }
            filter = filter + " )";
        }
        
        String query = " select * from nnchannel a1 "
                     + " inner join ("
                     + "         select distinct c.id"
                     + "         from systag_display d, systag_map m, nnchannel c"
                     + "         where d.systagId = " + categoryId
                     + "         and d.systagId = m.systagId"
                     + "         and c.id = m.channelId"
                     + "         and c.isPublic = true"
                     + "         and c.contentType != " + NnChannel.CONTENTTYPE_FAVORITE
                     + "         and c.status = " + NnChannel.STATUS_SUCCESS + filter
                     + "         order by c.updateDate desc) a2 "
                     + " on a1.id = a2.id";
                    
        return sql(query);
    }
}
