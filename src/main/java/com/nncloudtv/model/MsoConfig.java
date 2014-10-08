package com.nncloudtv.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

/*
 * Mso's configurations
 */
@PersistenceCapable(table = "mso_config", detachable = "true")
public class MsoConfig implements Serializable  {
    
    private static final long serialVersionUID = -5535237161784792007L;

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    @Persistent
    private long msoId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String item;
    
    // System wide config
    public static String RO                    = "read-only";
    public static String MEMCACHE_SERVER       = "memcache-server";
    public static String DEBUG                 = "debug";
    
    public static String CDN                   = "cdn";
    public static String VIDEO                 = "video";
    public static String FBTOKEN               = "fbtoken";                  //regardless of the brand, for player parsing feed data 
    public static String REALFBTOKEN           = "realfbtoken";
    public static String FORCE_UPGRADE         = "force-upgrade";
    public static String APP_EXPIRE            = "app-expire";               //format: "January 2, 2014" [note, with ","]
    public static String APP_VERSION_EXPIRE    = "app-version-expire";       //format: "ios 3.0.2;ios 3.0.7;android 4.0" 
    public static String UPGRADE_MSG           = "upgrade-msg";
    public static String QUEUED                = "queued";
    public static String SUPPORTED_REGION      = "supported-region"; //zh, en
    public static String API_MINIMAL           = "api-minimal";
    public static String SYSTEM_CATEGORY_MASK  = "system-category-mask";
    public static String STORE_ANDROID         = "store-android";            //android store url
    public static String STORE_IOS             = "store-ios";                //ios store url
    public static String FAVICON_URL           = "favicon-url";
    public static String FACEBOOK_APPTOKEN     = "facebook-apptoken";        //ios store url
    public static String FACEBOOK_CLIENTID     = "facebook-clientid";
    public static String FACEBOOK_CLIENTSECRET = "facebook-client-secret";
    public static String MAX_SETS              = "max-sets";
    public static String MAX_CH_PER_SET        = "max-ch-per-set";
    public static String YOUTUBE_ID_ANDROID    = "youtube-id-android";
    public static String CHROMECAST_ID         = "chromecast-id";
    public static String GCM_SENDER_ID         = "gcm-sender-id";
    public static String GCM_API_KEY           = "gcm-api-key";
    public static String SHAKE_DISCOVER        = "shake-discover"; //on, off
    public static String HOMEPAGE              = "homepage"; //whatson, portal
    public static String ABOUT_US              = "aboutus";
    public static String SIGNUP_ENFORCE        = "signup-enforce";
    public static String AD_IOS                = "ad-ios"; //"iad" or "direct-video" 
    public static String ADMOBKEY_ANDROID      = "admobkey-android";
    public static String ADMOBKEY_IOS          = "admobkey-ios";
    public static String AD_IOS_TYPE           = "ad-ios-type"; //interstitials, banner, video 
    public static String AD_ANDROID            = "ad-android"; //"admob" or "direct-video"
    public static String AUDIO_BACKGROUND      = "audio-background";
    public static String SOCIAL_FEEDS          = "social-feeds"; //twitter NBA;facebook ETtoday
    public static String SOCIAL_FEEDS_SERVER   = "social-feeds-server";
    public static String NOTIFICATION_SOUND_VIBRATION  = "notification-sound-vibration"; //sound off;vibration off
    public static String SEARCH                = "search"; //all, store, off, youtube
    public static String STORE                 = "store"; //on, off
    public static String CMS_LOGO              = "cms-logo"; // private label cms logo
    
    public static String GOOGLE_ANALYTICS_IOS     = "google-analytics-ios";
    public static String GOOGLE_ANALYTICS_ANDROID = "google-analytics-android";
    public static String GOOGLE_ANALYTICS_WEB     = "google-analytics-web";
    public static String FLURRY_ANALYTICS_ANDROID = "flurry-analytics-android";
    public static String FLURRY_ANALYTICS_IOS     = "flurry-analytics-ios";
    
    public static String ANDROID_URL_ORIGIN            = "android-url-origin";
    public static String ANDROID_URL_LANDING_DIRECT    = "android-url-landing-direct";
    public static String ANDROID_URL_LANDING_SUGGESTED = "android-url-landing-suggested";
    public static String ANDROID_URL_MARKET_DIRECT     = "android-url-market-direct";
    public static String ANDROID_URL_MARKET_SUGGESTED  = "android-url-market-suggested";
    public static String IOS_URL_ORIGIN                = "ios-url-origin";
    public static String IOS_URL_LANDING_DIRECT        = "ios-url-landing-direct";
    public static String IOS_URL_LANDING_SUGGESTED     = "ios-url-landing-suggested";
    
    // S3 related configurations
    public static String AWS_KEY          = "aws-key";
    public static String AWS_ID           = "aws-id";
    public static String CF_KEY_PAIR_ID   = "cf-key-pair-id";
    public static String CF_SUBDOMAIN     = "cf-subdomain";
    public static String S3_UPLOAD_BUCKET = "s3-upload-bucket";
    public static String S3_VIDEO_BUCKET  = "s3-video-bucket";
    
    // In App Purchase configurations
    public static String APP_NAME     = "app-name";
    public static String PACKAGE_NAME = "package-name";
    
    public static final short MAXCHPERSET_DEFAULT = 27;
    public static final short MAXSETS_DEFAULT     = 3;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String value;
    public static final String CDN_AMAZON = "amazon";
    public static final String CDN_AKAMAI = "akamai";
    public static final String DISABLE_ALL_SYSTEM_CATEGORY = "ALL";
    public static final String AD_DIRECT_VIDEO = "direct-video";
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    public MsoConfig() {}
    
    public MsoConfig(long msoId, String item, String value) {
        this.msoId = msoId;
        this.item = item;
        this.value = value;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMsoId() {
        return msoId;
    }

    public void setMsoId(long msoId) {
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
    
}
