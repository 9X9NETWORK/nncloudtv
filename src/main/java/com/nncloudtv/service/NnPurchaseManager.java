package com.nncloudtv.service;

import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnPurchaseDao;
import com.nncloudtv.lib.NNF;

@Service
public class NnPurchaseManager {
    
    protected static final Logger log = Logger.getLogger(NnPurchaseManager.class.getName());
    
    protected NnPurchaseDao dao = NNF.getPurchaseDao();
    
    
    
    
    
    
}
