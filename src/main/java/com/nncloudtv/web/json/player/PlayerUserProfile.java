package com.nncloudtv.web.json.player;

import java.io.Serializable;


public class PlayerUserProfile implements Serializable {
	private static final long serialVersionUID = 7702221665500819972L;
	
	private String name;
	private String email;
	private String description;
	private String image;
	private String gender;
	private String year;
	private String sphere;
	private String uiLang;
	private String phone;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
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
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	

}
