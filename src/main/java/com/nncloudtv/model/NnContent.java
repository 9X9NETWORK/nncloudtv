package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import com.nncloudtv.lib.NnStringUtil;

/** 
 * For website's dynamic content, example would be entries in FAQ.
 * Used as key/value pair.
 */
@PersistenceCapable(table = "nncontent", detachable = "true")
public class NnContent extends PersistentModel {
    
    @Persistent
    private long msoId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String item;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.LONGVARCHAR, length = NnStringUtil.LONGVARCHAR_LENGTH) // TEXT?
    private String value;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.VERY_SHORT_STRING_LENGTH)
    private String lang;
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    public NnContent() {}
    public NnContent(String item, String value, String lang, long msoId) {
        this.item = item;
        this.value = value;
        this.lang = lang;
        this.msoId = msoId;
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
    
    public String getLang() {
        return lang;
    }
    
    public void setLang(String lang) {
        this.lang = lang;
    }
    
    public long getMsoId() {
        return msoId;
    }
    
    public void setMsoId(long msoId) {
        this.msoId = msoId;
    }
    
}
