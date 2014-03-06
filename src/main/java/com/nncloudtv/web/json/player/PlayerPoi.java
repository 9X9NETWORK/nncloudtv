package com.nncloudtv.web.json.player;

import java.io.Serializable;

public class PlayerPoi implements Serializable {

	private static final long serialVersionUID = 7701883920247652103L;
	private String pId;
	private String startTime;
	private String endTime;
	private String type;
	private String context;
	public String getpId() {
		return pId;
	}
	public void setpId(String pId) {
		this.pId = pId;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
