package com.nncloudtv.web.json.cms;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.nncloudtv.model.SysTag;

public class Set implements Serializable {
    
    /**
     * eclipse generated
     */
    private static final long serialVersionUID = -4777307952253124679L;

    private long id;
    
    private long msoId;
    
    private long displayId;
    
    private int channelCnt;
    
    private String lang;
    
    private short seq;
    
    private String tag;
    
    private String name;
    
    private short sortingType;
    public static final short SORT_DEFAULT = SysTag.SORT_SEQ;
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + channelCnt;
        result = prime * result + (int) (displayId ^ (displayId >>> 32));
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((lang == null) ? 0 : lang.hashCode());
        result = prime * result + (int) (msoId ^ (msoId >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + seq;
        result = prime * result + sortingType;
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
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
        Set other = (Set) obj;
        if (channelCnt != other.channelCnt)
            return false;
        if (displayId != other.displayId)
            return false;
        if (id != other.id)
            return false;
        if (lang == null) {
            if (other.lang != null)
                return false;
        } else if (!lang.equals(other.lang))
            return false;
        if (msoId != other.msoId)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (seq != other.seq)
            return false;
        if (sortingType != other.sortingType)
            return false;
        if (tag == null) {
            if (other.tag != null)
                return false;
        } else if (!tag.equals(other.tag))
            return false;
        return true;
    }

    public String toString() {
        return new ToStringBuilder(this).
            append("id", id).
            append("msoId", msoId).
            append("channelCnt", channelCnt).
            append("lang", lang).
            append("seq", seq).
            append("tag", tag).
            append("name", name).
            append("sortingType", sortingType).
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

    public int getChannelCnt() {
        return channelCnt;
    }

    public void setChannelCnt(int channelCnt) {
        this.channelCnt = channelCnt;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public short getSeq() {
        return seq;
    }

    public void setSeq(short seq) {
        this.seq = seq;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getSortingType() {
        return sortingType;
    }

    public void setSortingType(short sortingType) {
        this.sortingType = sortingType;
    }

    public long getDisplayId() {
        return displayId;
    }

    public void setDisplayId(long displayId) {
        this.displayId = displayId;
    }
    
}
