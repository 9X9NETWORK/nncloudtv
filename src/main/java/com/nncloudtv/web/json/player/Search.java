package com.nncloudtv.web.json.player;

import java.util.ArrayList;
import java.util.List;

public class Search {
	private int numOfCuratorReturned;
	private int numOfChannelReturned;
	private int numOfSuggestReturned;
	private int numOfCuratorTotal;
	private int numOfChannelTotal;
	private int numOfSuggestTotal;	
	private List<ChannelLineup> channelLineups = new ArrayList<ChannelLineup>();
	//missing curator info. but maybe not used anymore
	
	public int getNumOfCuratorReturned() {
		return numOfCuratorReturned;
	}
	public void setNumOfCuratorReturned(int numOfCuratorReturned) {
		this.numOfCuratorReturned = numOfCuratorReturned;
	}
	public int getNumOfChannelReturned() {
		return numOfChannelReturned;
	}
	public void setNumOfChannelReturned(int numOfChannelReturned) {
		this.numOfChannelReturned = numOfChannelReturned;
	}
	public int getNumOfSuggestReturned() {
		return numOfSuggestReturned;
	}
	public void setNumOfSuggestReturned(int numOfSuggestReturned) {
		this.numOfSuggestReturned = numOfSuggestReturned;
	}
	public int getNumOfCuratorTotal() {
		return numOfCuratorTotal;
	}
	public void setNumOfCuratorTotal(int numOfCuratorTotal) {
		this.numOfCuratorTotal = numOfCuratorTotal;
	}
	public int getNumOfChannelTotal() {
		return numOfChannelTotal;
	}
	public void setNumOfChannelTotal(int numOfChannelTotal) {
		this.numOfChannelTotal = numOfChannelTotal;
	}
	public int getNumOfSuggestTotal() {
		return numOfSuggestTotal;
	}
	public void setNumOfSuggestTotal(int numOfSuggestTotal) {
		this.numOfSuggestTotal = numOfSuggestTotal;
	}
	public List<ChannelLineup> getChannelLineups() {
		return channelLineups;
	}
	public void setChannelLineups(List<ChannelLineup> channelLineups) {
		this.channelLineups = channelLineups;
	}

	
}
