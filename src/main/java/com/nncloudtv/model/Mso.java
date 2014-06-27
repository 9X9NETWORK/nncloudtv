package com.nncloudtv.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.*;

import com.nncloudtv.lib.NnStringUtil;

/**
 * a Multimedia service operator
 */
@PersistenceCapable(table="mso", detachable="true")
public class Mso implements Serializable {

    private static final long serialVersionUID = 352047930355952392L;
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String name; //name is unique, used as unique nameId, be careful of the case
    public static String NAME_9X9 = "9x9";
    public static String NAME_CTS = "cts";
    public static String NAME_5F  = "5f";
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String title;
    
    @Persistent 
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String intro;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String logoUrl;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String jingleUrl;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String contactEmail;
    
    @Persistent
    private short type;
    public static final short TYPE_NN         = 1; //default mso, must have and must have ONLY one
    public static final short TYPE_MSO        = 2;
    public static final short TYPE_3X3        = 3;
    public static final short TYPE_TCO        = 4; // for Generic CMS
    public static final short TYPE_ENTERPRISE = 5; // brand, US only
    public static final short TYPE_DEPRECATED = 6; // the mso that no longer used
    public static final short TYPE_FANAPP     = 7;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.VERY_SHORT_STRING_LENGTH)
    private String lang;
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    // the value comes from MsoConfig's SUPPORTED_REGION
    @NotPersistent
    private String supportedRegion;
    
    // The value of maximum number of sets each MSO
    @NotPersistent
    private short maxSets;
    
    // The maximum number of channels per set
    @NotPersistent
    private short maxChPerSet;
    
    @NotPersistent
    private boolean apnsEnabled;
    
    @NotPersistent
    private boolean gcmEnabled;
    
    public Mso(String name, String intro, String contactEmail, short type) {
        this.name = name;
        this.intro = intro;
        this.contactEmail = contactEmail;
        this.type = type;
    }
    
    public String getSupportedRegion() {
        return supportedRegion;
    }
    
    public void setSupportedRegion(String supportedRegion) {
        this.supportedRegion = supportedRegion;
    }
    
    public short getMaxSets() {
        return maxSets;
    }
    
    public void setMaxSets(short maxSets) {
        this.maxSets = maxSets;
    }
    
    public short getMaxChPerSet() {
        return maxChPerSet;
    }
    
    public void setMaxChPerSet(short maxChPerSet) {
        this.maxChPerSet = maxChPerSet;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

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

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getJingleUrl() {
        return jingleUrl;
    }

    public void setJingleUrl(String jingleUrl) {
        this.jingleUrl = jingleUrl;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean isApnsEnabled() {
        return apnsEnabled;
    }

    public void setApnsEnabled(boolean pushApnsEnabled) {
        this.apnsEnabled = pushApnsEnabled;
    }

    public boolean isGcmEnabled() {
        return gcmEnabled;
    }

    public void setGcmEnabled(boolean gcmEnabled) {
        this.gcmEnabled = gcmEnabled;
    }
}
