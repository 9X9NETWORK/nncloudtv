package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.Poi;
import com.nncloudtv.model.PoiEvent;
import com.nncloudtv.model.PoiPoint;

@Service
public class PoiPointManager {
    protected static final Logger log = Logger.getLogger(PoiPointManager.class.getName());
    
    public PoiPoint save(PoiPoint point) {
        Date now = NnDateUtil.now();
        if (point.getCreateDate() == null)
            point.setCreateDate(now);
        point.setUpdateDate(now);
        return NNF.getPoiPointDao().save(point);
    }
    
    public void delete(PoiPoint point) {
        
        if (point == null) {
            return ;
        }
        
        List<Poi> pois = NNF.getPoiMngr().findByPointId(point.getId());
        List<PoiEvent> events = new ArrayList<PoiEvent>();
        for (Poi poi : pois) {
            PoiEvent event = NNF.getPoiEventMngr().findById(poi.getEventId());
            if (event != null) {
                
                events.add(event);
            }
        }
        
        NNF.getPoiPointDao().delete(point);
        NNF.getPoiEventDao().deleteAll(events);
        NNF.getPoiDao().deleteAll(pois);
    }
    
    public PoiPoint findById(long id) {
        
        return NNF.getPoiPointDao().findById(id);
    }
    
    public List<PoiPoint> findByChannel(long channelId) {
        
        return NNF.getPoiPointDao().findByChannel(channelId);
    }
    
    public List<PoiPoint> findCurrentByChannelId(long channelId) {
        
        return NNF.getPoiPointDao().findCurrentByChannelId(channelId);
    }
    
    public List<PoiPoint> findCurrentByProgramId(long programId) {
        
        return NNF.getPoiPointDao().findCurrentByProgramId(programId);
    }
    
    public void delete(List<PoiPoint> points) {
        
        if (points == null || points.isEmpty()) { return; }
        
        Set<Poi> poiSet = new HashSet<Poi>();
        Set<PoiEvent> eventSet = new HashSet<PoiEvent>();
        
        for (PoiPoint point : points) {
            
            List<Poi> pois = NNF.getPoiMngr().findByPointId(point.getId());
            for (Poi poi : pois) {
                
                eventSet.add(NNF.getPoiEventMngr().findById(poi.getEventId()));
            }
            poiSet.addAll(pois);
        }
        
        NNF.getPoiEventDao().deleteAll(eventSet);
        NNF.getPoiDao().deleteAll(poiSet);
        NNF.getPoiPointDao().deleteAll(points);
    }
    
    public List<PoiPoint> findByProgramId(long programId) {
        
        return NNF.getPoiPointDao().findByProgramId(programId);
    }
    
    public boolean isPointCollision(PoiPoint originPoint, NnProgram program, int startTime, int endTime) {
        if (program == null) {
            return true;
        }
        if (startTime < 0 || endTime < 1 || endTime <= startTime) {
            return true;
        }
        if (startTime < program.getStartTimeInt() || startTime >= program.getEndTimeInt()) {
            return true;
        }
        if (endTime <= program.getStartTimeInt() || endTime > program.getEndTimeInt())
        {
            return true;
        }
        
        List<PoiPoint> points = NNF.getPoiPointDao().findByProgramId(program.getId());
        if (originPoint != null) {
            if (points.contains(originPoint)) {
                points.remove(originPoint);
            }
        }
        int duration = program.getDurationInt();
        for (PoiPoint point : points) {
            if (startTime >= point.getStartTimeInt() && startTime < point.getEndTimeInt()) {
                return true;
            }
            if ((point.getStartTimeInt() - startTime) > 0 && (point.getStartTimeInt() - startTime) < duration) {
                duration = point.getStartTimeInt() - startTime;
            }
        }
        if (endTime <= startTime + duration) {
            return false;
        } else {
            return true;
        }
    }
    
    // order by StartTime asc
    public static Comparator<PoiPoint> getStartTimeComparator() {
        
        class PointStartTimeComparator implements Comparator<PoiPoint> {
            public int compare(PoiPoint point1, PoiPoint point2) {
                return (point1.getStartTimeInt() - point2.getStartTimeInt());
            }
        }
        
        return new PointStartTimeComparator();
    }
    
    // TODO: remove
    public Long findOwner(PoiPoint point) {
        
        if (point == null) {
            return null;
        }
        
        NnChannelManager channelMngr = NNF.getChannelMngr();
        
        if (point.getType() == PoiPoint.TYPE_SUBEPISODE) {
            
            NnProgram program = NNF.getProgramMngr().findById(point.getTargetId());
            if (program == null) {
                return null;
            }
            NnChannel channel = channelMngr.findById(program.getChannelId());
            if (channel == null) {
                return null;
            }
            return channel.getUserId();
        } else if (point.getType() == PoiPoint.TYPE_CHANNEL) {
            
            NnChannel channel = channelMngr.findById(point.getTargetId());
            if (channel == null) {
                return null;
            }
            return channel.getUserId();
        } else { // other type not used now
            return null;
        }
    }
    
    // TODO move
    public Poi findPoiById(long id) {
        
        return NNF.getPoiDao().findById(id);
    }
    
    public List<Poi> findCurrentPoiByChannel(long channelId) {
        
        return NNF.getPoiDao().findCurrentByChannel(channelId);
    }
    
    public List<Poi> findCurrentPoiByProgram(long programId) {
        
        return NNF.getPoiDao().findCurrentByProgram(programId);
    }
    
}
