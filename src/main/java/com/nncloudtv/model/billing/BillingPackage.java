package com.nncloudtv.model.billing;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(table = "billing_package", detachable = "true")
public class BillingPackage {
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    @Persistent
    @Column(jdbcType="VARCHAR", length=100)
    private String name;
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    @Persistent
    private Date starteDate;
    
    @Persistent
    private Date endDate;
    
    @Persistent
    private int price;
    
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
    
}
