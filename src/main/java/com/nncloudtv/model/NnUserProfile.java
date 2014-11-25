package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table = "nnuser_profile", detachable = "true")
@Inheritance(customStrategy = "complete-table")
public class NnUserProfile extends PersistentModel {
    
    @Persistent
    private long userId;
    
    @Persistent
    private long msoId; //which mso a user belongs to
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String name;
    
    @Persistent
    private String dob; //for now it's year
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String intro;
            
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String imageUrl;
    public static String IMAGE_URL_DEFAULT = "https://s3.amazonaws.com/9x9ui/war/v2/images/profile_default101.png";
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.VERY_SHORT_STRING_LENGTH)
    private String sphere; //content region, used with LangTable
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.VERY_SHORT_STRING_LENGTH)
    private String lang; //ui language, used with LangTable
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String profileUrl; //curator url
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.SHORT_STRING_LENGTH)
    private String phoneNumber;
    
    @Persistent
    private short gender; //0 (f) or 1(m) or 2(not specified)
    
    @Persistent
    private boolean featured; 
    
    @Persistent
    private int cntSubscribe; //the number of channels the user subscribes
    
    @Persistent
    private int cntChannel; //the number of channels the user creates
    
    @Persistent
    private int cntFollower; //the number of users who subscribe to this user's channels
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.SHORT_STRING_LENGTH)
    private String priv; // indicate pcs read write delete and ccs read write delete
                         // 7th digit indicate the permission of nnchannel.status
    public static final String PRIV_PCS          = "11100000";
    public static final String PRIV_CMS          = "00011100";
    public static final String PRIV_SYSTEM_STORE = "00000010";
    public static final String PRIV_UPLOAD_VIDEO = "00000001";
    
    public long getMsoId() {
        return msoId;
    }
    
    public void setMsoId(long msoId) {
        this.msoId = msoId;
    }
    
    public String getImageUrl() {
        if (imageUrl == null)
            return NnUserProfile.IMAGE_URL_DEFAULT;
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public NnUserProfile() {
    }
    
    public NnUserProfile(long userId, long msoId) {
        this.userId = userId;
        this.msoId = msoId;
        Date now = new Date();
        this.createDate = now;
        this.updateDate = now;
    }
        
    public NnUserProfile(long msoId, String name, String sphere, String lang, String dob) {
        this.name = name;
        this.msoId = msoId;
        if (sphere != null && sphere.length() > 0)
            this.sphere = sphere;
        if (lang != null && lang.length() > 0)
            this.lang = lang;
        if (dob != null && dob.length() > 0)
            this.dob = dob;
        Date now = new Date();
        this.createDate = now;
        this.updateDate = now;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDob() {
        return dob;
    }
    
    public void setDob(String dob) {
        this.dob = dob;
    }
    
    public String getIntro() {
        return intro;
    }
    
    public void setIntro(String intro) {
        this.intro = intro;
    }
    
    public String getSphere() {
        return sphere;
    }
    
    public void setSphere(String sphere) {
        if (sphere != null && sphere.contains("_"))
            sphere = sphere.substring(0, 2);
        this.sphere = sphere;
    }
    
    public String getLang() {
        return lang;
    }
    
    public void setLang(String lang) {
        if (lang != null & lang.length() > 2)
            lang = lang.substring(0, 2);
        this.lang = lang;
    }
    
    public String getProfileUrl() {
        return profileUrl;
    }
    
    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
    
    public short getGender() {
        return gender;
    }
    
    public void setGender(short gender) {
        this.gender = gender;
    }
    
    public void setGender(String gender) {
        if (gender == null || gender.length() == 0)
            this.gender = 2;
        else if (gender.startsWith("f"))
            this.gender = 0;
        else
            this.gender = 1;
    }
    
    public boolean isFeatured() {
        return featured;
    }
    
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
    
    public int getCntSubscribe() {
        return cntSubscribe;
    }
    
    public void setCntSubscribe(int cntSubscribe) {
        this.cntSubscribe = cntSubscribe;
    }
    
    public int getCntChannel() {
        return cntChannel;
    }
    
    public void setCntChannel(int cntChannel) {
        this.cntChannel = cntChannel;
    }
    
    public int getCntFollower() {
        return cntFollower;
    }
    
    public void setCntFollower(int cntFollower) {
        this.cntFollower = cntFollower;
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
    
    public String getBrandUrl() {
        if (profileUrl != null && profileUrl.matches("[a-zA-Z].+")) {
            return "~" + profileUrl;
        }
        return profileUrl;
    }
    
    public String getPriv() {
        return priv;
    }
    
    public void setPriv(String priv) {
        this.priv = priv;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
}
