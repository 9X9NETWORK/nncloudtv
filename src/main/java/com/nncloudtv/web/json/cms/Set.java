package com.nncloudtv.web.json.cms;

import com.nncloudtv.model.SysTag;

public class Set extends SysTag {
    
    int cntChannel;
    String lang;
    short seq;
    String tag;
    String name;
    
    public int getCntChannel() { return cntChannel; }
    public String getLang() { return lang; }
    public short getSeq() { return seq; }
    public String getTag() { return tag; }
    public String getName() { return name; }
    
    public void setCntChannel(int cntChannel) { this.cntChannel = cntChannel; }
    public void setLang(String lang) { this.lang = lang; }
    public void setSeq(short seq) { this.seq = seq; }
    public void setTag(String tag) { this.tag = tag; }
    public void setName(String name) { this.name = name; }
    
    public short getSortingType() { return sorting; }
    public void setSortingType(short sorting) { this.sorting = sorting; }
    
}
