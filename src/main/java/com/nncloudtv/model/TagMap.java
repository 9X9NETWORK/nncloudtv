package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * To relate Category and Channel
 */
@PersistenceCapable(table = "tag_map", detachable = "true")
public class TagMap implements PersistentBaseModel {
    
    private static final long serialVersionUID = -2469181924008224613L;
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
    private long tagId;
    
    @Persistent
    private long channelId;
        
    @Persistent
    private Date updateDate;
    
    public TagMap(long tagId, long channelId) {
        this.tagId = tagId;
        this.channelId = channelId;
        this.updateDate = new Date();
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
    public long getTagId() {
        return tagId;
    }
    
    public void setTagId(long tagId) {
        this.tagId = tagId;
    }
    
    public long getChannelId() {
        return channelId;
    }
    
    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }
}
