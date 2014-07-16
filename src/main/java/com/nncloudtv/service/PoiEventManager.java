package com.nncloudtv.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.PoiEvent;

@Service
public class PoiEventManager {
    
    protected static final Logger log = Logger.getLogger(PoiEventManager.class.getName());
    
    public PoiEvent create(PoiEvent poiEvent) {
        Date now = new Date();
        poiEvent.setCreateDate(now);
        poiEvent.setUpdateDate(now);
        poiEvent = NNF.getPoiEventDao().save(poiEvent);
        return poiEvent;
    }
    
    public PoiEvent save(PoiEvent poiEvent) {
        Date now = new Date();
        poiEvent.setUpdateDate(now);
        poiEvent = NNF.getPoiEventDao().save(poiEvent);
        return poiEvent;
    }
    
    public void delete(PoiEvent poiEvent) {
        if (poiEvent == null) {
            return ;
        }
        NNF.getPoiEventDao().delete(poiEvent);
    }
    
    public void delete(List<PoiEvent> poiEvents) {
        NNF.getPoiEventDao().deleteAll(poiEvents);
    }
    
    public void deleteByIds(List<Long> eventIds) {
        List<PoiEvent> poiEvents = NNF.getPoiEventDao().findAllByIds(eventIds); // when List too long, TODO : will need rewrite
        NNF.getPoiEventDao().deleteAll(poiEvents);
    }
    
    public static String composeContext(Map<String, Object> context, int eventType) {
        // compose rule
        String result = "";
        if (eventType == PoiEvent.TYPE_HYPERLINK) {
            if (context.containsKey("link")) {
                result += context.get("link");
            }
            result += "|";
            if (context.containsKey("button")) {
                result += context.get("button");
            }
        }
        return result;
    }
    
    private Map<String, Object> explainContext_hyperChannel(String context) {
        // pair with compose rule
        Map<String, Object> output = new TreeMap<String, Object>();
        String[] values = context.split("\\|");
        switch (values.length) {
        case 0:
            output.put("link", "");
            output.put("button", "");
            break;
        case 1:
            output.put("link", values[0]);
            output.put("button", "");
            break;
        case 2:
            output.put("link", values[0]);
            output.put("button", values[1]);
            break;
        default:
            output.put("link", "");
            output.put("button", "");
        }
        
        return output;
    }
    
    public Map<String, Object> eventExplainFactory(PoiEvent event) {
        Map<String, Object> output = new TreeMap<String, Object>();
        if (event.getType() == PoiEvent.TYPE_HYPERLINK) {
            Map<String, Object> context = explainContext_hyperChannel(event.getContext());
            output.putAll(context);
        }
        output.put("message", event.getMessage());
        
        return output;
    }
    
    public PoiEvent findEventsByPoi(long poiId) {
        return NNF.getPoiEventDao().findByPoi(poiId);
    }
    
    public PoiEvent findByPoint(long pointId) {
        return NNF.getPoiEventDao().findByPoint(pointId);
    }
    
    public PoiEvent findById(Long eventId) {
        
        if (eventId == null) {
            return null;
        }
        
        PoiEvent result = NNF.getPoiEventDao().findById(eventId);
        return result;
    }
    
    public boolean isValidEventType(Short eventType) {
        
        if (eventType == null) {
            return false;
        }
        if (eventType == PoiEvent.TYPE_POPUP) {
            return true;
        }
        if (eventType == PoiEvent.TYPE_HYPERLINK) {
            return true;
        }
        if (eventType == PoiEvent.TYPE_INSTANTNOTIFICATION) {
            return true;
        }
        if (eventType == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            return true;
        }
        if (eventType == PoiEvent.TYPE_POLL) {
            return true;
        }
        
        return false;
    }

    public PoiEvent findByPoi(Long poiId) {        
        PoiEvent result = NNF.getPoiEventDao().findByPoi(poiId);
        return result;
    }
    
}
