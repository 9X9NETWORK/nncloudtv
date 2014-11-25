package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

/** 
 * User's problem reporting. 
 */
@PersistenceCapable(table = "nnuser_report", detachable = "true")
public class NnUserReport implements PersistentModel {
    
    private static final long serialVersionUID = 6423984464587227432L;
    private static final boolean cachable = true;
    
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
    private String type;
    public static String TYPE_PROBLEM = "problem";
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.LONG_STRING_LENGTH)
    private String comment; //use with item as key/value pair
    
    //session defined by the player, it's the same as PdrRaw session. 
    //to associate user's report and our logging data.
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String session;
    
    @Persistent
    private Date createDate;
    
    public NnUserReport() {}
    
    public NnUserReport(NnUser user, NnDevice device, String session, String type, String comment) {
        if (user != null) {
            this.userId = user.getId();
            this.userToken = user.getToken();
        }
        if (device != null) {
            this.deviceId = device.getId();
            this.deviceToken = device.getToken();
        }
        this.session = session;
        this.comment = comment;
        if (type == null)
            this.setType(NnUserReport.TYPE_PROBLEM);
        else
            this.setType(type);
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getSession() {
        return session;
    }
    
    public void setSession(String session) {
        this.session = session;
    }
    
    public Date getCreateDate() {
        return createDate;
    }
    
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }    
}
