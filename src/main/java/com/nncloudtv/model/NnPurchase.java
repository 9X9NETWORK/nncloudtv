package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.nncloudtv.lib.NnStringUtil;

@PersistenceCapable(table = "nnpurchase", detachable = "true")
@Inheritance(customStrategy = "complete-table")
public class NnPurchase extends PersistentModel {
    
    @Persistent
    private Date createDate;
    
    @Persistent
    private Date updateDate;
    
    @Persistent
    private Date expireDate;
    
    @Persistent
    private long itemId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.SHORT_STRING_LENGTH)
    private String userIdStr; //format: shard-userId, example: 1-1234
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.VERY_LONG_STRING_LENGTH)
    private String purchaseToken;
    
    @Persistent
    private boolean verified; // server side verified
    
    @Persistent
    private short status;
    public static final short ACTIVE   = 0;
    public static final short INACTIVE = 1;
    public static final short INVALID  = 2;
    
    public NnPurchase(NnItem item, NnUser user, String purchaseToken) {
        
        this.purchaseToken = purchaseToken;
        this.itemId = item.getId();
        this.userIdStr = user.getIdStr();
        this.status = ACTIVE;
        this.verified = false;
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
    
    public long getItemId() {
        return itemId;
    }
    
    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
    public String getUserIdStr() {
        return userIdStr;
    }
    
    public void setUserIdStr(String userIdStr) {
        this.userIdStr = userIdStr;
    }
    
    public String getPurchaseToken() {
        return purchaseToken;
    }
    
    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }
    
    public short getStatus() {
        return status;
    }
    
    public void setStatus(short status) {
        this.status = status;
    }
    
    public boolean isVerified() {
        return verified;
    }
    
    public void setVerified(boolean verified) {
        this.verified = verified;
    }
    
    public Date getExpireDate() {
        return expireDate;
    }
    
    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }
}
