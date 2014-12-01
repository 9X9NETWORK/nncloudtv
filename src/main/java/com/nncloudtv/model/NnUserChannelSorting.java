package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Store user's sorting preference for individual channel. 
 * Example would be newest to oldest, or oldest to newest.
 */
@PersistenceCapable(table = "nnuser_channel_sorting", detachable = "true")
public class NnUserChannelSorting implements PersistentModel {
    
    private static final long serialVersionUID = 1282142339904331933L;
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
    private long userId;
    
    @Persistent
    private long channelId;
    
    @Persistent
    private short sort; //how a channel's sorted
    public static final short SORT_NEWEST_TO_OLDEST = 1; //default
    public static final short SORT_OLDEST_TO_NEWEST = 2;
    public static final short SORT_MAPEL = 3; //mapel channel sorting, meaning can not be changed by user
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    public NnUserChannelSorting(long userId, long channelId, short sorting) {
        this.userId = userId;
        this.channelId = channelId;
        this.sort = sorting;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public long getChannelId() {
        return channelId;
    }
    
    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }
    
    public short getSort() {
        return sort;
    }
    
    public void setSort(short sort) {
        this.sort = sort;
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
}
