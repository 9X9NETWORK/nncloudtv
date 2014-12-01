package com.nncloudtv.model;

import java.io.Serializable;
import java.util.Date;

public interface PersistentModel extends PersistentBaseModel, Serializable {
    
    public void setUpdateDate(Date date);
    
    public Date getUpdateDate();
    
    public void setCreateDate(Date date);
    
    public Date getCreateDate();
    
}
