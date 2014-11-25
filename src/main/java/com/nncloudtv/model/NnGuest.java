package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.nncloudtv.service.NnUserManager;

/**
 * NnGuest account. It's also borrowed for NnUser captcha.
 * Data can be wiped out.
 */
@PersistenceCapable(table = "nnguest", detachable = "true")
@Inheritance(customStrategy = "complete-table")
public class NnGuest extends PersistentModel {
    
    @Persistent
    private String token;
    
    @Persistent
    private Date expiredAt; //used with captcha
    
    public static short GUESS_MAXTIMES = 5;
    @Persistent
    private int guessTimes; //used with captcha
    
    @Persistent
    private long captchaId;
    
    @Persistent
    private short type; //NnUSer use NnGuest for captcha use
    public static short TYPE_GUEST = 1;
    public static short TYPE_USER = 2;
    
    public static String TOKEN_PREFIX = "g99/";
    
    @Persistent
    private Date createDate;
        
    @Persistent
    private short shard; //which shard a user belongs to
        
    public NnGuest(short type) {
        if (type == TYPE_GUEST) {
            String random = NnUserManager.generateToken((short)1);
            this.token = TOKEN_PREFIX + random;
        }
        this.type = type;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public long getCaptchaId() {
        return captchaId;
    }
    
    public void setCaptchaId(long captchaId) {
        this.captchaId = captchaId;
    }
    
    public Date getExpiredAt() {
        return expiredAt;
    }
    
    public void setExpiredAt(Date expiredAt) {
        this.expiredAt = expiredAt;
    }
    
    public int getGuessTimes() {
        return guessTimes;
    }
    
    public void setGuessTimes(int guessTimes) {
        this.guessTimes = guessTimes;
    }
    
    public short getType() {
        return type;
    }
    
    public void setType(short type) {
        this.type = type;
    }
    
    public Date getCreateDate() {
        return createDate;
    }
    
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    
    public short getShard() {
        return shard;
    }
    
    public void setShard(short shard) {
        this.shard = shard;
    }
}
