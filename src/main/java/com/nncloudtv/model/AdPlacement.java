package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table = "ad_placement", detachable = "true")
public class AdPlacement {
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    long id;
    
    @Persistent
    long msoId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    String name;
    
    @PrimaryKey
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    String url;
    
    @Persistent
    short status;
    public static final short ON = 0;
    public static final short OFF = 1;
    
    @PrimaryKey
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
    
}
