package com.nncloudtv.web.json.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProgramInfo implements Serializable {
	private static final long serialVersionUID = 8833105022476444279L;
	private String id;
	private long channelId;
	private String name;
	private String description;
	private String contentType;
	private String duration;
	private String thumbnail;
	private String thumbnailLarge;
	private String fileUrl;
	private String audioFileUrl;
	private long publishTime;
	private String comment;
	private List<SubEpisode> subEpisodes = new ArrayList<SubEpisode>();
		
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getThumbnailLarge() {
		return thumbnailLarge;
	}
	public void setThumbnailLarge(String thumbnailLarge) {
		this.thumbnailLarge = thumbnailLarge;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public long getChannelId() {
		return channelId;
	}
	public void setChannelId(long channelId) {
		this.channelId = channelId;
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
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public List<SubEpisode> getSubEpisodes() {
		return subEpisodes;
	}
	public void setSubEpisodes(List<SubEpisode> subEpisodes) {
		this.subEpisodes = subEpisodes;
	}
		
}