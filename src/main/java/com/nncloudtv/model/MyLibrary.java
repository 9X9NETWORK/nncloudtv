package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table = "my_library", detachable = "true")
public class MyLibrary implements PersistentModel {
    
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
    private long msoId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.SHORT_STRING_LENGTH)
    private String userIdStr; //format: shard-userId, example: 1-1
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String name;
    
    @Persistent
    private short contentType;
    public static final short CONTENTTYPE_DIRECTLINK = 0;
    public static final short CONTENTTYPE_YOUTUBE    = 1;
    public static final short CONTENTTYPE_SCRIPT     = 2;
    public static final short CONTENTTYPE_RADIO      = 3;
    public static final short CONTENTTYPE_REFERENCE  = 4;
    public static final short CONTENTTYPE_PROTECTED  = 5;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.LONG_STRING_LENGTH)
    private String intro;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String imageUrl;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String fileUrl;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String storageId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String errorCode;
    
    @Persistent
    private boolean isPublic;
    
    @Persistent
    private short status;
    //general
    public static short STATUS_OK             = 0;
    public static short STATUS_ERROR          = 1;
    public static short STATUS_NEEDS_REVIEWED = 2;
    //quality
    public static short STATUS_BAD_QUALITY    = 101;
    
    @Persistent
    private int duration;
    
    @Persistent
    private short seq;
    
    @Persistent
    private Date createDate;
        
    @Persistent
    private Date updateDate;
    
    public MyLibrary(Mso mso, NnUser user, String name, short contentType, String fileUrl) {
        
        this.name = name;
        this.contentType = contentType;
        this.fileUrl = fileUrl;
        this.isPublic = true;
        this.msoId = mso.getId();
        this.userIdStr = user.getIdStr();
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
    
    public String getIntro() {
        return intro;
    }
    
    public void setIntro(String intro) {
        this.intro = intro;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public short getStatus() {
        return status;
    }
    
    public void setStatus(short status) {
        this.status = status;
    }
    
    public String getStorageId() {
        return storageId;
    }
    
    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    
    public short getContentType() {
        return contentType;
    }
    
    public void setContentType(short contentType) {
        this.contentType = contentType;
    }
    
    public short getSeq() {
        return seq;
    }
    
    public void setSeq(short seq) {
        this.seq = seq;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public long getMsoId() {
        return msoId;
    }
    
    public void setMsoId(long msoId) {
        this.msoId = msoId;
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public String getUserIdStr() {
        return userIdStr;
    }
    
    public void setUserIdStr(String userIdStr) {
        this.userIdStr = userIdStr;
    }
}
