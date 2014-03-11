package com.nncloudtv.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nncloudtv.lib.PMF;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoNotification;

public class MsoNotificationDao extends GenericDao<MsoNotification> {
    
    protected static final Logger log = Logger.getLogger(MsoNotificationDao.class.getName());
    
    public MsoNotificationDao() {
        super(MsoNotification.class);
    }
    
    public MsoNotification save(MsoNotification notification) {
        if (notification == null) {return null;}
        notification.setUpdateDate(new Date());
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        try {
            pm.makePersistent(notification);
            notification = pm.detachCopy(notification);
        } finally {
            pm.close();
        }
        return notification;
    }
    
    public List<MsoNotification> findByMso(Mso mso) {
        PersistenceManager pm = PMF.getContent().getPersistenceManager();
        List<MsoNotification> detached = new ArrayList<MsoNotification>();
        try {
            Query query = pm.newQuery(MsoNotification.class);
            query.setFilter("msoId == msoIdParam");
            query.setOrdering("updateDate desc");
            query.declareParameters("long msoIdParam");                
            @SuppressWarnings("unchecked")
            List<MsoNotification> results = (List<MsoNotification>) query.execute(mso.getId());
            detached = (List<MsoNotification>)pm.detachCopyAll(results);
        } finally {
            pm.close();
        }
        return detached;        
    }
    
}