
package com.nncloudtv.service;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.nncloudtv.dao.NnGuestDao;
import com.nncloudtv.model.NnGuest;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.web.json.player.UserInfo;

public class NnGuestManager {

    private NnGuestDao guestDao = new NnGuestDao();
    private NnUserManager userMngr;
    
    @Autowired
    public NnGuestManager(NnUserManager userMngr) {
        
        this.userMngr = userMngr;
    }
    
    public NnGuestManager() {
        
        this.userMngr = new NnUserManager();
    }
    
    public void save(NnGuest guest, HttpServletRequest req) {
        if (guest.getCreateDate() == null)
            guest.setCreateDate(new Date());
        if (guest.getShard() == 0) {
            short shard = userMngr.getShardByLocale(req);
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
        String sphere = userMngr.findLocaleByHttpRequest(req);
    	if (format == PlayerApiService.FORMAT_PLAIN) {
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
