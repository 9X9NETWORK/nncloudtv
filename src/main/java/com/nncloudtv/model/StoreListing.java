package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * blacklist
 * */
@PersistenceCapable(table = "store_listing", detachable = "true")
@Inheritance(customStrategy = "complete-table")
public class StoreListing extends PersistentModel {
    
    @Persistent
    private long channelId;
    
    @Persistent
    private long msoId;
    
    @Persistent
    private Date updateDate;
    
    public StoreListing(long channelId, long msoId) {
        this.channelId = channelId;
        this.msoId = msoId;
    }
    
    public long getChannelId() {
        return channelId;
    }
    
    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }
    
    public long getMsoId() {
        return msoId;
    }
    
    public void setMsoId(long msoId) {
        this.msoId = msoId;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
}
