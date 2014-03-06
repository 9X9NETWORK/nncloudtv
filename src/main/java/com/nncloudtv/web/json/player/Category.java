package com.nncloudtv.web.json.player;

import java.io.Serializable;

public class Category implements Serializable{

	private static final long serialVersionUID = -7308244982056922109L;
	
	private String id;
	private String name;
	private int cntChannel;
	private String nextLevel;
	
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
	public int getCntChannel() {
		return cntChannel;
	}
	public void setCntChannel(int cntChannel) {
		this.cntChannel = cntChannel;
	}
	public String getNextLevel() {
		return nextLevel;
	}
	public void setNextLevel(String nextLevel) {
		this.nextLevel = nextLevel;
	}
	
}
