package com.nncloudtv.web.json.player;

import java.io.Serializable;
import java.util.List;

import com.nncloudtv.model.AdPlacement;

public class BrandInfo  implements Serializable {
	private static final long serialVersionUID = -7760475382568969843L;
	
	private long key;
	private String name;
	private String title;
	private String logoUrl;
	private String jingleUrl;
	private String preferredLangCode;
	private String debug = "0";
	private String fbToken;
	private String readOnly;
	private String supportedRegion = "en US;zh 台灣";
	private String locale;
	private long brandInfoCounter;
	private String piwik;
	private String acceptLang;
	private String forceUpgrade = "0";
	private String upgradeMessage;
	private String tutorialVideo;
	private String gcmSenderId;
	private String aboutus;
	private List<AdPlacement> adPlacements;
	
	public List<AdPlacement> getAdPlacements() {
        return adPlacements;
    }
    public void setAdPlacements(List<AdPlacement> adPlacements) {
        this.adPlacements = adPlacements;
    }
    public String getTutorialVideo() {
		return tutorialVideo;
	}
	public void setTutorialVideo(String tutorialVideo) {
		this.tutorialVideo = tutorialVideo;
	}
	public String getUpgradeMessage() {
		return upgradeMessage;
	}
	public void setUpgradeMessage(String upgradeMessage) {
		this.upgradeMessage = upgradeMessage;
	}
	public String getForceUpgrade() {
		return forceUpgrade;
	}
	public void setForceUpgrade(String forceUpgrade) {
		this.forceUpgrade = forceUpgrade;
	}
	public String getPreferredLangCode() {
		return preferredLangCode;
	}
	public void setPreferredLangCode(String preferredLangCode) {
		this.preferredLangCode = preferredLangCode;
	}
	public long getKey() {
		return key;
	}
	public void setKey(long key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLogoUrl() {
		return logoUrl;
	}
	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}
	public String getJingleUrl() {
		return jingleUrl;
	}
	public void setJingleUrl(String jingleUrl) {
		this.jingleUrl = jingleUrl;
	}
	public String getDebug() {
		return debug;
	}
	public void setDebug(String debug) {
		this.debug = debug;
	}
	public String getFbToken() {
		return fbToken;
	}
	public void setFbToken(String fbToken) {
		this.fbToken = fbToken;
	}
	public String getReadOnly() {
		return readOnly;
	}
	public void setReadOnly(String readOnly) {
		this.readOnly = readOnly;
	}
	public String getSupportedRegion() {
		return supportedRegion;
	}
	public void setSupportedRegion(String supportedRegion) {
		this.supportedRegion = supportedRegion;
	}
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	public long getBrandInfoCounter() {
		return brandInfoCounter;
	}
	public void setBrandInfoCounter(long brandInfoCounter) {
		this.brandInfoCounter = brandInfoCounter;
	}
	public String getPiwik() {
		return piwik;
	}
	public void setPiwik(String piwik) {
		this.piwik = piwik;
	}
	public String getAcceptLang() {
		return acceptLang;
	}
	public void setAcceptLang(String acceptLang) {
		this.acceptLang = acceptLang;
	}
    public String getGcmSenderId() {
        return gcmSenderId;
    }
    public void setGcmSenderId(String gcmSenderId) {
        this.gcmSenderId = gcmSenderId;
    }
    public String getAboutus() {
        return aboutus;
    }
    public void setAboutus(String aboutus) {
        this.aboutus = aboutus;
    }

}
