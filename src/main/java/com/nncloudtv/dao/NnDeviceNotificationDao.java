package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.NnDeviceNotification;

public class NnDeviceNotificationDao extends GenericDao<NnDeviceNotification> {
    
    protected static final Logger log = Logger.getLogger(NnDeviceNotificationDao.class.getName());
    
    public NnDeviceNotificationDao() {
        super(NnDeviceNotification.class);
    }
    
    public List<NnDeviceNotification> findByDeviceId(long deviceId, long interval) {
        
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        List<NnDeviceNotification> detached = new ArrayList<NnDeviceNotification>();
        try {
            Query query = pm.newQuery(NnDeviceNotification.class);
            if (interval > 0)
                query.setFilter("deviceId == deviceIdParam && createDate > date_sub(curdate(), interval intervalParam day)");
            else
                query.setFilter("deviceId == deviceIdParam");
            query.declareParameters("long deviceIdParam, long intervalParam");
            query.setOrdering("createDate desc");
            @SuppressWarnings("unchecked")
            List<NnDeviceNotification> results = (List<NnDeviceNotification>) query.execute(deviceId, interval);
            detached = (List<NnDeviceNotification>) pm.detachCopyAll(results);
        } finally {
            pm.close();
        }
        return detached;
    }
    
    public List<NnDeviceNotification> findUnread() {
        
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        List<NnDeviceNotification> detached = new ArrayList<NnDeviceNotification>();
        try {
            Query query = pm.newQuery(NnDeviceNotification.class);
            query.setFilter("read == false");
            @SuppressWarnings("unchecked")
            List<NnDeviceNotification> results = (List<NnDeviceNotification>) query.execute();
            detached = (List<NnDeviceNotification>) pm.detachCopyAll(results);
        } finally {
            pm.close();
        }
        return detached;
    }
}
