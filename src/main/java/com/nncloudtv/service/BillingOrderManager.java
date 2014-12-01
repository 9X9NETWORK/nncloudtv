package com.nncloudtv.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.BillingOrderDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.BillingOrder;

@Service
public class BillingOrderManager {
    
    protected static final Logger log = Logger.getLogger(BillingOrderManager.class.getName());
    
    protected BillingOrderDao dao = NNF.getOrderDao();
    
    public BillingOrder save(BillingOrder order) {
        
        if (order == null) return null;
        
        Date now = NnDateUtil.now();
        
        if (order.getCreateDate() == null) {
            
            order.setCreateDate(now);
        }
        order.setUpdateDate(now);
        
        return dao.save(order);
    }
    
    public Collection<BillingOrder> save(Collection<BillingOrder> orders) {
        
        if (orders == null) return null;
        
        Date now = NnDateUtil.now();
        
        for (BillingOrder order : orders) {
            
            if (order.getCreateDate() == null) {
                
                order.setCreateDate(now);
            }
            order.setUpdateDate(now);
        }
        
        return dao.saveAll(orders);
    }
    
    public List<BillingOrder> findByStatus(short status) {
        
        return dao.findByStatus(status);
    }
    
    public List<BillingOrder> findByIds(List<Long> ids) {
        
        return dao.findAllByIds(ids);
    }
}
