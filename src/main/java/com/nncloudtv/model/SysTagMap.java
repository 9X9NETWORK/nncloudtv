package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table = "systag_map", detachable = "true")
public class SysTagMap implements PersistentModel {
    
    private static final long serialVersionUID = 6967129426270985959L;
    
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
    long sysTagId;
    
    @Persistent
    long channelId;
    
    @Persistent
    short timeStart; //for dayparting
    
    @Persistent
    short timeEnd; //for dayparting
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.VERY_SHORT_STRING_LENGTH)
    String attr;
    
    @Persistent
    boolean alwaysOnTop;
    
    @Persistent
    boolean featured;
    
    @Persistent
    short seq;
    
    @Persistent 
    Date createDate;
        
    @Persistent
    Date updateDate;
    
    public SysTagMap(long sysTagId, long channelId) {
        
        this.sysTagId = sysTagId;
        this.channelId = channelId;
        this.seq = 0;
        this.alwaysOnTop = false;
        this.featured = false;
    }
    
    public long getSysTagId() {
        return sysTagId;
    }
    
    public void setSysTagId(long sysTagId) {
        this.sysTagId = sysTagId;
    }
    
    public long getChannelId() {
        return channelId;
    }
    
    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }
    
    public short getSeq() {
        return seq;
    }
    
    public void setSeq(short seq) {
        this.seq = seq;
    }
    
    public Date getCreateDate() {
        return createDate;
    }
    
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
    public short getTimeStart() {
        return timeStart;
    }
    
    public void setTimeStart(short timeStart) {
        this.timeStart = timeStart;
    }
    
    public short getTimeEnd() {
        return timeEnd;
    }
    
    public void setTimeEnd(short timeEnd) {
        this.timeEnd = timeEnd;
    }
    
    public String getAttr() {
        return attr;
    }
    
    public void setAttr(String attr) {
        this.attr = attr;
    }
    
    public boolean isAlwaysOnTop() {
        return alwaysOnTop;
    }
    
    public void setAlwaysOnTop(boolean alwaysOnTop) {
        this.alwaysOnTop = alwaysOnTop;
    }
    
    public boolean isFeatured() {
        return featured;
    }
    
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
    
}
