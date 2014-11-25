package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.nncloudtv.lib.NnStringUtil;

/**
 * 9x9 Device account. It's not necessarily associated with 9x9 User account.
 */
@PersistenceCapable(table = "nndevice", detachable = "true")
@Inheritance(customStrategy = "complete-table")
public class NnDevice extends PersistentModel {
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String token; //each device has a unique token
    
    @Persistent
    private short shard; //which shard a user belongs to
    
    @Persistent
    private long userId; //if a device has associated user account, not always
    
    @Persistent
    private long msoId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String type;
    public static final String TYPE_FLIPR = "flipr";
    public static final String TYPE_APNS = "apns";
    public static final String TYPE_GCM = "gcm";
    
    @Persistent
    private int badge;
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    public NnDevice() {
        
    }
    
    public NnDevice(String token, long msoId, String type) {
        
        this.token = token;
        this.msoId = msoId;
        this.type = type;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public short getShard() {
        return shard;
    }
    
    public void setShard(short shard) {
        this.shard = shard;
    }
    
    public long getMsoId() {
        return msoId;
    }
    
    public void setMsoId(long msoId) {
        this.msoId = msoId;
    }
    
    public int getBadge() {
        return badge;
    }
    
    public void setBadge(int badge) {
        this.badge = badge;
    }
    
}
