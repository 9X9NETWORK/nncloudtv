package com.nncloudtv.web.json.player;

import java.util.ArrayList;
import java.util.List;

public class SubEpisode {
	private String name;
	private String description;
	private String contentType;
	private String duration;
	private String thumbnail;
	private String thumbnailLarge;
	private String fileUrl;
	private String audioFileUrl;
	private String startTime;
	private String endTime;
	private long publishTime;	
	private List<PlayerTitleCard> titleCards = new ArrayList<PlayerTitleCard>();
	private List<PlayerPoi> pois = new ArrayList<PlayerPoi>();
	
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	public String getThumbnailLarge() {
		return thumbnailLarge;
	}
	public void setThumbnailLarge(String thumbnailLarge) {
		this.thumbnailLarge = thumbnailLarge;
	}
	public String getFileUrl() {
		return fileUrl;
	}
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	public String getAudioFileUrl() {
		return audioFileUrl;
	}
	public void setAudioFileUrl(String audioFileUrl) {
		this.audioFileUrl = audioFileUrl;
	}
	public long getPublishTime() {
		return publishTime;
	}
	public void setPublishTime(long publishTime) {
		this.publishTime = publishTime;
	}
	public List<PlayerTitleCard> getTitleCards() {
		return titleCards;
	}
	public void setTitleCards(List<PlayerTitleCard> titleCards) {
		this.titleCards = titleCards;
	}
	public List<PlayerPoi> getPois() {
		return pois;
	}
	public void setPois(List<PlayerPoi> pois) {
		this.pois = pois;
	}

}
