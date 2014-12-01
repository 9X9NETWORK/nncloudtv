package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.AuthLib;
import com.nncloudtv.lib.NnStringUtil;

/**
 * 9x9 User accounts
 */
@PersistenceCapable(table = "nnuser", detachable = "true")
public class NnUser implements PersistentModel {
    
    private static final long serialVersionUID = 1663052759659769121L;
    private static final boolean cachable = true;
    
    public boolean isCachable() {
        return cachable;
    }
    
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
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String token;  //each user has a unique token, could be a access_token from fb as well
    
    @Persistent
    private short shard; //which shard a user belongs to
    public static short SHARD_UNKNWON = 0;
    public static short SHARD_DEFAULT = 1;
    public static short SHARD_CHINESE = 2;
    
    //xxx
    @NotPersistent
    private long msoId; //which mso a user belongs to
    
    //unique key, can be a facebook id
    //to get "email email", use getUserEmail
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String email; 
    
    @NotPersistent
    private NnUserProfile profile;
    
    @NotPersistent
    private String password;
    
    @Persistent(defaultFetchGroup = "true")
    private byte[] cryptedPassword;
    
    @Persistent
    private byte[] salt;
        
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    @Persistent
    private short type; //user's type
    public static short TYPE_ADMIN = 1;        // Administrator (abandoned)
    public static short TYPE_TBC = 2;          // a.k.a. Target Brand-name Customer (abandoned)
    public static short TYPE_TCO = 3;          // a.k.a. Target Content Owner (abandoned)
    public static short TYPE_USER = 4;         
    public static short TYPE_NN = 5;           // default user, must have and only one
    public static short TYPE_3X3 = 6;          // Taiwan partner
    public static short TYPE_ENTERPRISE = 7;   // U.S. partner
    public static short TYPE_FAKE_YOUTUBE = 8; //create fake account based on youtube users
    //email format will be have ("-at-" or "-AT-") and ("@9x9.tv")
    //example, aaa9x9@gmail.com" to "aaa9x9-AT-gmail.com@9x9.tv
    public static short TYPE_YOUTUBE_CONNECT = 9; 
    
    public static String GUEST_EMAIL = "guest@9x9.com";    
    public static String GUEST_NAME = "Guest";
    public static String ANONYMOUS_EMAIL = "anonymous@flipr.tv";
    public static String ANONYMOUS_NAME = "Anonymous";
    
    //used to store facebook account email
    //to get "facebook id", use getUserFbId()
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String fbId;
    
    @Persistent
    private long expires;
        
    //used for testing without changing the program logic
    //isTemp set to true means it can be wiped out
    @Persistent
    private boolean isTemp; 
    
    public NnUser(String email, String password, short type) {
        this.email = email;
        this.salt = AuthLib.generateSalt();
        this.cryptedPassword= AuthLib.encryptPassword(password, this.getSalt());
        this.profile = new NnUserProfile();
        this.type = type;
        this.profile = new NnUserProfile();
    }
    
    //for facebook
    public NnUser(String email, String fbId, String fbToken) {
        this.email = fbId;
        this.fbId = email;
        this.token = fbToken;
        this.profile = new NnUserProfile();
    }
    
    public NnUser(String email, String password, short type, long msoId) {
        this.email = email;
        this.salt = AuthLib.generateSalt();
        this.cryptedPassword= AuthLib.encryptPassword(password, this.getSalt());
        this.type = type;
        this.msoId = msoId;
        this.profile = new NnUserProfile();
    }
    
    public String getIdStr() {
        return shard + "-" + this.getId();
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getUserEmail() {
        if (this.isFbUser()) {
            return fbId;
        }
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
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
    
    public short getType() {
        return type;
    }
    
    public void setType(short type) {
        this.type = type;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public byte[] getCryptedPassword() {
        return cryptedPassword;
    }
    
    public void setCryptedPassword(byte[] cryptedPassword) {
        this.cryptedPassword = cryptedPassword;
    }
    
    public byte[] getSalt() {
        return salt;
    }
    
    public void setSalt(byte[] salt) {
        this.salt = salt;
    }
    
    public long getMsoId() {
        return msoId;
    }
    
    public void setMsoId(long msoId) {
        this.msoId = msoId;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public long getExpires() {
        return expires;
    }
    
    public void setExpires(long expires) {
        this.expires = expires;
    }
    
    public short getShard() {
        
        if (shard == 0) {
            
            return SHARD_DEFAULT;
        }
        
        return shard;
    }
    
    public void setShard(short shard) {
        this.shard = shard;
    }
    
    public boolean isTemp() {
        return isTemp;
    }
    
    public void setTemp(boolean isTemp) {
        this.isTemp = isTemp;
    }
    
    public String getFbId() {
        return fbId;
    }
    
    public void setFbId(String fbId) {
        this.fbId = fbId;
    }
    
    public String getUserFbId() {
        if (this.isFbUser()) {
            return email;
        }
        return null;
    }
    
    public boolean isFbUser() {
        if (fbId != null)
            return true;
        return false;
    }
    
    public NnUserProfile getProfile() {
        
        if (profile == null) {
            this.profile = new NnUserProfile(this.getId(), this.getMsoId());
        }
        return profile;
    }
    
    public void setProfile(NnUserProfile profile) {
        this.profile = profile;
    }

}
