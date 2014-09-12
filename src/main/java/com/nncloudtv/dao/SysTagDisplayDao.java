package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;

public class SysTagDisplayDao extends GenericDao<SysTagDisplay> {

    protected static final Logger log = Logger.getLogger(SysTagDisplayDao.class.getName());
    
    public SysTagDisplayDao() {
        super(SysTagDisplay.class);
    }    
    
    public List<SysTagDisplay> findByType(long msoId, short type, String lang) {
        
        String query = "select * from systag_display a1 "
                     + "inner join (select d.id from systag s, systag_display d "
                     + "            where s.msoId = " + msoId
                     + "            and s.type = " + type
                     + "            and d.lang = " + NnStringUtil.escapedQuote(lang)
                     + "            and s.id = d.systagId "
                     + "            order by s.seq) a2 "
                     + "on a1.id = a2.id";
        
        return sql(query);
    }
    
    public List<SysTagDisplay> findDayparting(short baseTime, String lang, long msoId) {
        
        String query = "select * from systag_display a1 "
                     + "   inner join (select d.id from systag s, systag_display d "
                     + "                          where s.msoId = " + msoId 
                     + "                            and type = " + SysTag.TYPE_DAYPARTING
                     + "                            and lang = " + NnStringUtil.escapedQuote(lang)
                     + "                            and s.id = d.systagId "
                     + "                            and (((s.timeStart != 0 or s.timeEnd != 0) and s.timeEnd > " + baseTime + " and s.timeStart <= " + baseTime + ") or"
                     + "                                 (s.timeStart = 0 and s.timeEnd = 0)) " 
                     + "                       order by s.seq) a2"
                     + "           on a1.id = a2.id";
        
        return sql(query);
    }
            
    public List<SysTagDisplay> findRecommendedSets(String lang, long msoId, short type) {
        
        String query = " select * from systag_display a1 "
                     + " inner join "
                     + "(select d.id, s.seq " 
                     + "  from systag s, systag_display d "
                     + " where s.msoId = " + msoId + ""
                     + "   and s.type = " + type
                     + "   and s.id = d.systagId "
                     + "   and featured = true "
                     + "   and d.lang = " + NnStringUtil.escapedQuote(lang) + ") a2"
                     + "   on a1.id = a2.id"
                     + "   order by a2.startTime asc, a2.seq asc";
        
        return sql(query);
    }
    
    public List<SysTagDisplay> findPlayerCategories(String lang, long msoId) {
        
        String query = " select * from systag_display a1 "
                     + " inner join "
                     + "(select d.id, s.seq " 
                     + "  from systag s, systag_display d "
                     + " where s.msoId = " + msoId + ""
                     + "   and s.type = " + SysTag.TYPE_CATEGORY
                     + "   and s.id = d.systagId "
                     + "   and d.lang=" + NnStringUtil.escapedQuote(lang) + ") a2"
                     + "   on a1.id=a2.id "
                     + "   order by a2.seq asc";
        
        return sql(query);
    }    
    
    //will need mso in the future to avoid name conflicts
    public SysTagDisplay findByName(String name) {
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        SysTagDisplay detached = null;
        try {
            Query query = pm.newQuery(SysTagDisplay.class);
            query.setFilter("name == " + NnStringUtil.escapedQuote(name));
            @SuppressWarnings("unchecked")
            List<SysTagDisplay> results = (List<SysTagDisplay>) query.execute();
            if (results.size() > 0) {
                detached = pm.detachCopy(results.get(0));
            }
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public SysTagDisplay findBySysTagId(long sysTagId) {
        
        PersistenceManager pm = getPersistenceManager();
        SysTagDisplay detached = null;
        try {
            Query query = pm.newQuery(SysTagDisplay.class);
            query.setFilter("systagId == " + sysTagId);
            @SuppressWarnings("unchecked")
            List<SysTagDisplay> results = (List<SysTagDisplay>) query.execute();
            if (results.size() > 0) {
                detached = pm.detachCopy(results.get(0));
            }
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public List<SysTagDisplay> findAllBySysTagId(long sysTagId) {
        
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        List<SysTagDisplay> detached = new ArrayList<SysTagDisplay>();
        
        try {
            Query query = pm.newQuery(SysTagDisplay.class);
            query.setFilter("systagId == " + sysTagId);
            @SuppressWarnings("unchecked")
            List<SysTagDisplay> results = (List<SysTagDisplay>) query.execute();
            
            if (results != null && results.size() > 0) {
                detached = (List<SysTagDisplay>) pm.detachCopyAll(results);
            }
        } finally {
            pm.close();
        }
        
        return detached;
    }
    
    public SysTagDisplay findBySysTagIdAndLang(Long sysTagId, String lang) {
    
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        SysTagDisplay detached = null;
        try {
            Query query = pm.newQuery(SysTagDisplay.class);
            query.setFilter("systagId == sysTagIdParam && lang == langParam");
            query.declareParameters("long sysTagIdParam, String langParam");
            @SuppressWarnings("unchecked")
            List<SysTagDisplay> results = (List<SysTagDisplay>) query.execute(sysTagId, lang);
            if (results.size() > 0) {
                detached = pm.detachCopy(results.get(0));
            }
        } finally {
            pm.close();
        }
        return detached;
    }
}
