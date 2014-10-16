package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table = "nnitem", detachable = "true")
public class NnItem {
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    @Persistent
    private long msoId;
    
    @Persistent
    private long channelId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String productIdRef; 
    
    @Persistent
    private short billingPlatform;
    public static final short UNKNOWN    = 0;
    public static final short GOOGLEPLAY = 1;
    public static final short APPSTORE   = 2;
    
    @Persistent
    private short status;
    public static final short ACTIVE     = 0;
    public static final short INACTIVE   = 1;
    public static final short PROCESSING = 2;
    public static final short REJECTED   = 3;
    public static final short WAIT_FOR_APPROVAL = 4;
    public static final short TERMINATED = 5;
    
    public NnItem(long msoId, long channelId, short billingPlatform) {
        
        this.msoId = msoId;
        this.channelId = channelId;
        this.billingPlatform = billingPlatform;
        this.status = WAIT_FOR_APPROVAL;
    }
    
	public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
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
    public String getProductIdRef() {
        return productIdRef;
    }
    
    public void setProductIdRef(String productIdRef) {
        this.productIdRef = productIdRef;
    }
    
    public short getBillingPlatform() {
        return billingPlatform;
    }
    
    public void setBillingPlatform(short billingPlatform) {
        this.billingPlatform = billingPlatform;
    }
    
    public short getStatus() {
        return status;
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

    public void setStatus(short status) {
        this.status = status;
    }
}
