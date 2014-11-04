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
    
    public PoiEvent save(PoiEvent event) {
        
        event.setUpdateDate(new Date());
        if (event.getCreateDate() == null) {
            
            event.setCreateDate(new Date());
        }
        event = NNF.getPoiEventDao().save(event);
        
        return event;
    }
    
    public void delete(PoiEvent poiEvent) {
        
        NNF.getPoiEventDao().delete(poiEvent);
    }
    
    public void delete(List<PoiEvent> poiEvents) {
        
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
    
    public PoiEvent findByPoi(Long poiId) {        
        PoiEvent result = NNF.getPoiEventDao().findByPoi(poiId);
        return result;
    }
    
    public PoiEvent findById(String eventIdStr) {
        
        return NNF.getPoiEventDao().findById(eventIdStr);
    }
    
}
