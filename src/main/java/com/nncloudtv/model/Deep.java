package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.nncloudtv.lib.NnStringUtil;

/**
 * Used for shallow recommendation.
 * Data is from recommendation engine
 */
@PersistenceCapable(table = "deep", detachable = "true")
@Inheritance(customStrategy = "complete-table")
public class Deep extends PersistentModel {
    
    @Persistent
    private long userId;
    
    @Persistent
    private long msoId;
    
    @Persistent
    private short shard;
    
    //channel ids, separated by comma
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.LONG_STRING_LENGTH)
    private String recommendIds;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.VERY_SHORT_STRING_LENGTH)
    private String lang;
    
    @Persistent
    private Date updateDate;
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public short getShard() {
        return shard;
    }
    
    public void setShard(short shard) {
        this.shard = shard;
    }
    
    public String getRecommendIds() {
        return recommendIds;
    }
    
    public void setRecommendIds(String recommendIds) {
        this.recommendIds = recommendIds;
    }
    
    public String getLang() {
        return lang;
    }
    
    public void setLang(String lang) {
        this.lang = lang;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public long getMsoId() {
        return msoId;
    }
    
    public void setMsoId(long msoId) {
        this.msoId = msoId;
    }
}
