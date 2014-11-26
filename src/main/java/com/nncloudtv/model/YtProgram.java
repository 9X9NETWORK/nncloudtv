package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

/**
 * Used by android device, essentially simplified version of nnprogram. Will see how it goes, maybe will be merged to nnprogram somehow.
 * They are data crawled from YouTube.
 */
@PersistenceCapable(table = "ytprogram", detachable = "true")
public class YtProgram implements PersistentModel {
    
    private static final long serialVersionUID = -8000687567807163404L;
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
    private long channelId;
    
    @Persistent 
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String ytUserName;
    
    @Persistent 
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String ytVideoId;
    
    @Persistent 
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String name;
    
    @Persistent 
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String duration;
    
    @Persistent 
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String imageUrl;
    
    @Persistent 
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String intro;
    
    @Persistent
    private Date crawlDate;
    
    @Persistent
    private Date updateDate;
    
    public YtProgram() {}
    public YtProgram(long channelId, String ytUserName, String ytVideoId, 
                     String name, String duration, String imageUrl, 
                     String intro, Date crawlDate, Date updateDate) {
        this.channelId = channelId;
        this.ytUserName = ytUserName;
        this.ytVideoId = ytVideoId;
        if (name != null) {
            int len = (name.length() > 255 ? 255 : name.length()); 
            name = name.replaceAll("\\s", " ");                
            name = name.substring(0, len);           
        }        
        this.name = name;        
        this.duration = duration;
        this.imageUrl = imageUrl;
        if (intro != null) {
            int len = (intro.length() > 255 ? 255 : intro.length()); 
            intro = intro.replaceAll("\\s", " ");                
            intro = intro.substring(0, len);           
        }        
        this.intro = intro;        
        this.crawlDate = crawlDate;
        this.updateDate = updateDate;
    }
    
    public long getChannelId() {
        return channelId;
    }
    
    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }
    
    public String getYtUserName() {
        return ytUserName;
    }
    
    public void setYtUserName(String ytUserName) {
        this.ytUserName = ytUserName;
    }
    
    public String getYtVideoId() {
        return ytVideoId;
    }
    
    public void setYtVideoId(String ytVideoId) {
        this.ytVideoId = ytVideoId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPlayerName() {
        String name = this.getName();
        if (name != null) {
            name = name.replace("|", "\\|");
            name = name.replaceAll("\\s", " ");
        }
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getIntro() {
        return intro;
    }
    
    public String getPlayerIntro() {
        String pintro = this.getIntro();
        if (pintro != null) {
            int len = (pintro.length() > 256 ? 256 : pintro.length());
            pintro = pintro.replaceAll("\\s", " ");
            pintro = pintro.substring(0, len);
        }
        return pintro;
    }
    
    public void setIntro(String intro) {
        this.intro = intro;
    }
    
    public Date getCrawlDate() {
        return crawlDate;
    }
    
    public void setCrawlDate(Date crawlDate) {
        this.crawlDate = crawlDate;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
}
