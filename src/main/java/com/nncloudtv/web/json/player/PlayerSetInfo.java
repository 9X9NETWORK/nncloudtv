package com.nncloudtv.web.json.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerSetInfo implements Serializable {
	private static final long serialVersionUID = -522397186162351583L;
	
	private String msoName;
	private String msoImageUrl;
	private String msoDescription;
	private List<SetInfo> setInfo = new ArrayList<SetInfo>();
	private List<ChannelLineup> channels = new ArrayList<ChannelLineup>();
	private List<ProgramInfo> programs = new ArrayList<ProgramInfo>();
	
	public String getMsoName() {
		return msoName;
	}
	public void setMsoName(String msoName) {
		this.msoName = msoName;
	}
	public String getMsoImageUrl() {
		return msoImageUrl;
	}
	public void setMsoImageUrl(String msoImageUrl) {
		this.msoImageUrl = msoImageUrl;
	}
	public String getMsoDescription() {
		return msoDescription;
	}
	public void setMsoDescription(String msoDescription) {
		this.msoDescription = msoDescription;
	}
	public List<ChannelLineup> getChannels() {
		return channels;
	}
	public void setChannels(List<ChannelLineup> channels) {
		this.channels = channels;
	}
	public List<ProgramInfo> getPrograms() {
		return programs;
	}
	public void setPrograms(List<ProgramInfo> programs) {
		this.programs = programs;
	}
	public List<SetInfo> getSetInfo() {
		return setInfo;
	}
	public void setSetInfo(List<SetInfo> setInfo) {
		this.setInfo = setInfo;
	}	

}
