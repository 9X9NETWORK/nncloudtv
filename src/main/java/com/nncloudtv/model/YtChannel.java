package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table = "ytchannel", detachable = "true")
public class YtChannel implements PersistentModel {
    
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
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.EXTENDED_STRING_LENGTH)
    private String name; //the name we define, versus oriName
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.EXTENDED_STRING_LENGTH)
    private String intro;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String imageUrl; 
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.VERY_SHORT_STRING_LENGTH)
    private String sphere; //used with LangTable
    
    @Persistent
    private int cntEpisode; //episode count
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.EXTENDED_STRING_LENGTH)
    private String sourceUrl; //unique if not null
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.EXTENDED_STRING_LENGTH)
    private String tag;
    
    @Persistent
    public short contentType;
    public static final short CONTENTTYPE_YOUTUBE_CHANNEL = 3;
    public static final short CONTENTTYPE_YOUTUBE_PLAYLIST = 4;
        
    @Persistent
    private short status;
        
    @Persistent
    private Date updateDate;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getIntro() {
        return intro;
    }
    
    public void setIntro(String intro) {
        this.intro = intro;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getSphere() {
        return sphere;
    }
    
    public void setSphere(String sphere) {
        this.sphere = sphere;
    }
    
    public int getCntEpisode() {
        return cntEpisode;
    }
    
    public void setCntEpisode(int cntEpisode) {
        this.cntEpisode = cntEpisode;
    }
    
    public String getSourceUrl() {
        return sourceUrl;
    }
    
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
    
    public String getTag() {
        return tag;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    public short getContentType() {
        return contentType;
    }
    
    public void setContentType(short contentType) {
        this.contentType = contentType;
    }
    
    public short getStatus() {
        return status;
    }
    
    public void setStatus(short status) {
        this.status = status;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
}
