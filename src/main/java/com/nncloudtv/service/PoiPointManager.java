package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.Poi;
import com.nncloudtv.model.PoiPoint;

@Service
public class PoiPointManager {
    protected static final Logger log = Logger.getLogger(PoiPointManager.class.getName());
    
    public PoiPoint create(PoiPoint point) {
        Date now = new Date();
        point.setCreateDate(now);
        point.setUpdateDate(now);
        point = NNF.getPoiPointDao().save(point);
        return point;
    }
    
    public PoiPoint save(PoiPoint point) {
        Date now = new Date();
        point.setUpdateDate(now);
        point = NNF.getPoiPointDao().save(point);
        return point;
    }
    
    public void delete(PoiPoint point) {
        
        if (point == null) {
            return ;
        }
        
        List<Poi> pois = NNF.getPoiDao().findByPointId(point.getId());
        List<Long> eventIds = new ArrayList<Long>();
        if (pois != null) {
            for (Poi p : pois) {
                eventIds.add(p.getEventId());
            }
        }
        
        // TODO : rewrite when AD's cms is ready
        if (eventIds.size() > 0) {
            NNF.getPoiEventMngr().deleteByIds(eventIds);
        }
        if (pois != null && pois.size() > 0) {
            NNF.getPoiDao().deleteAll(pois);
        }
        NNF.getPoiPointDao().delete(point);
    }
    
    public void delete(List<PoiPoint> points) {
        List<Poi> pois = new ArrayList<Poi>();
        List<Poi> temps;
        List<Long> eventIds = new ArrayList<Long>();
        for (PoiPoint point : points) {
            temps = NNF.getPoiDao().findByPointId(point.getId()); // TODO: computing issue, try to reduce mysql queries
            for (Poi temp : temps) {
                eventIds.add(temp.getEventId());
            }
            pois.addAll(temps);
        }
        
        // TODO : rewrite when AD's cms is ready
        NNF.getPoiEventMngr().deleteByIds(eventIds);
        NNF.getPoiDao().deleteAll(pois);
        NNF.getPoiPointDao().deleteAll(points);
    }
    
    public PoiPoint findById(long id) {
        return NNF.getPoiPointDao().findById(id);
    }
    
    public List<PoiPoint> findByChannel(long channelId) {
        return NNF.getPoiPointDao().findByChannel(channelId);
    }

    public List<PoiPoint> findCurrentByChannel(long channelId) {
        return NNF.getPoiPointDao().findCurrentByChannel(channelId);
    }
    
    public List<PoiPoint> findCurrentByProgram(long programId) {
        return NNF.getPoiPointDao().findCurrentByProgram(programId);
    }
    
    public List<PoiPoint> findByProgram(long programId) {
        
        List<PoiPoint> points = NNF.getPoiPointDao().findByProgram(programId);
        if (points != null) {
            Collections.sort(points, getPointStartTimeComparator());
        }
        
        return points;
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
        
        List<PoiPoint> points = NNF.getPoiPointDao().findByProgram(program.getId());
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
    public Comparator<PoiPoint> getPointStartTimeComparator() {
        
        class PointStartTimeComparator implements Comparator<PoiPoint> {
            public int compare(PoiPoint point1, PoiPoint point2) {
                return (point1.getStartTimeInt() - point2.getStartTimeInt());
            }
        }
        
        return new PointStartTimeComparator();
    }
    
    /** return owner's userId */
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
