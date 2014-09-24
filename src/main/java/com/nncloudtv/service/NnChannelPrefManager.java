package com.nncloudtv.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnChannelPrefDao;
import com.nncloudtv.model.NnChannelPref;

@Service
public class NnChannelPrefManager {
	
	protected static final Logger log = Logger
	        .getLogger(NnChannelPrefManager.class.getName());
	
	private NnChannelPrefDao prefDao = new NnChannelPrefDao();
	
	public NnChannelPrefManager() {
	}
	
	public NnChannelPrefManager(NnChannelPrefDao prefDao) {
        
        this.prefDao = prefDao;
    }
	
    public NnChannelPref save(NnChannelPref pref) {
		Date now = new Date();
		if (pref.getCreateDate() == null)
			pref.setCreateDate(now);
		pref.setUpdateDate(now);
		return prefDao.save(pref);
	}
	
	public List<NnChannelPref> save(List<NnChannelPref> prefs) {
        Date now = new Date();
        for (NnChannelPref pref : prefs) {
            if (pref.getCreateDate() == null) {
                pref.setCreateDate(now);
            }
            pref.setUpdateDate(now);
        }
        return prefDao.saveAll(prefs);
    }
	
	public List<NnChannelPref> findByChannelId(long channelId) {
		return prefDao.findByChannelId(channelId);
	}
	
	public List<NnChannelPref> findByChannelIdAndItem(long channelId, String item) {
		return prefDao.findByChannelIdAndItem(channelId, item);
	}
	
	public void delete(NnChannelPref pref) {
	    if (pref != null) {
	        prefDao.delete(pref);
	    }
	}
	
	public void delete(List<NnChannelPref> prefs) {
	    if (prefs != null && prefs.size() > 0) {
	        prefDao.deleteAll(prefs);
	    }
    }
    
    public void setAutoSync(Long channelId, String autoSync) {
        
        if (channelId == null || autoSync == null) {
            return ;
        }
        
        NnChannelPref channelAutoSync;
        List<NnChannelPref> channelPrefs = findByChannelIdAndItem(channelId, NnChannelPref.AUTO_SYNC);
        if (channelPrefs == null || channelPrefs.isEmpty()) {
            channelAutoSync = new NnChannelPref(channelId, NnChannelPref.AUTO_SYNC, NnChannelPref.OFF);
        } else {
            channelAutoSync = channelPrefs.get(0);
        }
        
        channelAutoSync.setValue(autoSync);
        
        save(channelAutoSync);
    }
}
