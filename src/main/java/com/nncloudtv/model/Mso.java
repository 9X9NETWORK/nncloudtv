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
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.EXTENDED_STRING_LENGTH)
    private String intro;
    
    @Persistent 
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String shortIntro;
    
    @Persistent 
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String slogan;
    
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
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (apnsEnabled ? 1231 : 1237);
        result = prime * result
                + ((contactEmail == null) ? 0 : contactEmail.hashCode());
        result = prime * result
                + ((createDate == null) ? 0 : createDate.hashCode());
        result = prime * result + (gcmEnabled ? 1231 : 1237);
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((intro == null) ? 0 : intro.hashCode());
        result = prime * result
                + ((jingleUrl == null) ? 0 : jingleUrl.hashCode());
        result = prime * result + ((lang == null) ? 0 : lang.hashCode());
        result = prime * result + ((logoUrl == null) ? 0 : logoUrl.hashCode());
        result = prime * result + maxChPerSet;
        result = prime * result + maxSets;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((shortIntro == null) ? 0 : shortIntro.hashCode());
        result = prime * result + ((slogan == null) ? 0 : slogan.hashCode());
        result = prime * result
                + ((supportedRegion == null) ? 0 : supportedRegion.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + type;
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
        Mso other = (Mso) obj;
        if (apnsEnabled != other.apnsEnabled)
            return false;
        if (contactEmail == null) {
            if (other.contactEmail != null)
                return false;
        } else if (!contactEmail.equals(other.contactEmail))
            return false;
        if (createDate == null) {
            if (other.createDate != null)
                return false;
        } else if (!createDate.equals(other.createDate))
            return false;
        if (gcmEnabled != other.gcmEnabled)
            return false;
        if (id != other.id)
            return false;
        if (intro == null) {
            if (other.intro != null)
                return false;
        } else if (!intro.equals(other.intro))
            return false;
        if (jingleUrl == null) {
            if (other.jingleUrl != null)
                return false;
        } else if (!jingleUrl.equals(other.jingleUrl))
            return false;
        if (lang == null) {
            if (other.lang != null)
                return false;
        } else if (!lang.equals(other.lang))
            return false;
        if (logoUrl == null) {
            if (other.logoUrl != null)
                return false;
        } else if (!logoUrl.equals(other.logoUrl))
            return false;
        if (maxChPerSet != other.maxChPerSet)
            return false;
        if (maxSets != other.maxSets)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (shortIntro == null) {
            if (other.shortIntro != null)
                return false;
        } else if (!shortIntro.equals(other.shortIntro))
            return false;
        if (slogan == null) {
            if (other.slogan != null)
                return false;
        } else if (!slogan.equals(other.slogan))
            return false;
        if (supportedRegion == null) {
            if (other.supportedRegion != null)
                return false;
        } else if (!supportedRegion.equals(other.supportedRegion))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (type != other.type)
            return false;
        if (updateDate == null) {
            if (other.updateDate != null)
                return false;
        } else if (!updateDate.equals(other.updateDate))
            return false;
        return true;
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

    public String getShortIntro() {
        return shortIntro;
    }

    public void setShortIntro(String shortIntro) {
        this.shortIntro = shortIntro;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }
}
