package com.nncloudtv.web.json.cms;

import com.nncloudtv.model.SysTag;

public class Category extends SysTag {
    
    private static final long serialVersionUID = -3796096026005107627L;
    
    String name;
    int cntChannel;
    String lang;
    short seq;
    String enName;
    String zhName;
    
    public String getName() { return name; }
    public int getCntChannel() { return cntChannel; }
    public String getLang() { return lang; }
    public short getSeq() { return seq; }
    public String getEnName() { return enName; }
    public String getZhName() { return zhName; }
    
    public void setName(String name) { this.name = name; }
    public void setCntChannel(int cntChannel) { this.cntChannel = cntChannel; }
    public void setLang(String lang) { this.lang = lang; }
    public void setSeq(short seq) { this.seq = seq; }
    public void setEnName(String enName) { this.enName = enName; }
    public void setZhName(String zhName) { this.zhName = zhName; }
    
}
