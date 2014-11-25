package com.nncloudtv.model;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table = "app", detachable = "true")
@Inheritance(customStrategy = "complete-table")
public class App extends PersistentModel {
    
    @Persistent
    private long msoId; //maybe different mso wants different
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String msoName;
    
    public static final short TYPE_IOS     = 1;
    public static final short TYPE_ANDROID = 2;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.EXTENDED_STRING_LENGTH)
    private String name;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.EXTENDED_STRING_LENGTH)
    private String intro;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String imageUrl;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String iosStoreUrl;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String androidStoreUrl;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String androidPackageName;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.VERY_SHORT_STRING_LENGTH)
    private String sphere;
    
    @Persistent
    private boolean featured;
    
    @Persistent
    private int position1; //featured position. to begin with, use position 1 only. 
    
    @Persistent
    private int position2; //general list position
    
    public long getMsoId() {
        return msoId;
    }
    
    public void setMsoId(long msoId) {
        this.msoId = msoId;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getIosStoreUrl() {
        return iosStoreUrl;
    }
    
    public void setIosStoreUrl(String iosStoreUrl) {
        this.iosStoreUrl = iosStoreUrl;
    }
    
    public String getAndroidStoreUrl() {
        return androidStoreUrl;
    }
    
    public void setAndroidStoreUrl(String androidStoreUrl) {
        this.androidStoreUrl = androidStoreUrl;
    }
    
    public String getSphere() {
        return sphere;
    }
    
    public void setSphere(String sphere) {
        this.sphere = sphere;
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
    
    public boolean isFeatured() {
        return featured;
    }
    
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
    
    public int getPosition1() {
        return position1;
    }
    
    public void setPosition1(int position1) {
        this.position1 = position1;
    }
    
    public int getPosition2() {
        return position2;
    }
    
    public void setPosition2(int position2) {
        this.position2 = position2;
    }
    
    public String getMsoName() {
        return msoName;
    }
    
    public void setMsoName(String msoName) {
        this.msoName = msoName;
    }
    
    public String getAndroidPackageName() {
        return androidPackageName;
    }
    
    public void setAndroidPackageName(String androidPackageName) {
        this.androidPackageName = androidPackageName;
    }
}
