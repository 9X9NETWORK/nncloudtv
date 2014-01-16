package com.nncloudtv.web.json.cms;

import java.util.Map;

public class MsoEx extends com.nncloudtv.model.Mso {
    
    private static final long serialVersionUID = 543022127572205720L;
    
    public MsoEx(String name, String intro, String contactEmail, short type) {
        
        super(name, intro, contactEmail, type);
    }
    
    private Map<String, String> meta;
    
    // the value comes from MsoConfig's SUPPORTED_REGION
    private String supportedRegion;
    
    // The value of maximum number of sets each MSO
    private short maxSets;
    
    // The maximum number of channels per set
    private short maxChPerSet;
    
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
    
    public Map<String, String> getMeta() {
        return meta;
    }
    
    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }
    
}
