package com.nncloudtv.model;

import java.io.Serializable;

public interface PersistentBaseModel extends Serializable {
    
    public long getId();
    
    public void setId(long id);
    
    public boolean isCachable();
    
}
