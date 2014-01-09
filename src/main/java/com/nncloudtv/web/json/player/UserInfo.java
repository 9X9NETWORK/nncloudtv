package com.nncloudtv.web.json.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserInfo implements Serializable {
	private static final long serialVersionUID = 4146310092582003421L;
	
	private String token;
	private String userIdStr;
	private String name;
	private String sphere;
	private String lastLogin;
	private String uiLang;
	private String curator;
	private boolean created; 
	private boolean fbUser;
	private List<String> prefs = new ArrayList<String>(); 
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSphere() {
		return sphere;
	}
	public void setSphere(String sphere) {
		this.sphere = sphere;
	}
	public String getLastLogin() {
		return lastLogin;
	}
	public void setLastLogin(String lastLogin) {
		this.lastLogin = lastLogin;
	}
	public String getUiLang() {
		return uiLang;
	}
	public void setUiLang(String uiLang) {
		this.uiLang = uiLang;
	}
	public String getCurator() {
		return curator;
	}
	public void setCurator(String curator) {
		this.curator = curator;
	}
	public boolean isCreated() {
		return created;
	}
	public void setCreated(boolean created) {
		this.created = created;
	}
	public boolean isFbUser() {
		return fbUser;
	}
	public void setFbUser(boolean fbUser) {
		this.fbUser = fbUser;
	}
	public List<String> getPrefs() {
		return prefs;
	}
	public void setPrefs(List<String> prefs) {
		this.prefs = prefs;
	}
	public String getUserIdStr() {
		return userIdStr;
	}
	public void setUserIdStr(String userIdStr) {
		this.userIdStr = userIdStr;
	}

	
}
