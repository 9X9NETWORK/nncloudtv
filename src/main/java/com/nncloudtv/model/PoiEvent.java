package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table = "poi_event", detachable = "true")
@Inheritance(customStrategy = "complete-table")
public class PoiEvent extends PersistentModel {
    
    @Persistent
    private long userId; // will be replaced by profileId
    
    @Persistent
    private long msoId; // will be replaced by profileId
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String name;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String notifyMsg;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String notifyScheduler; //timestamp list, separate by comma    
    
    @Persistent
    private short type;
    public static final short TYPE_POPUP = 0;
    public static final short TYPE_HYPERLINK = 1;
    public static final short TYPE_INSTANTNOTIFICATION = 2;
    public static final short TYPE_SCHEDULEDNOTIFICATION = 3;
    public static final short TYPE_POLL = 4;
    
    /**
     * json format, example
     * 
     * {
     *   message: "更多壹傳媒內幕,盡在'媒體停看聽'",
     *   button: [
     *             {text: "了解更多", actionUrl: "http://www.9x9.tv/view?ch=1380&ep=6789"}
     *           ]
     * } 
     */
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.VERY_LONG_STRING_LENGTH)
    private String context;    
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String message; //response message   
    
    @Persistent
    private Date createDate;
        
    @Persistent
    private Date updateDate;
    
    public short getType() {
        return type;
    }
    
    public void setType(short type) {
        this.type = type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public String getHyperChannelText() {
        if (context != null) {
            String[] splits = context.split("\\|");
            if (splits.length > 1)
                return splits[1];
        }
        return null;
    }
    
    public String getHyperChannelLink() {
        if (context != null) {
            String[] splits = context.split("\\|");
            if (splits.length > 1)
                return splits[0];
        }
        return null;
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
    
    public long getUserId() {
        return userId;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getNotifyMsg() {
        return notifyMsg;
    }
    
    public void setNotifyMsg(String notifyMsg) {
        this.notifyMsg = notifyMsg;
    }
    
    public String getNotifyScheduler() {
        return notifyScheduler;
    }
    
    public void setNotifyScheduler(String notifyScheduler) {
        this.notifyScheduler = notifyScheduler;
    }
    
}
