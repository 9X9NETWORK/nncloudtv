package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/** 
 * Mso's default IPG, defining what channel is shown on the ipg
 */
@PersistenceCapable(table = "mso_ipg", detachable = "true")
public class MsoIpg implements PersistentModel {
    
    private static final long serialVersionUID = -3779766133873993236L;
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
    private long msoId; 
    
    @Persistent 
    private long channelId;
    
    @Persistent 
    private String groupName;
    
    public static short TYPE_GENERAL = 1;
    public static short TYPE_READONLY = 2;
    @Persistent    
    private short type;
    
    @Persistent
    private short seq; //the sequence in the ipg
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    public MsoIpg(long msoId, long channelId, short seq, short type) {
        this.msoId = msoId;
        this.channelId = channelId;
        this.seq = seq;
        this.type = type;
    }
    
    public long getMsoId() {
        return msoId;
    }
    
    public void setMsoId(long msoId) {
        this.msoId = msoId;
    }
    
    public long getChannelId() {
        return channelId;
    }
    
    public void setChannelId(long channelId) {
        this.channelId = channelId;
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
    
    public void setSeq(short seq) {
        this.seq = seq;
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
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }    
}
