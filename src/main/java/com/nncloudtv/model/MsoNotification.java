package com.nncloudtv.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * PCS Push Notification
 */
@PersistenceCapable(table="mso_notification", detachable="true")
public class MsoNotification implements Serializable {
   
    private static final long serialVersionUID = -1574784862238151019L;

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;

    @Persistent
    private long msoId;
   
    @Persistent
    private Date scheduleDate;
    
    @Persistent
    private Date publishDate;

    @Persistent
    private Date updateDate;

    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String message;

    @Persistent
    @Column(jdbcType="VARCHAR", length=255)
    private String url;

    public long getMsoId() {
      return msoId;
    }

    public void setMsoId(long msoId) {
       this.msoId = msoId;
    }
    
    public long getId() {
       return id;
    }
    
    public void setId(long id) {
       this.id = id;
    }
    public Date getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(Date scheduleDate) {
        this.scheduleDate = scheduleDate;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
