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
import com.nncloudtv.model.LocaleTable;
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
        
        String orderStr = " ORDER by m.alwaysOnTop DESC, ";
        if (lang != null && isPlayer == true && limitRows == false) // store only
            orderStr += " CASE m.alwaysOnTop WHEN TRUE THEN m.seq ELSE (CASE c.sphere WHEN " + NnStringUtil.escapedQuote(lang) + " THEN 1 ELSE 2 END) END,";
        orderStr += " c.updateDate DESC ";
        if (sort == SysTag.SORT_SEQ)
            orderStr = " ORDER BY m.seq ";
        if (limitRows)
            orderStr = " ORDER BY RAND() LIMIT 9 ";
        if (start >= 0 && count > 0)
            orderStr += String.format(" LIMIT %d, %d", start, count);
        String playerStr =    (!isPlayer) ? "" : String.format(" AND c.isPublic = TRUE AND c.status = %d ", NnChannel.STATUS_SUCCESS);
        String langStr   = (lang == null) ? "" : String.format(" AND c.sphere IN (%s, %s) ", NnStringUtil.escapedQuote(lang), NnStringUtil.escapedQuote(LocaleTable.LANG_OTHER));
        String blackList =   (msoId == 0) ? "" : String.format(" AND c.id NOT IN (SELECT channelId FROM store_listing WHERE msoId = %d) ", msoId);
        String query = "SELECT * FROM nnchannel a1"
                     + "   INNER JOIN (SELECT distinct c.id "
                     + "                 FROM systag_display d, systag_map m, nnchannel c "
                     + "                WHERE d.systagId = " + systagId 
                     + "                  AND d.systagId = m.systagId "
                     + "                  AND c.id = m.channelId "
                     + "                  AND c.contentType != " + NnChannel.CONTENTTYPE_FAVORITE
                     + "                  " + playerStr
                     + "                  " + blackList
                     + "                  " + langStr
                     + "                  " + orderStr
                     + "              ) a2 ON a1.id = a2.id";
        
        return sql(query);
    }
    
    public List<NnChannel> findByContentType(short type) {
        List<NnChannel> detached = new ArrayList<NnChannel>(); 
        PersistenceManager pm = getPersistenceManager();
        try {
            Query query = pm.newQuery(NnChannel.class);
            query.setFilter("contentType == contentTypeParam");
            query.declareParameters("short contentTypeParam");
            @SuppressWarnings("unchecked")
            List<NnChannel> channels = (List<NnChannel>) query.execute(type);
            if (channels.size() > 0)
                detached = (List<NnChannel>) pm.detachCopyAll(channels);
            query.closeAll();
        } finally {
            pm.close();
        }
        return detached;
    }    
    
    //find good channels, for all needs to be extended
    public List<NnChannel> findChannelsByTag(String name) {
        
        String query = "SELECT * FROM nnchannel "
                     + "        WHERE id IN ( "
                     + "                     SELECT DISTINCT map.channelId " 
                     + "                       FROM ytprogram yt, tag_map map " 
                     + "                      WHERE yt.channelId = map.channelId "
                     + "                        AND map.tagId = (SELECT id FROM tag WHERE name = " + NnStringUtil.escapedQuote(name) + ")"
                     + "                    ) "
                     + "     ORDER BY RAND() LIMIT 9;";
        
        return sql(query);
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
        
        log.info("start:" + start + ";end:" + limit);
        if (start == 0) start = 0;
        if (limit == 0) limit = 9;
        
        String query = "SELECT * FROM nnchannel "
                     + "        WHERE (LOWER(name) LIKE LOWER(" + NnStringUtil.escapedQuote("%" + keyword + "%") + ")";
        if (all || content == null || !content.equals(SearchLib.STORE_ONLY)) // PCS
            query += " || LOWER(intro) LIKE LOWER(" + NnStringUtil.escapedQuote("%" + keyword + "%") + ")";
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
        if (extra != null)
            query += " AND (" + extra + ")";
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
            
            List<NnChannel> channels = new ArrayList<NnChannel>();
            PersistenceManager pm = getPersistenceManager();
            try {
                Query query = pm.newQuery(NnChannel.class);
                query.setOrdering("seq asc, contentType asc");
                query.setFilter("userIdStr == userIdStrParam");
                query.declareParameters("String userIdStrParam");
                if (limit > 0)
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
                for (String str : split)
                    list.add(NnStringUtil.escapedQuote(str));
                listStr = StringUtils.join(list, ",");
            }
            
            String query = "SELECT * FROM nnchannel "
                         + "        WHERE userIdStr IN (" + listStr + ")"
                         + "          AND isPublic = TRUE "
                         + "          AND status IN (0, 3) "
                         + "     ORDER BY seq, contentType ";
            if (limit > 0)
                query += " LIMIT " + limit;
            
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
                     + "        WHERE c.id IN ( "
                     + "                       SELECT channelId "
                     + "                         FROM nncloudtv_nnuser1.nnuser_watched "
                     + "                        WHERE userId = " + userId
                     + "                          AND msoId = " + msoId
                     + "                          AND channelId NOT IN ( "
                     + "                                                SELECT channelId "
                     + "                                                  FROM nncloudtv_nnuser1.nnuser_subscribe "
                     + "                                                 WHERE userId = " + userId
                     + "                                                   AND msoId = " + msoId
                     + "                                               )"
                     + "                      )"
                     + "     ORDER BY updateDate DESC";
        
        return sql(query);
    }
    
    public List<NnChannel> getCategoryChannels(long categoryId, List<String> spheres) {
        
        String filter = "";
        if (spheres != null && !spheres.isEmpty()) {
            for (int i = 0; i < spheres.size(); i++)
                spheres.set(i, NnStringUtil.escapedQuote(spheres.get(i)));
            filter += " AND c.sphere IN (";
            filter += StringUtils.join(spheres, ",");
            filter += ")";
        }
        
        String query = "SELECT * FROM nnchannel a1 "
                     + "   INNER JOIN ( "
                     + "               SELECT DISTINCT c.id"
                     + "                 FROM systag_display d, systag_map m, nnchannel c "
                     + "                WHERE d.systagId = " + categoryId
                     + "                  AND d.systagId = m.systagId "
                     + "                  AND c.id = m.channelId "
                     + "                  AND c.isPublic = TRUE "
                     + "                  AND c.contentType != " + NnChannel.CONTENTTYPE_FAVORITE
                     + "                  AND c.status = " + NnChannel.STATUS_SUCCESS
                     + "                    " + filter
                     + "                ORDER BY c.updateDate DESC "
                     + "              ) a2 "
                     + "           ON a1.id = a2.id";
                    
        return sql(query);
    }
}
