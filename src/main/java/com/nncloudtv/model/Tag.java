package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

/**
 * tag
 */
@PersistenceCapable(table = "tag", detachable = "true")
public class Tag implements PersistentModel {
    
    private static final long serialVersionUID = -8803012032455804100L;
    private static final boolean cachable = true;
    
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
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String name;
    
    //special tag name for internal use
    public static final String RECOMMEND = "recommend";
    public static final String HOT = "hot";
    public static final String HOT_EN = "hot(9x9en)";
    public static final String HOT_ZH = "hot(9x9zh)";
    public static final String FEATURED = "featured";
    public static final String FEATURED_EN = "featured(9x9en)";
    public static final String FEATURED_ZH = "featured(9x9zh)";
    public static final String TRENDING = "trending";
    public static final String TRENDING_EN = "trending(9x9en)";
    public static final String TRENDING_ZH = "trending(9x9zh)";
    
    @Persistent
    private Date updateDate;
    
    public Tag(String name) {
        this.name = name;
        this.updateDate = new Date();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
}
