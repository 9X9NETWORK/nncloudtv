package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.nncloudtv.lib.NnStringUtil;

/** 
 * Store user's last watched program of each channel
 */
@PersistenceCapable(table = "nnuser_watched", detachable = "true")
@Inheritance(customStrategy = "complete-table")
public class NnUserWatched extends PersistentModel {
    
    @Persistent
    private long userId;
    
    @Persistent
    private long msoId;
    
    @Persistent
    private long channelId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String program; //it can be a 9x9 program id or youtube program id (not number)
    
    @Persistent
    private Date updateDate;
    
    public NnUserWatched(NnUser user, long channelId, String program) {
        this.msoId = user.getMsoId();
        this.userId = user.getId();
        this.channelId = channelId;
        this.program = program;
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
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
    public String getProgram() {
        return program;
    }
    
    public void setProgram(String program) {
        this.program = program;
    }
    
    public long getMsoId() {
        return msoId;
    }
    
    public void setMsoId(long msoId) {
        this.msoId = msoId;
    }
    
}
