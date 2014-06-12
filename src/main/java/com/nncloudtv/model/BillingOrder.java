package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(table = "billing_order", detachable = "true")
public class BillingOrder {
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    @Persistent
    private long packageId;
    
    @Persistent
    private long profileId;
    
    @Persistent
    private long itemId; // Can be any ID - msoId, userId, subscriptionId, channelId ... 
    
    @Persistent
    private int totalPaymentAmount;
    
    @Persistent
    private int cntPayment;
    
    @Persistent
    private short paymentMechanism;
    public final static short OTHER         = 0;
    public final static short CLEARCOMMERCE = 1;
    public final static short PAYPAL        = 2;
    
    @Persistent
    private short status;
    public final static short INITIAL    = 0;
    public final static short VERIFIED   = 1;
    public final static short PREAUTHED  = 2;
    public final static short CAPTURED   = 3;
    public final static short SETTLED    = 4;
    public final static short CANCELED   = 5;
    public final static short RECURRING  = 6;
    public final static short TERMINATED = 7;
    public final static short CLOSED     = 8;
    public final static short ERROR      = 9;
    
    @Persistent
    private Date expiryDate;
    
    @Persistent
    @Column(jdbcType = "TEXT")
    private String note;
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
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
    
    public long getPackageId() {
        return packageId;
    }
    
    public void setPackageId(long packageId) {
        this.packageId = packageId;
    }
    
    public long getProfileId() {
        return profileId;
    }
    
    public void setProfileId(long profileId) {
        this.profileId = profileId;
    }
    
    public long getItemId() {
        return itemId;
    }
    
    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
    
    public int getTotalPaymentAmount() {
        return totalPaymentAmount;
    }
    
    public void setTotalPaymentAmount(int totalPaymentAmount) {
        this.totalPaymentAmount = totalPaymentAmount;
    }
    
    public int getCntPayment() {
        return cntPayment;
    }
    
    public void setCntPayment(int cntPayment) {
        this.cntPayment = cntPayment;
    }
    
    public short getPaymentMechanism() {
        return paymentMechanism;
    }
    
    public void setPaymentMechanism(short paymentMechanism) {
        this.paymentMechanism = paymentMechanism;
    }
    
    public short getStatus() {
        return status;
    }
    
    public void setStatus(short status) {
        this.status = status;
    }
    
    public Date getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
}
