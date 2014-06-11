package com.nncloudtv.model.billing;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Transaction
 * 
 * @author louis
 * 
 */
@PersistenceCapable(table = "txn", detachable = "true")
public class Txn {
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    @Persistent
    private short type;
    public static final short UNKNWON     = 0;
    public static final short CC_VERIFY   = 1;
    public static final short CC_PREAUTH  = 2;
    public static final short CC_AUTH     = 3;
    public static final short CC_POSTAUTH = 4;
    public static final short CC_SETTLE   = 5;
    
    @Persistent
    private short status;
    public static final short INITIAL   = 0;
    public static final short COMPLETED = 1;
    public static final short FAILED    = 2;
    public static final short ACCEPTED  = 3;
    public static final short REJECTED  = 4;
    public static final short VOIDED    = 5;
    
    @Persistent
    private long orderId;
    
}
