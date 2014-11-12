package com.nncloudtv.web;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.web.api.ApiContext;

@Controller
@RequestMapping("notify")
public class NotificationController {
    
    protected static final Logger log = Logger.getLogger(NotificationController.class.getName());
    
    @RequestMapping(value="apns")
    public ResponseEntity<String> apns(@RequestParam(required=true) Long id, HttpServletRequest req, HttpServletResponse resp) {
        
        log.info("notifyId = " + id);
        
        ApiContext context = new ApiContext();
        
        // APNs push notification
        NNF.getNotiService().sendToAPNS(id, context.isProductionSite());
        
        return NnNetUtil.textReturn("OK");
    }
    
    @RequestMapping(value="gcm")
    public ResponseEntity<String> gcm(@RequestParam(required=true) Long id, HttpServletRequest req, HttpServletResponse resp) {
        
        log.info("notifyId = " + id);
        
        // GCM push notification
        NNF.getNotiService().sendToGCM(id);
        
        return NnNetUtil.textReturn("OK");
    }
}
