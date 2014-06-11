package com.nncloudtv.model.billing;

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
    private int paymentAmount;
    
    @Persistent
    private short paymentMechanism;
    public final static short OTHER         = 0;
    public final static short CLEARCOMMERCE = 1;
    public final static short PAYPAL        = 2;
    
    @Persistent
    private short paymentStatus;
    public final static short INITIAL   = 0;
    public final static short VERIFIED  = 1;
    public final static short PREAUTHED = 2;
    public final static short CAPTURED  = 3;
    public final static short SETTLED   = 4;
    public final static short CANCELED  = 5;
    public final static short RECURRING = 6;
    public final static short FINISHED  = 7;
    
    @Persistent
    private Date expiryDate;
    
}
