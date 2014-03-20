package com.nncloudtv.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.commons.lang.builder.ToStringBuilder;

@PersistenceCapable(table="systag_display", detachable="true")
public class SysTagDisplay implements Serializable {

    private static final long serialVersionUID = 180961281773849098L;

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;

    @Persistent
    private long systagId;
    
    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String name;
    
    @Persistent
    @Column(jdbcType="VARCHAR", length=5)
    private String lang; //used with LangTable

    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String imageUrl; //currently episode thumbnail

    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String imageUrl2; //currently channel thumbnail
    
    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String bannerImageUrl; //banner basic resolution

    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String bannerImageUrl2; //banner retina resolution
    
    @Persistent
    @Column(jdbcType="VARCHAR", length=500)
    private String popularTag; //sequence shown in the directory

    @Persistent
    private int cntChannel;
    
    @Persistent
    private Date updateDate;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((bannerImageUrl == null) ? 0 : bannerImageUrl.hashCode());
        result = prime * result
                + ((bannerImageUrl2 == null) ? 0 : bannerImageUrl2.hashCode());
        result = prime * result + cntChannel;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result
                + ((imageUrl == null) ? 0 : imageUrl.hashCode());
        result = prime * result
                + ((imageUrl2 == null) ? 0 : imageUrl2.hashCode());
        result = prime * result + ((lang == null) ? 0 : lang.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((popularTag == null) ? 0 : popularTag.hashCode());
        result = prime * result + (int) (systagId ^ (systagId >>> 32));
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
        SysTagDisplay other = (SysTagDisplay) obj;
        if (bannerImageUrl == null) {
            if (other.bannerImageUrl != null)
                return false;
        } else if (!bannerImageUrl.equals(other.bannerImageUrl))
            return false;
        if (bannerImageUrl2 == null) {
            if (other.bannerImageUrl2 != null)
                return false;
        } else if (!bannerImageUrl2.equals(other.bannerImageUrl2))
            return false;
        if (cntChannel != other.cntChannel)
            return false;
        if (id != other.id)
            return false;
        if (imageUrl == null) {
            if (other.imageUrl != null)
                return false;
        } else if (!imageUrl.equals(other.imageUrl))
            return false;
        if (imageUrl2 == null) {
            if (other.imageUrl2 != null)
                return false;
        } else if (!imageUrl2.equals(other.imageUrl2))
            return false;
        if (lang == null) {
            if (other.lang != null)
                return false;
        } else if (!lang.equals(other.lang))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (popularTag == null) {
            if (other.popularTag != null)
                return false;
        } else if (!popularTag.equals(other.popularTag))
            return false;
        if (systagId != other.systagId)
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
            append("systagId", systagId).
            append("name", name).
            append("lang", lang).
            append("imageUrl", imageUrl).
            append("imageUrl2", imageUrl2).
            append("bannerImageUrl", bannerImageUrl).
            append("bannerImageUrl2", bannerImageUrl2).
            append("popularTag", popularTag).
            append("cntChannel", cntChannel).
            append("updateDate", updateDate).
            toString();
    }

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
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
