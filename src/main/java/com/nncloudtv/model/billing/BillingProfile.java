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
    
}
