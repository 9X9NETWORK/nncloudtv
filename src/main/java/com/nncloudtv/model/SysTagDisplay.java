package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table = "systag_display", detachable = "true")
public class SysTagDisplay implements PersistentBaseModel {
    
    private static final long serialVersionUID = 7258016865351679922L;
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
    long systagId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    String name;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.VERY_SHORT_STRING_LENGTH)
    String lang; //used with LangTable
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    String imageUrl; //currently episode thumbnail
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    String imageUrl2; //currently channel thumbnail
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    String bannerImageUrl; //banner basic resolution
    public static final int DEFAULT_WIDTH  = 800;
    public static final int DEFAULT_HEIGHT = 244;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    String bannerImageUrl2; //banner retina resolution
    public static final int RETINA_WIDTH  = 1536;
    public static final int RETINA_HEIGHT = 570;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.EXTENDED_STRING_LENGTH)
    String popularTag; //sequence shown in the directory
    
    @Persistent
    int cntChannel;
    
    @Persistent
    Date updateDate;
    
    public String getBannerImageUrl() {
        return bannerImageUrl;
    }
    
    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }
    
    public String getBannerImageUrl2() {
        return bannerImageUrl2;
    }
    
    public void setBannerImageUrl2(String bannerImageUrl2) {
        this.bannerImageUrl2 = bannerImageUrl2;
    }
    
    public long getSystagId() {
        return systagId;
    }
    
    public void setSystagId(long systagId) {
        this.systagId = systagId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLang() {
        return lang;
    }
    
    public void setLang(String lang) {
        this.lang = lang;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
    public String getPopularTag() {
        return popularTag;
    }
    
    public void setPopularTag(String popularTag) {
        this.popularTag = popularTag;
    }
    
    public int getCntChannel() {
        return cntChannel;
    }
    
    public void setCntChannel(int cntChannel) {
        this.cntChannel = cntChannel;
    }
    
    public String getImageUrl2() {
        return imageUrl2;
    }
    
    public void setImageUrl2(String imageUrl2) {
        this.imageUrl2 = imageUrl2;
    }
    
}
