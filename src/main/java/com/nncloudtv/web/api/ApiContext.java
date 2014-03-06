package com.nncloudtv.web.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Joiner;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;


public class ApiContext {
    
    public final static String PRODUCTION_SITE_URL_REGEX = "^http(s)?:\\/\\/(www\\.)?9x9\\.tv$";
    public final static String DEFAULT_VERSION = "31";
    public final static String HEADER_USER_AGENT = "user-agent";
    public final static String HEADER_REFERRER = "referer";
    
    public final static String PARAM_MSO = "mso";
    public final static String PARAM_LANG = "lang";
    public final static String PARAM_SPHERE = "shpere";
    public final static String PARAM_VERSION = "v";
    
    MsoManager msoMngr;
    
    HttpServletRequest httpReq;
    Locale locale;
    Integer version;
    String root;
    Mso mso;
    
    public Integer getVersion() {
        
        return version;
    }
    
    public Locale getLocale() {
        
        return locale;
    }
    
    public Mso getMso() {
    
        return mso;
    }
    
    protected static final Logger log = Logger.getLogger(ApiContext.class.getName());
    
    @Autowired
    public ApiContext(HttpServletRequest req) {
        
        msoMngr = new MsoManager();
        init(req);
    }
    
    @Autowired
    public ApiContext(HttpServletRequest req, MsoManager mngr) {
        
        msoMngr = mngr;
        init(req);
    }
    
    private void init(HttpServletRequest req) {
        
        httpReq = req;
        log.info("user agent = " + req.getHeader(ApiContext.HEADER_USER_AGENT));
        
        String lang = httpReq.getParameter(ApiContext.PARAM_LANG);
        if (LangTable.isValidLanguage(lang)) {
            locale = LangTable.getLocale(lang);
        } else {
            locale = Locale.ENGLISH; // TODO: from http request
        }
        
        version = Integer.parseInt(ApiContext.DEFAULT_VERSION);
        String versionStr = httpReq.getParameter(PARAM_VERSION);
        if (versionStr != null) {
            try {
                version = Integer.parseInt(versionStr);
            } catch (NumberFormatException e) {
            }
        }
        
        root = NnNetUtil.getUrlRoot(httpReq);
        if (root == "") {
            root = MsoConfigManager.getServerDomain();
        }
        mso = msoMngr.getByNameFromCache(httpReq.getParameter(ApiContext.PARAM_MSO));
        if (mso == null) {
            String domain = root.replaceAll("^http(s)?:\\/\\/", "");
            String[] split = domain.split("\\.");
            if (split.length > 2) {
                log.info("sub-domain = " + split[0]);
                mso = msoMngr.findByName(split[0]);
            }
            if (mso == null) {
                mso = msoMngr.getByNameFromCache(Mso.NAME_9X9);
            }
        }
        
        log.info("language = " + locale.getLanguage() + "; mso = " + mso.getName() + "; version = " + version + "; root = " + root);
    }
    
    public Boolean isProductionSite() {
        
        if (root == null || root.isEmpty()) {
            
            return false;
            
        } else if (root.matches(ApiContext.PRODUCTION_SITE_URL_REGEX)) {
            
            return true;
            
        } else {
            
            String domain = root.replaceAll("^http(s)?:\\/\\/", "");
            String[] splits = domain.split("\\.");
            if (splits.length == 3) {
                String subdomain = splits[0];
                log.info("subdomain = " + subdomain);
                if (msoMngr.findByName(subdomain) != null)
                    return true;
            }
        }
        
        return false;
    }
    
    public String getAppDomain() {
        
        String domain = root.replaceAll("^http(s)?:\\/\\/(www\\.)?", "");
        log.info("domain = " + domain);
        List<String> splits = new ArrayList<String>(Arrays.asList(domain.split("\\.")));
        
        if (splits.size() < 3)
            return MsoManager.isNNMso(mso) ? "www." + domain : mso.getName() + "." + domain;
        
        log.info("subdomain = " + splits.get(0));
        if (msoMngr.findByName(splits.get(0)) != null) {
            
            splits.remove(0);
        }
        String remain = Joiner.on(".").join(splits);
        
        return MsoManager.isNNMso(mso) ? (splits.size() < 3 ? "www." + remain : remain) : mso.getName() + "." + remain;
    }
    
    public boolean isAndroid() {
        
        String userAgent = httpReq.getHeader(ApiContext.HEADER_USER_AGENT);
        if (userAgent.contains("Android")) {
            log.info("request from Android");
            return true;            
        }        
        return false;
    }
    
    public boolean isIos() {
        
        String userAgent = httpReq.getHeader(ApiContext.HEADER_USER_AGENT);
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            log.info("request from ios");
            return true;            
        }        
        return false;
    }
}
