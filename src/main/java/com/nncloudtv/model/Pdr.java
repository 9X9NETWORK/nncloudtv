package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table = "pdr", detachable = "true")
@Inheritance(customStrategy = "complete-table")
public class Pdr extends PersistentModel {
    
    @Persistent
    private long userId;
    
    //when looking up NnUser, token and userId should find the same user 
    //for easier lookup        
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String userToken;
    
    @Persistent
    private long deviceId;
    
    //when looking up NnDevice, token and deviceId should find the same device 
    //for easier lookup        
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String deviceToken;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String session;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String ip;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.LONGVARCHAR, length = NnStringUtil.LONGVARCHAR_LENGTH)
    private String detail;    
    
    @Persistent
    private Date updateDate;
    
    public Pdr(long userId, String session, String detail) {
        this.userId = userId;
        this.session = session;
        this.detail = detail;
    }
    
    public Pdr (NnUser user, NnDevice device, String session, String detail) {
        this.session = session;
        if (user != null) {
            this.userId = user.getId();
            this.userToken = user.getToken();
        }
        if (device != null) {
            this.deviceId = device.getId();
            this.deviceToken = device.getToken();
        }
        this.detail = detail;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public String getDetail() {
        return detail;
    }
    
    public void setDetail(String detail) {
        this.detail = detail;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
    public String getSession() {
        return session;
    }
    
    public void setSession(String session) {
        this.session = session;
    }
    
    public String getUserToken() {
        return userToken;
    }
    
    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
    
    public long getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getDeviceToken() {
        return deviceToken;
    }
    
    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
}
