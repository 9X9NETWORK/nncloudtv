package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnChannelPrefDao;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnChannelPref;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.web.json.cms.IapInfo;
import com.nncloudtv.web.json.facebook.FacebookPage;

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
    
    // "get" is from cache, "find" is from DB
    public NnChannelPref getByChannelIdAndItem(long channelId, String item) {
        
        String cacheKey = CacheFactory.getNnChannelPrefKey(channelId, item);
        NnChannelPref pref = (NnChannelPref) CacheFactory.get(cacheKey);
        
        if (pref != null){
            if (pref.getValue() == null || pref.getValue().isEmpty()) {
                log.fine("empty nnchannelpref from cache, key = " + cacheKey);
                return null;
            } else {
                log.fine("nnchannelpref from cache, key = " + cacheKey + ", value = " + pref.getValue());
                return pref;
            }
        }
        
        pref = findByChannelIdAndItem(channelId, item);
        
        if (pref != null) {
            log.info("set nnchannelpref to cache, key = " + cacheKey + ", value = " + pref.getValue());
            CacheFactory.set(cacheKey, pref);
        } else {
            log.info("set empty nnchannelpref to cache, key = " + cacheKey);
            CacheFactory.set(cacheKey, new NnChannelPref());
        }
        return pref;
        
    }
    
    public NnChannelPref findByChannelIdAndItem(long channelId, String item) {
        List<NnChannelPref> prefs = dao.findByChannelIdAndItem(channelId, item);
        if (prefs.size() > 0)
            return prefs.get(0);
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
    public String composeFacebookAutoshare(String fbUserId, String accessToken) {
        
        return fbUserId + "," + accessToken;
    }
    
    // TODO: to be removed
    public String[] parseFacebookAutoshare(String value) {
        int seperatorIndex = value.indexOf(',');
        if (seperatorIndex == -1) {
            return null;
        } else if (seperatorIndex + 1 == value.length()) { // the ',' is the last one char
            return null;
        } else if (seperatorIndex == 0) { // the ',' is the first one char
            return null;
        }
        String[] result = new String[2];
        result[0] = value.substring(0, seperatorIndex); // facebookID
        result[1] = value.substring(seperatorIndex + 1); // accessToken
        return result;
    }
    
    public void deleteAllChannelsFBbyUser(NnUser user) {
        List<NnChannel> channels = NNF.getChannelMngr().findByUser(user, 0, true);
        List<NnChannelPref> channelPrefs = new ArrayList<NnChannelPref>();
        NnChannelPref temp;
        for (NnChannel channel : channels) {
            temp = findByChannelIdAndItem(channel.getId(), NnChannelPref.FB_AUTOSHARE);
            if (temp != null) {
                channelPrefs.add(temp);
            }
        }
        delete(channelPrefs);
    }
    
    public void updateAllChannelsFBbyUser(NnUser user, List<FacebookPage> pages) {
        
        if (pages == null || pages.size() == 0 || user == null) {
            return;
        }
        
        List<NnChannel> channels = NNF.getChannelMngr().findByUser(user, 0, true);
        List<NnChannelPref> channelPrefs = new ArrayList<NnChannelPref>();
        NnChannelPref temp;
        for (NnChannel channel : channels) {
            temp = findByChannelIdAndItem(channel.getId(), NnChannelPref.FB_AUTOSHARE);
            if (temp != null) {
                channelPrefs.add(temp);
            }
        }
        
        Map<String, String> pageMap = new TreeMap<String, String>();
        for (FacebookPage page : pages) {
            pageMap.put(page.getId(), page.getAccess_token());
        }
        
        String[] parsed;
        for (NnChannelPref channelPref : channelPrefs) {
            parsed = parseFacebookAutoshare(channelPref.getValue());
            if (pageMap.containsKey(parsed[0])) {
                channelPref.setValue(composeFacebookAutoshare(parsed[0], pageMap.get(parsed[0])));
            }
        }
        
        save(channelPrefs);
    }
    
    // TODO: to be removed
    public void setBrand(Long channelId, Mso mso) {
        
        if (channelId == null || mso == null) {
            return;
        }
        
        NnChannelPref channelPref = findByChannelIdAndItem(channelId, NnChannelPref.BRAND_AUTOSHARE);
        if (channelPref == null) {
            
            channelPref = new NnChannelPref(channelId, NnChannelPref.BRAND_AUTOSHARE, mso.getName());
            save(channelPref);
            
        } else {
            
            if (!channelPref.getValue().equals(mso.getName())) {
                
                channelPref.setValue(mso.getName());
                save(channelPref);
            }
        }
    }
    
    // TODO: to be removed
    public NnChannelPref getBrand(Long channelId) {
        
        if (channelId == null) {
            return null;
        }
        
        NnChannelPref channelPref = findByChannelIdAndItem(channelId, NnChannelPref.BRAND_AUTOSHARE);
        if (channelPref == null) {
            
            return new NnChannelPref(channelId, NnChannelPref.BRAND_AUTOSHARE, Mso.NAME_9X9);
        }
        return channelPref;
    }
    
    public IapInfo getIapInfo(long channelId) {
        
        IapInfo iapInfo = new IapInfo();
        
        // title
        NnChannelPref titlePref = NNF.getChPrefMngr().findByChannelIdAndItem(channelId, NnChannelPref.IAP_TITLE);
        if (titlePref != null) {
            iapInfo.setTitle(titlePref.getValue());
        }
        
        // description
        NnChannelPref descPref = NNF.getChPrefMngr().findByChannelIdAndItem(channelId, NnChannelPref.IAP_DESC);
        if (descPref != null) {
            iapInfo.setDescription(descPref.getValue());
        }
        
        // price
        NnChannelPref pricePref = NNF.getChPrefMngr().findByChannelIdAndItem(channelId, NnChannelPref.IAP_PRICE);
        if (pricePref != null) {
            iapInfo.setPrice(pricePref.getValue());
        }
        
        // thumbnail
        NnChannelPref thumbPref = NNF.getChPrefMngr().findByChannelIdAndItem(channelId, NnChannelPref.IAP_THUMB);
        if (thumbPref != null) {
            iapInfo.setThumbnail(thumbPref.getValue());
        }
        
        return iapInfo;
    }
    
}
