package com.nncloudtv.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.commons.lang.builder.ToStringBuilder;

@PersistenceCapable(table="systag", detachable="true")
public class SysTag implements Serializable {
    private static final long serialVersionUID = 6838197745642387197L;

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;

    @Persistent
    private long msoId;

    @Persistent
    private short type;
    public static final short TYPE_CATEGORY = 1;
    public static final short TYPE_SET = 2;
    public static final short TYPE_DAYPARTING = 3;
    public static final short TYPE_PREVIOUS = 4;    
    public static final short TYPE_SUBSCRIPTION = 5;
    public static final short TYPE_ACCOUNT = 6;
    public static final short TYPE_33SET = 7;
    public static final short TYPE_DESTROYED = 8;
    
    @Persistent
    private short sorting; // indicate how to sort SysTagMaps that belong to this SysTag
    public static final short SORT_SEQ = 1; //default
    public static final short SORT_DATE = 2;
    
    @Persistent
    private short seq;
    
    @Persistent
    private boolean featured; //for set

    @Persistent
    private short timeStart; //for dayparting

    @Persistent
    private short timeEnd; //for dayparting

    @Persistent
    @Column(jdbcType="VARCHAR", length=10)    
    private String attr;
    public static final short ATTR_APP_STACK = 0;
    public static final short ATTR_APP_SUBSCRIPTION = 1;
    public static final short ATTR_APP_ACCOUNT = 2;
    public static final short ATTR_APP_CHANNEL = 3;
    public static final short ATTR_APP_DIR = 4;
    public static final short ATTR_APP_SEARCH = 5;
    
    @Persistent 
    private Date createDate;
        
    @Persistent
    private Date updateDate;
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attr == null) ? 0 : attr.hashCode());
        result = prime * result
                + ((createDate == null) ? 0 : createDate.hashCode());
        result = prime * result + (featured ? 1231 : 1237);
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + (int) (msoId ^ (msoId >>> 32));
        result = prime * result + seq;
        result = prime * result + sorting;
        result = prime * result + timeEnd;
        result = prime * result + timeStart;
        result = prime * result + type;
        result = prime * result
                + ((updateDate == null) ? 0 : updateDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SysTag other = (SysTag) obj;
        if (attr == null) {
            if (other.attr != null)
                return false;
        } else if (!attr.equals(other.attr))
            return false;
        if (createDate == null) {
            if (other.createDate != null)
                return false;
        } else if (!createDate.equals(other.createDate))
            return false;
        if (featured != other.featured)
            return false;
        if (id != other.id)
            return false;
        if (msoId != other.msoId)
            return false;
        if (seq != other.seq)
            return false;
        if (sorting != other.sorting)
            return false;
        if (timeEnd != other.timeEnd)
            return false;
        if (timeStart != other.timeStart)
            return false;
        if (type != other.type)
            return false;
        if (updateDate == null) {
            if (other.updateDate != null)
                return false;
        } else if (!updateDate.equals(other.updateDate))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).
            append("id", id).
            append("msoId", msoId).
            append("type", type).
            append("sorting", sorting).
            append("seq", seq).
            append("featured", featured).
            append("timeStart", timeStart).
            append("timeEnd", timeEnd).
            append("attr", attr).
            append("createDate", createDate).
            append("updateDate", updateDate).
            toString();
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

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public short getSeq() {
        return seq;
    }

    public void setSeq(short seq) {
        this.seq = seq;
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

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
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

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public short getSorting() {
        return sorting;
    }

    public void setSorting(short sorting) {
        this.sorting = sorting;
    }
    
}
