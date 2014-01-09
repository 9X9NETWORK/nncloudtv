package com.nncloudtv.web.json.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Portal implements Serializable{
	
	private static final long serialVersionUID = 5863254382469998879L;
	private UserInfo userInfo;
	private List<SetInfo> setInfo = new ArrayList<SetInfo>();
	private List<ChannelLineup> channelLineup = new ArrayList<ChannelLineup>();
	private List<ProgramInfo> programInfo = new ArrayList<ProgramInfo>();
	
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
	public List<ProgramInfo> getProgramInfo() {
		return programInfo;
	}
	public void setProgramInfo(List<ProgramInfo> programInfo) {
		this.programInfo = programInfo;
	}
	public List<SetInfo> getSetInfo() {
		return setInfo;
	}
	public void setSetInfo(List<SetInfo> setInfo) {
		this.setInfo = setInfo;
	}
	

}
