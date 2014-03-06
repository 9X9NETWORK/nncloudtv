package com.nncloudtv.web.json.player;

import java.util.ArrayList;
import java.util.List;

public class PlayerDevice {
	String token;
	private List<String> users = new ArrayList<String>(); 
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}
	
}
