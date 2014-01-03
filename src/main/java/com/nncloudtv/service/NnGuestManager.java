package com.nncloudtv.service;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.nncloudtv.dao.NnGuestDao;
import com.nncloudtv.model.NnGuest;

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
}
