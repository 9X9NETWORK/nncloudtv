package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table = "poi_pdr", detachable = "true")
public class PoiPdr implements PersistentModel {
    
    private static final long serialVersionUID = -7038238631337505136L;
    private static final boolean cachable = true;
    
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
    private long userId; //if a device has associated user account, not always
    
    @Persistent
    private long msoId;
    
    @Persistent
    private long eventId; //if a device has associated user account, not always
    
    @Persistent
    private long poiId; //if a device has associated user account, not always
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String select;
    
    @Persistent
    private Date updateDate;
    
    @Persistent
    private Date scheduledDate;
    
    @Persistent
    private boolean notified;
    
    public PoiPdr(long userId, long msoId, long poiId, long eventId, String select) {
        this.userId = userId;
        this.msoId = msoId;
        this.poiId = poiId;
        this.eventId = eventId;
        this.select = select;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
    public long getMsoId() {
        return msoId;
    }
    
    public void setMsoId(long msoId) {
        this.msoId = msoId;
    }
    
    public String getSelect() {
        return select;
    }
    
    public void setSelect(String select) {
        this.select = select;
    }
    
    public long getEventId() {
        return eventId;
    }
    
    public void setEventId(long eventId) {
        this.eventId = eventId;
    }
    
    public long getPoiId() {
        return poiId;
    }
    
    public void setPoiId(long poiId) {
        this.poiId = poiId;
    }
    
    public Date getScheduledDate() {
        return scheduledDate;
    }
    
    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
    
    public boolean isNotified() {
        return notified;
    }
    
    public void setNotified(boolean notified) {
        this.notified = notified;
    }
    
}
