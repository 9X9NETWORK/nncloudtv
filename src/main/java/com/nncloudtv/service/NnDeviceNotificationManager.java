package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.nncloudtv.dao.NnDeviceNotificationDao;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.NnDeviceNotification;

public class NnDeviceNotificationManager {
    
    protected static final Logger log = Logger.getLogger(NnDeviceNotificationManager.class.getName());
    
    private NnDeviceNotificationDao dao = new NnDeviceNotificationDao();
    
    public void delete(NnDeviceNotification notification) {
        
        dao.delete(notification);
    }
    
    public void delete(List<NnDeviceNotification> notifications) {
        
        dao.deleteAll(notifications);
    }
    
    public NnDeviceNotification save(NnDeviceNotification notification) {
        
        if (notification == null) return null;
        
        Date now = new Date();
        
        if (notification.getCreateDate() == null) {
            notification.setCreateDate(now);
        }
        
        notification.setUpdateDate(now);
        notification = dao.save(notification);
        
        return notification;
    }
    
    public List<NnDeviceNotification> save(List<NnDeviceNotification> notifications) {
        
        if (notifications == null) return new ArrayList<NnDeviceNotification>();
        
        Date now = new Date();
        
        for (NnDeviceNotification notification : notifications) {
            
            if (notification.getCreateDate() == null) {
                notification.setCreateDate(now);
            }
            notification.setUpdateDate(now);
        }
        
        return dao.saveAll(notifications);
    }
    
    public List<NnDeviceNotification> findByDeviceId(long deviceId, long interval) {
        
        return dao.findByDeviceId(deviceId, interval);
    }
    
    public List<NnDeviceNotification> findByDeviceId(long deviceId) {
        
        return dao.findByDeviceId(deviceId, 0);
    }
    
    public Object composeNotificationList(List<NnDeviceNotification> notifications) {
        
        String output = "";
        for (NnDeviceNotification notification : notifications) {
            output += this.composeEachNotificationList(notification) + "\n";
        }
        
        return output;
    }
    
    private String composeEachNotificationList(NnDeviceNotification notification) {
        
        String[] ori = {
            String.valueOf(notification.isRead()),
            notification.getTimeStamp(),
            notification.getMessage(),
            notification.getContent() == null ? "" : notification.getContent(),
            notification.getTitle() == null ? "" : notification.getTitle(),
            notification.getLogo() == null ? "" : notification.getLogo()
        };
        
        return NnStringUtil.getDelimitedStr(ori);
    }
    
    public List<NnDeviceNotification> list(int page, int limit, String sort) {
        return dao.list(page, limit, sort);
    }
    
    public List<NnDeviceNotification> list(int page, int limit, String sort, String filter) {
        return dao.list(page, limit, sort, filter);
    }
    
    public List<NnDeviceNotification> findUnreadByDeviceId(long deviceId) {
        
        return dao.findUnreadDeviceId(deviceId);
    }
}
