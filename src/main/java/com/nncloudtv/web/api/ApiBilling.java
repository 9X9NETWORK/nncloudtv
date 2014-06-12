package com.nncloudtv.web.api;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nncloudtv.model.billing.BillingOrder;
import com.nncloudtv.model.billing.BillingPackage;
import com.nncloudtv.model.billing.BillingProfile;
import com.nncloudtv.service.BillingPackageManager;

@Controller
@RequestMapping("api/billing")
public class ApiBilling extends ApiGeneric {
    
    protected static Logger log = Logger.getLogger(ApiBilling.class.getName());
    
    private BillingPackageManager packageMngr;
    //private BillingProfileManager profileMngr;
    //private BillingOrderManager   orderMngr;
    
    public ApiBilling() {
        
        packageMngr = new BillingPackageManager();
        //profileMngr = new BillingProfileManager();
        //orderMngr   = new BillingOrderManager();
    }
    
    @RequestMapping(value = "packages", method = RequestMethod.GET)
    public @ResponseBody List<BillingPackage> login(HttpServletRequest req, HttpServletResponse resp) {
        
        return packageMngr.findAll();
    }
    
    @RequestMapping(value = "profiles", method = RequestMethod.POST)
    public @ResponseBody BillingProfile billingProfileCreate(HttpServletRequest req, HttpServletResponse resp) {
        
        // Qoo
        
        
        
        return null;
    }
    
    @RequestMapping(value = "orders", method = RequestMethod.POST)
    public @ResponseBody BillingOrder billingOrderCreate(HttpServletRequest req, HttpServletResponse resp) {
        
        // Qoo
        
        
        
        return null;
    }
    
}
