package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable(table = "poi", detachable = "true")
@Inheritance(customStrategy = "complete-table")
public class Poi extends PersistentModel {
    
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