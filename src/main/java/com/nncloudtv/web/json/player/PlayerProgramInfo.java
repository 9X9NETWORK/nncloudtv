package com.nncloudtv.web.json.player;

import java.util.ArrayList;
import java.util.List;

public class PlayerProgramInfo {
	private UserInfo userInfo;
	private List<ProgramInfo> programInfo = new ArrayList<ProgramInfo>();
	public UserInfo getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}
	public List<ProgramInfo> getProgramInfo() {
		return programInfo;
	}
	public void setProgramInfo(List<ProgramInfo> programInfo) {
		this.programInfo = programInfo;
	}

}
