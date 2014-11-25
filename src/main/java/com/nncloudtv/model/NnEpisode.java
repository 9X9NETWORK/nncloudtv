package com.nncloudtv.model;

import java.util.Date;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.service.CounterFactory;

@PersistenceCapable(table = "nnepisode", detachable = "true")
public class NnEpisode extends PersistentModel {
    
    @Persistent
    private long channelId;
    
    /**
     * The usage of episode `storageId`
     * 
     * 1. orphan episode:     store channelId (at the meanwhile channelId=0)
     * 2. referenced episode: store referenced episodeId
     * 3. virtual channel:    store real channelId (temporarily used in composeEachEpisodeInfo())
     */
    @Persistent
    private long storageId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String name;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String imageUrl;
    public static final short DEFAULT_WIDTH = 720;
    public static final short DEFAULT_HEIGHT = 405;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.VERY_LONG_STRING_LENGTH)
    private String intro;
    
    @Persistent
    private boolean isPublic;
    
    @Persistent
    public short contentType;
    public static final short CONTENTTYPE_GENERAL  = 0;
    public static final short CONTENTTYPE_UPLOADED = 5; // this episdoe contains only uploaded video (for CMS)
    
    @Persistent
    private Date scheduleDate;
    
    @Persistent
    private Date publishDate;
        
    @Persistent
    private Date updateDate;
    
    // TODO create DB field
    @NotPersistent
    private Date createDate;
    
    @NotPersistent
    private long cntView;
    
    @NotPersistent
    private String playbackUrl;
    
    @Persistent
    private int duration;
    
    @Persistent
    private int seq;
    
    public NnEpisode(long channelId) {
        this.channelId = channelId;
        Date now = NnDateUtil.now();
        this.updateDate = now;
        this.publishDate = now;
    } 
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
    
    public void setIntro(String intro) {
        this.intro = intro;
    }
    
    public Date getPublishDate() {
        return publishDate;
    }
    
    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
    public long getChannelId() {
        return channelId;
    }
    
    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public boolean getIsPublic() {
        return isPublic;
    }
    
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public int getSeq() {
        return seq;
    }
    
    public void setSeq(int seq) {
        this.seq = seq;
    }
    
    public long getCntView() {
        //v_ch10514_e21688
        String cacheName = "u_ch" + channelId + "_e" + id;
        try {
            String result = (String) CacheFactory.get(cacheName);
            if (result != null) {
                return Integer.parseInt(result);
            }
            cntView = CounterFactory.getCount(cacheName);
        } catch (Exception e) {
            NnLogUtil.logException(e);
            cntView = 0;
        }
        CacheFactory.set(cacheName, String.valueOf(cntView));
        return cntView;
    }
    
    public int getDuration() {
    
        return duration;
    }
    
    public void setDuration(int duration) {
    
        this.duration = duration;
    }
    
    public String getPlaybackUrl() {
    
        return playbackUrl;
    }
    
    public void setPlaybackUrl(String playbackUrl) {
    
        this.playbackUrl = playbackUrl;
    }
    
    public Date getScheduleDate() {
        return scheduleDate;
    }
    
    public void setScheduleDate(Date scheduleDate) {
        this.scheduleDate = scheduleDate;
    }
    
    public long getStorageId() {
        return storageId;
    }
    
    public void setStorageId(long storageId) {
        this.storageId = storageId;
    }
    
    public short getContentType() {
        return contentType;
    }
    
    public void setContentType(short contentType) {
        this.contentType = contentType;
    }
    
    public Date getCreateDate() {
        return createDate;
    }
    
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
     
}
