package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(table = "poi", detachable = "true")
public class Poi implements PersistentBaseModel {
    
    private static final long serialVersionUID = -6330960416877922240L;
    private static final boolean cachable = false;
    
    public boolean isCachable() {
        return cachable;
    }
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    @Persistent
    private long campaignId;
    
    @Persistent
    private long eventId;
    
    @Persistent
    private long pointId;
    
    @Persistent
    private Date startDate;
    
    @Persistent
    private Date endDate;
    
    @Persistent
    private String hoursOfWeek;
    
    @Persistent
    private Date updateDate;
    
    public long getCampaignId() {
        return campaignId;
    }
    
    public void setCampaignId(long campaignId) {
        this.campaignId = campaignId;
    }
    
    public long getEventId() {
        return eventId;
    }
    
    public void setEventId(long eventId) {
        this.eventId = eventId;
    }
    
    public long getPointId() {
        return pointId;
    }
    
    public void setPointId(long pointId) {
        this.pointId = pointId;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public String getHoursOfWeek() {
        return hoursOfWeek;
    }
    
    public void setHoursOfWeek(String hoursOfWeek) {
        this.hoursOfWeek = hoursOfWeek;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
}
