package com.nncloudtv.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Subscription count
 */
@PersistenceCapable(table = "cnt_view", detachable = "true")
public class CntView implements PersistentModel {
    
    private static final long serialVersionUID = -4453915666072588744L;
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
    private long channelId;
    
    @Persistent
    private long date;
    
    @Persistent
    private int cnt;
    
    public CntView(long channelId) {
        this.channelId = channelId;
        this.cnt = 1;
    }
    
    public long getDate() {
        return date;
    }
    
    public void setDate(long date) {
        this.date = date;
    }
    
    public long getChannelId() {
        return channelId;
    }
    
    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }
    
    public int getCnt() {
        return cnt;
    }
    
    public void setCnt(int cnt) {
        this.cnt = cnt;
    }
    
}
