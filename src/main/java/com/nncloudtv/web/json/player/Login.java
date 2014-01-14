package com.nncloudtv.web.json.player;

public class Login {

	private ApiStatus apiStatus;
	private String token;
	private String name;
	private long lastLogin;
	private String sphere;
	private String uiLang;
	private String curator;
	private boolean created; 
	private boolean fbUser;
	
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
	public long getLastLogin() {
		return lastLogin;
	}
	public void setLastLogin(long lastLogin) {
		this.lastLogin = lastLogin;
	}
	public String getSphere() {
		return sphere;
	}
	public void setSphere(String sphere) {
		this.sphere = sphere;
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
	public ApiStatus getApiStatus() {
		return apiStatus;
	}
	public void setApiStatus(ApiStatus apiStatus) {
		this.apiStatus = apiStatus;
	}
		
}
