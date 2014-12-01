package com.nncloudtv.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnUserPrefDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserPref;

@Service
public class NnUserPrefManager {
    
    protected static final Logger log = Logger.getLogger(NnUserPrefManager.class.getName());
        
    private NnUserPrefDao dao = NNF.getPrefDao();
    
    public NnUserPref save(NnUser user, NnUserPref pref) {
        Date now = NnDateUtil.now();
        if (pref.getCreateDate() == null)
            pref.setCreateDate(now);
        pref.setUpdateDate(now);
        return dao.save(user, pref);
    }
    
    public List<NnUserPref> findByUser(NnUser user) {
        return dao.findByUser(user);
    }
    
    public NnUserPref findByUserAndItem(NnUser user, String item) {
        return dao.findByUserAndItem(user, item);
    }
    
    public void delete(NnUser user, NnUserPref pref) {
        dao.delete(user, pref);
    }
}
