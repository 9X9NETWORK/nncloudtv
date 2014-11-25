package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * To relate Category and Channel
 */
@PersistenceCapable(table = "tag_map", detachable = "true")
public class TagMap extends PersistentModel {
    
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
