package com.nncloudtv.web.json.cms;

import com.nncloudtv.model.SysTag;

public class Set extends SysTag {
    
    int    cntChannel;
    String lang;
    String tag;
    String name;
    String iosBannerUrl;
    String androidBannerUrl;
    
    public int    getCntChannel()  { return cntChannel; }
    public String getLang()        { return lang; }
    public String getTag()         { return tag; }
    public String getName()        { return name; }
    public short  getSortingType() { return sorting; }
    public String getIosBannerUrl() { return iosBannerUrl; }
    public String getAndroidBannerUrl() { return androidBannerUrl; }
    
    public void setCntChannel(int cntChannel) { this.cntChannel = cntChannel; }
    public void setLang(String lang)          { this.lang = lang; }
    public void setTag(String tag)            { this.tag = tag; }
    public void setName(String name)          { this.name = name; }
    public void setSortingType(short sorting) { this.sorting = sorting; }
    public void setIosBannerUrl(String iosBannerUrl) { this.iosBannerUrl = iosBannerUrl; }
    public void setAndroidBannerUrl(String androidBannerUrl) { this.androidBannerUrl = androidBannerUrl; }
    
}
