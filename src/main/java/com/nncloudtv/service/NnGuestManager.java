
package com.nncloudtv.service;
import javax.servlet.http.HttpServletRequest;

import com.nncloudtv.dao.NnGuestDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.NnGuest;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.web.api.ApiContext;
import com.nncloudtv.web.json.player.UserInfo;

public class NnGuestManager {
    
    private NnGuestDao guestDao = new NnGuestDao();
    
    public void save(NnGuest guest, HttpServletRequest req) {
        if (guest.getCreateDate() == null)
            guest.setCreateDate(NnDateUtil.now());
        if (guest.getShard() == 0) {
            short shard = NNF.getUserMngr().getShardByLocale(req);
            guest.setShard(shard);
        }
        guestDao.save(guest);
    }
    
    public void delete(NnGuest guest) {
        guestDao.delete(guest);
    }
    
    public NnGuest findByToken(String token) {
        return guestDao.findByToken(token);
    }
    
    public Object getPlayerGuestRegister(NnGuest guest, short format, HttpServletRequest req) {
        
        String sphere = NNF.getUserMngr().findLocaleByHttpRequest(req);
        if (format == ApiContext.FORMAT_PLAIN) {
            String[] result = {""};
            result[0] += PlayerApiService.assembleKeyValue("token", guest.getToken());
            result[0] += PlayerApiService.assembleKeyValue("name", NnUser.GUEST_NAME);
            result[0] += PlayerApiService.assembleKeyValue("sphere", sphere);
            result[0] += PlayerApiService.assembleKeyValue("lastLogin", "");    	
            return result;
        } else {
            UserInfo json = new UserInfo();
            json.setToken(guest.getToken());
            json.setName(NnUser.GUEST_NAME);
            json.setSphere(sphere);
            json.setLastLogin("");
            return json;
        }
    }
}
