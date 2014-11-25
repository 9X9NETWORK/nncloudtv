package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table = "ad_placement", detachable = "true")
public class AdPlacement extends PersistentModel {
    
    @Persistent
    long msoId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    String name;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    String url;
    
    @Persistent
    short status;
    public static final short ON = 0;
    public static final short OFF = 1;
    
    @Persistent
    short type;
    public static final short DIRECT = 0;
    public static final short VAST10 = 1;
    public static final short VAST20 = 2;
    
    @Persistent
    short seq;
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    @Persistent
    private Date starteDate;
    
    @Persistent
    private Date endDate;
    
    public AdPlacement(long msoId, String url, short type) {
        
        this.msoId = msoId;
        this.url = url;
        this.type = type;
        this.seq = 0;
        this.status = ON;
    }
    
    public long getMsoId() {
        return msoId;
    }
    
    public void setMsoId(long msoId) {
        this.msoId = msoId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public short getStatus() {
        return status;
    }
    
    public void setStatus(short status) {
        this.status = status;
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
    
    public Date getStarteDate() {
        return starteDate;
    }
    
    public void setStarteDate(Date starteDate) {
        this.starteDate = starteDate;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
}
