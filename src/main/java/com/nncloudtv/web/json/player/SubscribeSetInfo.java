package com.nncloudtv.web.json.player;

import java.io.Serializable;

public class SubscribeSetInfo implements Serializable{
	private static final long serialVersionUID = 8325357627934269959L;
	private short seq;
	private long id;
	private String name;
	private String imageUrl;
	private short type;
	public short getSeq() {
		return seq;
	}
	public void setSeq(short seq) {
		this.seq = seq;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public short getType() {
		return type;
	}
	public void setType(short type) {
		this.type = type;
	}
	
}
