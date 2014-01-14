package com.nncloudtv.web.json.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CategoryInfo implements Serializable {
	private static final long serialVersionUID = -7791696170870124075L;
	String id;
	String name;
	long start;
	long count;
	long total;
	List<String> tags = new ArrayList<String>();	
	List<ChannelLineup> channelLineup = new ArrayList<ChannelLineup>();
	List<ProgramInfo> programInfo = new ArrayList<ProgramInfo>();
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
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
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
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
}
