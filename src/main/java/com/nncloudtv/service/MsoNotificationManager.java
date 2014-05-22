package com.nncloudtv.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.MsoNotificationDao;
import com.nncloudtv.model.MsoNotification;

@Service
public class MsoNotificationManager {
    
    static MsoNotificationDao dao = new MsoNotificationDao();
    protected static final Logger log = Logger.getLogger(MsoNotificationManager.class.getName());
    
    public MsoNotification save(MsoNotification notification) {
        Date now = new Date();
        notification.setUpdateDate(now);
        if (notification.getCreateDate() == null) {
            notification.setCreateDate(now);
        }
        return dao.save(notification);
    }
    
    public List<MsoNotification> list(int page, int limit, String sidx, String sord, String filter) {
        return dao.list(page, limit, sidx, sord, filter);
    }
    
    public List<MsoNotification> listScheduled(int page, int limit, String filter) {
        return dao.listScheduled(page, limit, filter);
    }
    
    public MsoNotification findById(long id) {
        return dao.findById(id);
    }
    
    public void delete(MsoNotification notification) {
        if (notification != null) {
            dao.delete(notification);
        }
    }
    
}

