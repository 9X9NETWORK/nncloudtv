package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.PoiMapDao;
import com.nncloudtv.dao.PoiPointDao;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.PoiEvent;
import com.nncloudtv.model.PoiMap;
import com.nncloudtv.model.PoiPoint;

@Service
public class PoiPointManager {
    protected static final Logger log = Logger.getLogger(PoiPointManager.class.getName());
    
    private PoiPointDao dao = new PoiPointDao();
    private PoiMapDao poiMapDao = new PoiMapDao();
    private PoiEventManager poiEventMngr = new  PoiEventManager();
    
    public PoiPoint create(PoiPoint poi) {
        Date now = new Date();
        poi.setCreateDate(now);
        poi.setUpdateDate(now);
        poi = dao.save(poi);
        return poi;
    }
    
    public PoiPoint save(PoiPoint poi) {
        Date now = new Date();
        poi.setUpdateDate(now);
        poi = dao.save(poi);
        return poi;
    }
    
    public void delete(PoiPoint poi) {
        if (poi == null) {
            return ;
        }
        List<PoiMap> poiMaps = poiMapDao.findByPoiId(poi.getId());
        List<Long> eventIds = new ArrayList<Long>();
        for (PoiMap poiMap : poiMaps) {
            eventIds.add(poiMap.getEventId());
        }
        poiEventMngr.deleteByIds(eventIds);
        poiMapDao.deleteAll(poiMaps);
        dao.delete(poi);
    }
    
    public void delete(List<PoiPoint> pois) {
        List<PoiMap> poiMaps = new ArrayList<PoiMap>();
        List<PoiMap> temps;
        List<Long> eventIds = new ArrayList<Long>();
        for (PoiPoint poi : pois) {
            temps = poiMapDao.findByPoiId(poi.getId()); //TODO: computing issue, try to reduce mysql queries
            for (PoiMap temp : temps) {
                eventIds.add(temp.getEventId());
            }
            poiMaps.addAll(temps);
        }
        poiEventMngr.deleteByIds(eventIds);
        poiMapDao.deleteAll(poiMaps);
        dao.deleteAll(pois);
    }
    
    public boolean hookEvent(long poiId, long eventId) {
        PoiMap poiMap = new PoiMap();
        Date now = new Date();
        poiMap.setPoiId(poiId);
        poiMap.setEventId(eventId);
        poiMap.setUpdateDate(now);
        poiMap = poiMapDao.save(poiMap);
        
        if (poiMap != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public PoiPoint findById(long id) {
        return dao.findById(id);
    }
    
    public List<PoiPoint> findByProgramId(long programId) {
        return dao.findByProgram(programId);
    }
    
    public List<PoiPoint> findByChannel(long channelId) {
        return dao.findByChannel(channelId);
    }
    
    public List<PoiPoint> findByProgram(long programId) {
        return dao.findByProgram(programId);
    }
    
    public Map<String, Object> getEventByPoi(PoiPoint poi) {
        Map<String, Object> result = new TreeMap<String, Object>();
        if (poi == null) {
            return result;
        }
        
        // event part
        List<PoiEvent> poiEvents = poiEventMngr.findPoiEventsByPoiId(poi.getId());
        if (poiEvents.size() == 0) {
            // dynamic assign event by rules
            
        } else {
            // there should apply some rule to choose an event
            PoiEvent event = poiEvents.get(0);
            result.putAll(poiEventMngr.eventExplainFactory(event));
        }
        
        // POI part
        result.put("id", poi.getId());
        result.put("programId", poi.getTargetId());
        result.put("name", poi.getName());
        //result.put("intro", poi.getIntro());
        result.put("intro", null);
        result.put("startTime", poi.getStartTimeInt());
        result.put("endTime", poi.getEndTimeInt());
        result.put("tag", poi.getTag());
        
        return result;
    }
    
    public List<Map<String, Object>> getEventsByProgram(NnProgram program) {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        if (program == null) {
            return results;
        }
        
        // sort
        List<PoiPoint> pois = dao.findByProgram(program.getId());
        Collections.sort(pois, getPoiStartTimeComparator());
        
        for (PoiPoint poi : pois) {
            results.add(getEventByPoi(poi)); // TODO: computing issue, try to reduce mysql queries, List<List<PoiEvent>> List<int>
        }
        return results;
    }
    
    // list which need revertHtml, should avoid collision issue
    public static Map<String, Object> revertHtml(Map<String, Object> responseObj) {
        String[] keys = {"name", "intro", "message", "button"}; 
        
        for (String key : keys) {
            if(responseObj.containsKey(key)) {
                String temp = (String) responseObj.get(key);
                temp = NnStringUtil.revertHtml(temp);
                responseObj.put(key, temp);
            }
        }
        
        return responseObj;
    }
    
    public boolean isPoiCollision(PoiPoint originPoi, NnProgram program, int startTime, int endTime) {
        if (program == null) {
            return true;
        }
        if (startTime < 0 || endTime < 1 || endTime <= startTime) {
            return true;
        }
        if (startTime < program.getStartTimeInt() || startTime >= program.getEndTimeInt())
            return true;
        if (endTime <= program.getStartTimeInt() || endTime > program.getEndTimeInt())
            return true;
        
        List<PoiPoint> pois = dao.findByProgram(program.getId());
        if (originPoi != null) {
            if (pois.contains(originPoi)) {
                pois.remove(originPoi);
            }
        }
        int duration = program.getDurationInt();
        for (PoiPoint poi : pois) {
            if (startTime >= poi.getStartTimeInt() && startTime < poi.getEndTimeInt()) {
                return true;
            }
            if ((poi.getStartTimeInt() - startTime) > 0 && (poi.getStartTimeInt() - startTime) < duration) {
                duration = poi.getStartTimeInt() - startTime;
            }
        }
        if (endTime <= startTime + duration) {
            return false;
        } else {
            return true;
        }
    }
    
    // order by StartTime asc
    public Comparator<PoiPoint> getPoiStartTimeComparator() {
        
        class PoiStartTimeComparator implements Comparator<PoiPoint> {
            public int compare(PoiPoint poi1, PoiPoint poi2) {
                return (poi1.getStartTimeInt() - poi2.getStartTimeInt());
            }
        }
        
        return new PoiStartTimeComparator();
    }

}
