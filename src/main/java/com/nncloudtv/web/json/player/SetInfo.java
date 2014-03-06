package com.nncloudtv.web.json.player;

import java.io.Serializable;

public class SetInfo implements Serializable{
	private static final long serialVersionUID = -4113221584100270596L;
	private String id;
	private String name;
	private String thumbnail;
	private String thumbnail2;
	private int numberOfChannels;	
    private String bannerImageUrl; //banner basic resolution
    private String bannerImageUrl2; //banner retina resolution
    
	public String getBannerImageUrl() {
		return bannerImageUrl;
	}
	public void setBannerImageUrl(String bannerImageUrl) {
		this.bannerImageUrl = bannerImageUrl;
	}
	public String getBannerImageUrl2() {
		return bannerImageUrl2;
	}
	public void setBannerImageUrl2(String bannerImageUrl2) {
		this.bannerImageUrl2 = bannerImageUrl2;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	public String getThumbnail2() {
		return thumbnail2;
	}
	public void setThumbnail2(String thumbnail2) {
		this.thumbnail2 = thumbnail2;
	}
	public int getNumberOfChannels() {
		return numberOfChannels;
	}
	public void setNumberOfChannels(int numberOfChannels) {
		this.numberOfChannels = numberOfChannels;
	}

}
