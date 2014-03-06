package com.nncloudtv.web.json.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerChannelLineup implements Serializable {
	private static final long serialVersionUID = -1858584282953395002L;
	
	private UserInfo userInfo;
	private List<SubscribeSetInfo> subscribeSetInfo = new ArrayList<SubscribeSetInfo>();
	private List<ChannelLineup> channelLineup = new ArrayList<ChannelLineup>();
	
	public UserInfo getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}
	public List<ChannelLineup> getChannelLineup() {
		return channelLineup;
	}
	public void setChannelLineup(List<ChannelLineup> channelLineup) {
		this.channelLineup = channelLineup;
	}
	public List<SubscribeSetInfo> getSubscribeSetInfo() {
		return subscribeSetInfo;
	}
	public void setSubscribeSetInfo(List<SubscribeSetInfo> subscribeSetInfo) {
		this.subscribeSetInfo = subscribeSetInfo;
	}
	
}
