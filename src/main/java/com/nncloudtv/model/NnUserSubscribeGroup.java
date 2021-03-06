package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

/**
 * For user's IPG. User's channels are grouped into 9 groups  
 */
@PersistenceCapable(table = "nnuser_subscribe_group", detachable = "true")
public class NnUserSubscribeGroup implements PersistentModel {
    
    private static final long serialVersionUID = 1589646980279047445L;
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
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String name; //group name
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String imageUrl; //group image url
    
    @Persistent
    private short seq; //from 1 to 9
    
    //indicate it is a "free" set or not
    //free set means user can not touch this set and channels in the set will be changed by administrator    
    @Persistent
    private short status;
    public static final int STATUS_FREE = 0;
    public static final int STATUS_OCCUPIED = 1;
    
    @Persistent
    private short type;
    public static final short TYPE_USER = 1;
    public static final short TYPE_RO = 2;
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setSeq(short seq) {
        this.seq = seq;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    
    public Date getCreateDate() {
        return createDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public short getType() {
        return type;
    }
    
    public void setType(short type) {
        this.type = type;
    }
    
    public short getSeq() {
        return seq;
    }
    
    public void setStatus(short status) {
        this.status = status;
    }
    
}
