package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

/**
 * 9x9 user preference, stored in key/value pair
 */
@PersistenceCapable(table = "nnchannel_pref", detachable = "true")
public class NnChannelPref implements PersistentModel {
    
    private static final long serialVersionUID = -9159364675960624271L;
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
    private long channelId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String item;
    
    public static final String FB_AUTOSHARE    = "fb-autoshare";
    public static final String BRAND_AUTOSHARE = "brand-autoshare"; // indicate which brand the channel sharing at, the value is mso's name
    public static final String AUTO_SYNC       = "auto-sync";       // indicate YouTube-sync-channel is auto sync to YouTube
    public static final String SOCIAL_FEEDS    = "social-feeds";    // channel related social network information, refer to MsoConfig.SOCIAL_FEEDS
    public static final String BANNER_IMAGE    = "banner-image";    // banner image url
    // In-app purchase information
    public static final String IAP_TITLE       = "iap-title";
    public static final String IAP_DESC        = "iap-desc";
    public static final String IAP_PRICE       = "iap-price";
    public static final String IAP_THUMB       = "iap-thumb";
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String value;
    
    public static final String ON     = "on";
    public static final String OFF    = "off";
    public static final String FAILED = "failed";
    
    public NnChannelPref() {
        
    }
    
    public NnChannelPref(Long channelId, String item, String value) {
        this.channelId = channelId;
        this.item = item;
        this.value = value;
    }
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    public long getChannelId() {
        return channelId;
    }
    
    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }
    
    public String getItem() {
        return item;
    }
    
    public void setItem(String item) {
        this.item = item;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
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
}
