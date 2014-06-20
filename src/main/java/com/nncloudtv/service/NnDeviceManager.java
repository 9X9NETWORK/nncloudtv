package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnDeviceDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.NnDevice;
import com.nncloudtv.model.NnDeviceNotification;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.web.json.player.PlayerDevice;

@Service
public class NnDeviceManager {
    protected static final Logger log = Logger.getLogger(NnDeviceManager.class.getName());
    
    private NnDeviceDao dao = new NnDeviceDao();
    private NnDeviceNotificationManager notificationMngr = new NnDeviceNotificationManager();
    private HttpServletRequest req;
    
    public HttpServletRequest getReq() { return req; }
    public void setReq(HttpServletRequest req) { this.req = req;}
    
    public void save(NnDevice device) {
        
        device.setUpdateDate(new Date());
        dao.save(device);
    }
    
    public void save(List<NnDevice> devices) {
        
        Date now = new Date();
        for (NnDevice device : devices) {
            device.setCreateDate(now);
        }
        dao.saveAll(devices);
    }
    
    //can create device based on user, or device
    public NnDevice create(NnDevice device, NnUser user, String type) {
        if (device != null && user != null) {
            NnDevice existed = this.findByTokenAndUser(device.getToken(), user); 
            if (existed != null) {
                log.info("found existed:" + device.getToken() + ";user:" + user.getToken());
                return existed;
            }
        }        
        if (device == null)
            device = new NnDevice();
        if (device.getToken() == null)
            device.setToken(NnUserManager.generateToken(NNF.getUserMngr().getShardByLocale(req)));
        if (user != null) {
            device.setUserId(user.getId());
            device.setShard(user.getShard()); //for future reference
            device.setMsoId(user.getMsoId());            
        }
        if (device.getMsoId() == 0) {
            //!!! problem
            device.setMsoId(1);
        }
        device.setType(type);
        Date now = new Date();
        if (device.getCreateDate() == null)
            device.setCreateDate(now);
        device.setUpdateDate(now);       
        device = dao.save(device);
        return device;
    }
    
    public NnDevice findByTokenAndUser(String token, NnUser user) {
        return dao.findByTokenAndUser(token, user);
    }

    //find a device that's not associated with any user account, which is user id = 0
    public NnDevice findDeviceOpenToken(String token) {
        return dao.findDeviceOpenToken(token);
    }
    
    public List<NnDevice> findByToken(String token) {
        return dao.findByToken(token);
    }

    public List<NnDevice> findByUser(NnUser user) {
        return dao.findByUser(user);
    }
    
    public NnDevice addUser(String token, NnUser user) {        
        List<NnDevice> devices = this.findByToken(token);        
        if (devices.size() == 0)
            return null;
        NnDevice existed = this.findDeviceOpenToken(token);
        if (existed != null) {
            existed.setUserId(user.getId());            
            dao.save(existed);
            return existed;
        }
        existed = this.findByTokenAndUser(token, user);
        if (existed != null) {
            log.info("found existed device and user: device token: " + token + ";user id:" + user.getId());
            return existed;
        }        
        NnDevice device = new NnDevice();
        device.setToken(devices.get(0).getToken());
        this.create(device, user, null);
        return device;
    }    
    
    public void delete(NnDevice device) {
        
        List<NnDeviceNotification> notifications = notificationMngr.findByDeviceId(device.getId());
        
        dao.delete(device);
        notificationMngr.delete(notifications);
    }
    
    public void delete(List<NnDevice> deleteDevices) {
        
        List<NnDeviceNotification> notifications = new ArrayList<NnDeviceNotification>();
        
        for (NnDevice device : deleteDevices) {
            notifications.addAll(notificationMngr.findByDeviceId(device.getId()));
        }
        
        dao.deleteAll(deleteDevices);
        notificationMngr.delete(notifications);
    }
    
    public Object getPlayerDeviceInfo(NnDevice device, short format, List<NnUser> users) {
    	String token = device.getToken();
        if (format == PlayerApiService.FORMAT_PLAIN) {
            String[] result = {token};
        	if (users != null) {
                for (NnUser u : users) {
                    result[0] += u.getToken() + "\t" + u.getProfile().getName() + "\t" + u.getUserEmail() + "\n";
                }    		
        	}
            return result;
        } else {
        	PlayerDevice json = new PlayerDevice();
        	json.setToken(token);
        	if (users != null) {
                for (NnUser u : users) {
                    json.getUsers().add(u.getToken() + "\t" + u.getProfile().getName() + "\t" + u.getUserEmail());
                }    		
        	}        	
        	return json;
        }    	
    }
    
    public boolean removeUser(String token, NnUser user) {
        List<NnDevice> devices = this.findByToken(token);
        if (devices.size() == 0)
            return false;
        NnDevice existed = this.findByTokenAndUser(token, user);
        if (existed == null)
            return false;
        this.delete(existed);
        return true;
    }
    
    public NnDevice findDuplicated(String token, long msoId, String type) {
        
        return dao.findDuplicated(token, msoId, type);
    }
    
    public List<NnDevice> findByMsoAndType(long msoId, String type) {
        
        return dao.findByMsoAndType(msoId, type);
    }
}
