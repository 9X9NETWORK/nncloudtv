package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.commons.lang.StringUtils;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.PMF;
import com.nncloudtv.lib.SearchLib;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.SysTag;

public class NnChannelDao extends GenericDao<NnChannel> {
    
    protected static final Logger log = Logger.getLogger(NnChannelDao.class.getName());
    
    public NnChannelDao() {
        super(NnChannel.class);
    }    
    
    public long findPlayerChannelsCountBySysTag(long id, String lang, long msoId) {
        
        return findBySysTag(id, lang, false, 0, 0, SysTag.SORT_UNKNWON, msoId, true).size();
    }
    
    //player channels means status=true and isPublic=true
    public List<NnChannel> findBySysTag(long systagId, String lang, boolean limitRows, int start, int count, short sort, long msoId, boolean isPlayer) {
        
        String orderStr = " order by m.alwaysOnTop desc,"
                        + "          case m.alwaysOnTop when true then m.seq else (case c.sphere when '" + lang + "' then 1 else 2 end) end,"
                        + "          c.updateDate desc ";
        if (sort == SysTag.SORT_SEQ) {
            
            orderStr = " order by m.seq ";
        }
        if (limitRows){
            
            orderStr = " order by rand() limit 9 ";
        }
        if (start >= 0 && count > 0) {
            //start = start - 1;
            orderStr += String.format(" limit %d, %d", start, count);
        }
        String playerStr =    (!isPlayer) ? "" : String.format(" and c.isPublic = true and c.status = %d ", NnChannel.STATUS_SUCCESS);
        String langStr   = (lang == null) ? "" : String.format(" and (c.sphere = %s or c.sphere = 'other') ", NnStringUtil.escapedQuote(lang));
        String blackList =   (msoId == 0) ? "" : String.format(" and c.id not in (select channelId from store_listing where msoId = %d) ", msoId);
        String query = "select * from nnchannel a1"
                     + "   inner join (select distinct c.id "
                     + "                 from systag_display d, systag_map m, nnchannel c "
                     + "                where d.systagId = " + systagId 
                     + "                  and d.systagId = m.systagId "
                     + "                  and c.id = m.channelId "
                     + "                  and c.contentType != " + NnChannel.CONTENTTYPE_FAVORITE
                     + "                  " + playerStr
                     + "                  " + blackList
                     + "                  " + langStr
                     + "                  " + orderStr
                     + "              ) a2 on a1.id = a2.id";
        
        return sql(query);
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
    public List<NnChannel> search(String keyword, String content, String extra, boolean all, int start, int limit) {
        
        //log.info("start:" + start + ";end:" + limit);
        if (start == 0) start = 0;
        if (limit == 0) limit = 9;
        
        String query = "SELECT * FROM nnchannel "
                     +"         WHERE (LOWER(name) LIKE LOWER(" + NnStringUtil.escapedQuote("%" + keyword + "%") + ")";
        
        if (all || content == null || !content.equals(SearchLib.STORE_ONLY)) { // PCS
            
            query += " || LOWER(intro) LIKE LOWER(" + NnStringUtil.escapedQuote("%" + keyword + "%") + ")";
        }
        query += ")";
        
        if (!all) {
            
            query += " AND (status = " + NnChannel.STATUS_SUCCESS + " OR status = " + NnChannel.STATUS_WAIT_FOR_APPROVAL + ")";
            
            if (content != null) {
                
                if (content.equals(SearchLib.YOUTUBE_ONLY)) {
                    
                    query += " AND contentType = " + NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL;
                    
                } else if (content.equals(SearchLib.STORE_ONLY)) {
                 
                    // store only
                    query += " AND (status = " + NnChannel.STATUS_SUCCESS + ")";
                }
            }
            
            query += " AND isPublic = TRUE";
        }
        
        if (extra != null) {
            query += " AND (" + extra + ")";
        }
        
        query += " LIMIT " + start + ", " + limit;
        
        return sql(query);
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
        
        String query = "SELECT * FROM nnchannel " 
                     + "        WHERE poolType >= " + type 
                     + "          AND (sphere = '" + lang + "' OR sphere = 'other') "
                     + "     ORDER BY rand() ";
        
        if (limit > 0) 
            query += " limit " + limit;
        
        return sql(query);
    }
    
    @SuppressWarnings("unchecked")
    public List<NnChannel> findByUser(String userIdStr, int limit, boolean isAll) {
        
        if (isAll) {
            
            PersistenceManager pm = getPersistenceManager();
            List<NnChannel> channels = new ArrayList<NnChannel>();
            
            try {
                
                Query query = pm.newQuery(NnChannel.class);
                query.setOrdering("seq asc, contentType asc");
                query.setFilter("userIdStr == userIdStrParam");
                query.declareParameters("String userIdStrParam");
                if (limit != 0)
                    query.setRange(0, limit);
                channels = (List<NnChannel>) query.execute(userIdStr);
                channels = (List<NnChannel>)pm.detachCopyAll(channels);
                query.closeAll();
                
            } finally {
                
                pm.close();
            }
            
            return channels;
            
        } else {
            
            String listStr = "";
            if (userIdStr != null) {
                
                List<String> list = new ArrayList<String>();
                String[] split = userIdStr.split(",");
                for (String str : split) {
                    
                    list.add(NnStringUtil.escapedQuote(str));
                }
                
                listStr = StringUtils.join(list, ",");
            }
            
            String query = "SELECT * FROM nnchannel "
                         + "        WHERE userIdStr in (" + listStr + ")"
                         + "          AND isPublic = true "
                         + "          AND status in (0, 3) "
                         + "     ORDER BY seq, contentType ";
            
            if (limit > 0) {
                
                query += " LIMIT " + limit;
            }
            
            return sql(query);
        }
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
        
        String query = "SELECT * FROM nncloudtv_content.nnchannel c "
                     + "        WHERE c.id IN ("
                     + "               SELECT channelId FROM nncloudtv_nnuser1.nnuser_watched "
                     + "                WHERE userId = " + userId
                     + "                  AND msoId = " + msoId
                     + "                  AND channelId NOT IN ("
                     + "                                SELECT channelId FROM nncloudtv_nnuser1.nnuser_subscribe "
                     + "                                 WHERE userId = " + userId + " and msoId = " + msoId
                     + "                      )"
                     + "              )"
                     + "     ORDER BY updateDate DESC";
        
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
