package com.nncloudtv.service;

import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnItemDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnItem;

@Service
public class NnItemManager {
    
    protected static final Logger log = Logger.getLogger(NnItemManager.class.getName());
    
    protected NnItemDao dao = NNF.getItemDao();
    
    public NnItem findOne(Mso mso, String os, NnChannel channel) {
        
        short platform = NnItem.UNKNOWN;
        if (os.equals(PlayerService.OS_ANDROID)) {
            platform = NnItem.GOOGLEPLAY;
        } else if (os.equals(PlayerService.OS_IOS)) {
            platform = NnItem.APPSTORE;
        }
        
        return dao.findOne(mso.getId(), platform, channel.getId());
    }
}
