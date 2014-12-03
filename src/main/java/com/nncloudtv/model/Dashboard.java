package com.nncloudtv.model;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

/**
 * For android device. Things to display when device boots up 
 */
@PersistenceCapable(table = "dashboard", detachable = "true")
public class Dashboard implements PersistentBaseModel {
    
    private static final long serialVersionUID = -4730435228418723296L;
    private static final boolean cachable = false;
    
    public boolean isCachable() {
        return cachable;
    }
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    @Persistent
    private long msoId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String name;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String stackName;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String icon;
    
    @Persistent
    private short timeStart;
    
    @Persistent
    private short timeEnd;
    
    @Persistent
    private short opened;
    
    @Persistent
    private short type;
    public static final short TYPE_STACK = 0;
    public static final short TYPE_SUBSCRIPTION = 1;
    public static final short TYPE_ACCOUNT = 2;
    public static final short TYPE_CHANNEL = 3;
    public static final short TYPE_DIR = 4;
    public static final short TYPE_SEARCH = 5;
    
    //for stack with time, attr indicates the "previously on" id
    //for "previously on", attr is -1
    @Persistent
    private short attr; 
    
    @Persistent
    private short seq;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public short getSeq() {
        return seq;
    }
    
    public void setSeq(short seq) {
        this.seq = seq;
    }
    
    public short getTimeStart() {
        return timeStart;
    }
    
    public void setTimeStart(short timeStart) {
        this.timeStart = timeStart;
    }
    
    public short getTimeEnd() {
        return timeEnd;
    }
    
    public void setTimeEnd(short timeEnd) {
        this.timeEnd = timeEnd;
    }
    
    public short getOpened() {
        return opened;
    }
    
    public void setOpened(short opened) {
        this.opened = opened;
    }
    
    public short getType() {
        return type;
    }
    
    public void setType(short type) {
        this.type = type;
    }
    
    public String getStackName() {
        return stackName;
    }
    
    public void setStackName(String stackName) {
        this.stackName = stackName;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public short getAttr() {
        return attr;
    }
    
    public void setAttr(short attr) {
        this.attr = attr;
    }
    
    public long getMsoId() {
        return msoId;
    }
    
    public void setMsoId(long msoId) {
        this.msoId = msoId;
    }
    
}
