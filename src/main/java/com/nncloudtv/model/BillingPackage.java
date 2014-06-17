package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table = "billing_package", detachable = "true")
public class BillingPackage {
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    @Persistent
    private Date starteDate;
    
    @Persistent
    private Date endDate;
    
    @Persistent
    @Column(jdbcType = "VARCHAR", length = NnStringUtil.SHORT_STRING_LENGTH)
    private String name;
    
    @Persistent
    private int price;
    
    @Persistent
    private int setupFees; // for PERIODIC_CHARGE
    
    @Persistent
    private short status;
    public static final short PREPARING = 0;
    public static final short ONLINE    = 1;
    public static final short OFFLINE   = 2;
    public static final short RETIRED   = 3;
    
    @Persistent
    private short chargeType;
    public static final short ONE_TIME_CHARGE = 0;
    public static final short PERIODIC_CHARGE = 1;
    public static final short PAY_BY_USAGE    = 2;
    public static final short PREPAID         = 3;
    
    @Persistent
    private String chargeCycle;
    public static final String DAILY   = "1D";
    public static final String WEEKLY  = "1W";
    public static final String MONTHLY = "1M";
    
    @Persistent
    @Column(jdbcType="VARCHAR", length=NnStringUtil.LONG_STRING_LENGTH)
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getPrice() {
        return price;
    }
    
    public void setPrice(int price) {
        this.price = price;
    }
    
    public int getSetupFees() {
        return setupFees;
    }
    
    public void setSetupFees(int setupFees) {
        this.setupFees = setupFees;
    }
    
    public short getStatus() {
        return status;
    }
    
    public void setStatus(short status) {
        this.status = status;
    }
    
    public short getChargeType() {
        return chargeType;
    }
    
    public void setChargeType(short chargeType) {
        this.chargeType = chargeType;
    }
    
    public String getChargeCycle() {
        return chargeCycle;
    }
    
    public void setChargeCycle(String chargeCycle) {
        this.chargeCycle = chargeCycle;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
}
