package com.nncloudtv.web.json.player;

import java.io.Serializable;

public class ChannelLineup implements Serializable {
	private static final long serialVersionUID = -1457144493675060136L;
		
	private short position;
	private long  id;
	private String name;
	private String description;
	private String thumbnail;
	private int numberOfEpisode;
	private short type;
	private int status;
	private short contentType;
	private String channelSource; //youtube channel id or facebook url
	private long lastUpdateTime;
	private short sorting;
	private String piwikId;
	private String recentlyWatchedPrograms;
	private String youtubeName; //original youtube channel name
	private long numberOfSubscribers;
	private long numberOfViews;
	private String tags;
	private String curatorProfile; //curatorId	
	private String curatorName;
	private String curatorDescription;
	private String curatorThumbnail;
	private String subscriberProfiles;
	private String subscriberThumbnails;
	private String lastEpisodeTitle;
	private String poi;
	
	public short getPosition() {
		return position;
	}
	public void setPosition(short position) {
		this.position = position;
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	public int getNumberOfEpisode() {
		return numberOfEpisode;
	}
	public void setNumberOfEpisode(int numberOfEpisode) {
		this.numberOfEpisode = numberOfEpisode;
	}
	public short getType() {
		return type;
	}
	public void setType(short type) {
		this.type = type;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public short getContentType() {
		return contentType;
	}
	public void setContentType(short contentType) {
		this.contentType = contentType;
	}
	public String getChannelSource() {
		return channelSource;
	}
	public void setChannelSource(String channelSource) {
		this.channelSource = channelSource;
	}
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	public short getSorting() {
		return sorting;
	}
	public void setSorting(short sorting) {
		this.sorting = sorting;
	}
	public String getPiwikId() {
		return piwikId;
	}
	public void setPiwikId(String piwikId) {
		this.piwikId = piwikId;
	}
	public String getRecentlyWatchedPrograms() {
		return recentlyWatchedPrograms;
	}
	public void setRecentlyWatchedPrograms(String recentlyWatchedPrograms) {
		this.recentlyWatchedPrograms = recentlyWatchedPrograms;
	}
	public String getYoutubeName() {
		return youtubeName;
	}
	public void setYoutubeName(String youtubeName) {
		this.youtubeName = youtubeName;
	}
	public long getNumberOfSubscribers() {
		return numberOfSubscribers;
	}
	public void setNumberOfSubscribers(long numberOfSubscribers) {
		this.numberOfSubscribers = numberOfSubscribers;
	}
	public long getNumberOfViews() {
		return numberOfViews;
	}
	public void setNumberOfViews(long numberOfViews) {
		this.numberOfViews = numberOfViews;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public String getCuratorProfile() {
		return curatorProfile;
	}
	public void setCuratorProfile(String curatorProfile) {
		this.curatorProfile = curatorProfile;
	}
	public String getCuratorName() {
		return curatorName;
	}
	public void setCuratorName(String curatorName) {
		this.curatorName = curatorName;
	}
	public String getCuratorDescription() {
		return curatorDescription;
	}
	public void setCuratorDescription(String curatorDescription) {
		this.curatorDescription = curatorDescription;
	}
	public String getCuratorThumbnail() {
		return curatorThumbnail;
	}
	public void setCuratorThumbnail(String userThumbnail) {
		this.curatorThumbnail = userThumbnail;
	}
	public String getSubscriberProfiles() {
		return subscriberProfiles;
	}
	public void setSubscriberProfiles(String subscriberProfiles) {
		this.subscriberProfiles = subscriberProfiles;
	}
	public String getSubscriberThumbnails() {
		return subscriberThumbnails;
	}
	public void setSubscriberThumbnails(String subscriberThumbnails) {
		this.subscriberThumbnails = subscriberThumbnails;
	}
	public String getLastEpisodeTitle() {
		return lastEpisodeTitle;
	}
	public void setLastEpisodeTitle(String lastEpisodeTitle) {
		this.lastEpisodeTitle = lastEpisodeTitle;
	}
	public String getPoi() {
		return poi;
	}
	public void setPoi(String poi) {
		this.poi = poi;
	}		

}
