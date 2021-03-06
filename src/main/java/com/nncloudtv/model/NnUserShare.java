package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

/** 
 * Store user's sharing information, record channel and program info a user shares with others 
 */
@PersistenceCapable(table = "nnuser_share", detachable = "true")
public class NnUserShare implements PersistentModel {
    
    private static final long serialVersionUID = -8910431935780863764L;
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
    private long setId;
    
    @Persistent
    private long channelId;
    
    @Persistent
    private long programId; //program id from our own system
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String programIdStr; //youtube's program id 
        
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public long getSetId() {
        return setId;
    }
    
    public void setSetId(long setId) {
        this.setId = setId;
    }
    
    public long getChannelId() {
        return channelId;
    }
    
    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }
    
    public long getProgramId() {
        return programId;
    }
    
    public void setProgramId(long programId) {
        this.programId = programId;
    }
    
    public String getProgramIdStr() {
        return programIdStr;
    }
    
    public void setProgramIdStr(String programIdStr) {
        this.programIdStr = programIdStr;
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
