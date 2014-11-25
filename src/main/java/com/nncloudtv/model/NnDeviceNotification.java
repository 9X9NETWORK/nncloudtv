package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.nncloudtv.lib.NnStringUtil;

/**
 * Store Pushed notifications. 
 */
@PersistenceCapable(table = "nndevice_notification", detachable = "true")
@Inheritance(customStrategy = "complete-table")
public class NnDeviceNotification extends PersistentModel {
    
    @Persistent
    private long deviceId;
    
    @Persistent
    private boolean read;
    
    public NnDeviceNotification(long deviceId, String message) {
        
        this.deviceId = deviceId;
        this.message = message;
        this.read = false;
    }
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String message;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String content;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String logo;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String title;
    
    @Persistent
    private short type;
    public static final short TYPE_PUSH_NOTIFICATION = 0;
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    public long getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }
    
    public short getSort() {
        return type;
    }
    
    public void setSort(short sort) {
        this.type = sort;
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
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getLogo() {
        return logo;
    }
    
    public void setLogo(String logo) {
        this.logo = logo;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTimeStamp() {
        return (createDate == null) ? null : String.valueOf(createDate.getTime());
    }
}
