package com.nncloudtv.model.billing;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table="billing_profile", detachable="true")
public class BillingProfile {
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    @Persistent
    private short cardStatus;
    public static final short GOOD = 0;
    public static final short FRAUD = 1;
    
    @Persistent
    @Column(jdbcType = "VARCHAR", length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String cardHolderName;
    
    @Persistent
    @Column(jdbcType = "VARCHAR", length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String cardRemainDigits;
    
    @Persistent
    @Column(jdbcType = "VARCHAR", length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String email;
    
    @Persistent
    @Column(jdbcType = "VARCHAR", length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String name;
    
    @Persistent
    @Column(jdbcType = "VARCHAR", length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String addr1;
    
    @Persistent
    @Column(jdbcType = "VARCHAR", length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String addr2;
    
    @Persistent
    @Column(jdbcType = "VARCHAR", length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String city;
    
    @Persistent
    @Column(jdbcType = "VARCHAR", length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String state;
    
    @Persistent
    @Column(jdbcType = "VARCHAR", length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String zip;
    
    @Persistent
    @Column(jdbcType = "VARCHAR", length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String country;
    
    @Persistent
    @Column(jdbcType = "VARCHAR", length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String phone;
    
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
    
    public short getCardStatus() {
        return cardStatus;
    }
    
    public void setCardStatus(short cardStatus) {
        this.cardStatus = cardStatus;
    }
    
    public String getCardHolderName() {
        return cardHolderName;
    }
    
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }
    
    public String getCardRemainDigits() {
        return cardRemainDigits;
    }
    
    public void setCardRemainDigits(String cardRemainDigits) {
        this.cardRemainDigits = cardRemainDigits;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddr1() {
        return addr1;
    }
    
    public void setAddr1(String addr1) {
        this.addr1 = addr1;
    }
    
    public String getAddr2() {
        return addr2;
    }
    
    public void setAddr2(String addr2) {
        this.addr2 = addr2;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getZip() {
        return zip;
    }
    
    public void setZip(String zip) {
        this.zip = zip;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
}
