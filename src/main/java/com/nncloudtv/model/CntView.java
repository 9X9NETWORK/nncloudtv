package com.nncloudtv.model;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Subscription count
 */
@PersistenceCapable(table = "cnt_view", detachable = "true")
@Inheritance(customStrategy = "complete-table")
public class CntView extends PersistentModel {
    
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
