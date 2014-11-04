package com.nncloudtv.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnChannelPrefDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnChannelPref;

@Service
public class NnChannelPrefManager {
    
    protected static final Logger log = Logger.getLogger(NnChannelPrefManager.class.getName());
    
    private NnChannelPrefDao dao = NNF.getChPrefDao();
    
    public NnChannelPref save(NnChannelPref pref) {
        
        Date now = NnDateUtil.now();
        if (pref.getCreateDate() == null)
            pref.setCreateDate(now);
        pref.setUpdateDate(now);
        
        return dao.save(pref);
    }
    
    public List<NnChannelPref> save(List<NnChannelPref> prefs) {
        
        Date now = NnDateUtil.now();
        for (NnChannelPref pref : prefs) {
            if (pref.getCreateDate() == null) {
                pref.setCreateDate(now);
            }
            pref.setUpdateDate(now);
        }
        
        return dao.saveAll(prefs);
    }
    
    public List<NnChannelPref> findByChannelId(long channelId) {
        
        return dao.findByChannelId(channelId);
    }
    
    public NnChannelPref findByChannelIdAndItem(long channelId, String item) {
        
        List<NnChannelPref> prefs = dao.findByChannelIdAndItem(channelId, item);
        if (prefs.size() > 0) {
            
            return prefs.get(0);
        }
        
        return null;
    }
    
    public void delete(NnChannelPref pref) {
        
        if (pref != null) {
            dao.delete(pref);
        }
    }
    
    public void delete(List<NnChannelPref> prefs) {
        
        if (prefs != null && prefs.size() > 0) {
            dao.deleteAll(prefs);
        }
    }
    
    // TODO: to be removed
    public void setAutoSync(Long channelId, String autoSync) {
        
        if (channelId == null || autoSync == null) {
            return ;
        }
        
        NnChannelPref channelPref = findByChannelIdAndItem(channelId, NnChannelPref.AUTO_SYNC);
        if (channelPref == null) {
            
            channelPref = new NnChannelPref(channelId, NnChannelPref.AUTO_SYNC, NnChannelPref.OFF);
        }
        
        channelPref.setValue(autoSync);
        
        save(channelPref);
    }
}
