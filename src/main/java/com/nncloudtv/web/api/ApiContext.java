package com.nncloudtv.web.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import com.google.common.base.Joiner;
import com.nncloudtv.lib.CookieHelper;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.LocaleTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;

public class ApiContext {
    
    protected static final Logger log = Logger.getLogger(ApiContext.class.getName());
    
    @Override
    protected void finalize() throws Throwable {
        
        NnLogUtil.logFinalize(getClass().getName());
    }
    
    public final static String PRODUCTION_SITE_URL_REGEX = "^http(s)?:\\/\\/((cc|api|www)\\.)?(9x9|flipr)\\.tv$";
    
    public final static short  FORMAT_PLAIN      = 2;
    public final static short  FORMAT_JSON       = 1;
    public final static int    LATEST_VERSION    = 42;
    
    public final static String OS_ANDROID        = "android";
    public final static String OS_IOS            = "ios";
    public final static String OS_WEB            = "web";
    
    public final static int    DEFAULT_VERSION   = 31;
    public final static String DEFAULT_OS        = OS_WEB;
    
    public final static String HEADER_USER_AGENT = "User-Agent";
    public final static String HEADER_REFERRER   = "Referer";
    
    public final static String PARAM_APP_VERSION = "appver";
    public final static String PARAM_OS          = "os";
    public final static String PARAM_MSO         = "mso";
    public final static String PARAM_LANG        = "lang";
    public final static String PARAM_SPHERE      = "shpere";
    public final static String PARAM_REGION      = "region";
    public final static String PARAM_VERSION     = "v";
    public final static String PARAM_FORMAT      = "format";
    
    HttpServletRequest httpReq;
    Locale language;
    Integer version;
    String appVersion;
    String os;
    String root;
    Mso mso;
    Boolean productionSite = null;
    short format;
    
    public short getFmt() {
        return format;
    }
    
    public Integer getVer() {
        return version;
    }
    
    public String getAppVersion() {
    	return appVersion;
    }
    
    public String getOs() {
    	return os;
    }
    
    public Mso getMso() {
        return mso;
    }
    
    public long getMsoId() {
        return mso.getId();
    }
    
    public String getMsoName() {
        return mso.getName();
    }
    
    public String getLang() {
        return language.getLanguage();
    }
    
    public String getCookie(String cookieName) {
        return CookieHelper.getCookie(httpReq, cookieName);
    }
    
    public String getRoot() {
        return root;
    }
    
    public String getParam(String name) {
        
        return getParam(name, null);
    }
    
    public String getParam(String name, String defaultValue) {
        
        String value = httpReq.getParameter(name);
        
        return value == null ? defaultValue : value;
    }
    
    public ApiContext(HttpServletRequest req) {
        
        httpReq = req;
        String userAgent = httpReq.getHeader(HEADER_USER_AGENT);
        if (userAgent == null) userAgent = "";
        System.out.println("[ApiContext] user-agent = " + userAgent);
        
        this.format = FORMAT_JSON;
        String returnFormat = getParam(PARAM_FORMAT);
        if (returnFormat == null || returnFormat.isEmpty() || !returnFormat.equals("json")) {
            this.format = FORMAT_PLAIN;
        }
        
        String lang = getParam(PARAM_LANG);
        if (LocaleTable.isLanguageSupported(lang)) {
            language = LocaleTable.getLocaleFromLang(lang);
        } else {
            language = Locale.ENGLISH;
        }
        
        version = DEFAULT_VERSION;
        Integer versionInt = NnStringUtil.evalInt(getParam(PARAM_VERSION));
        if (versionInt != null) {
            version = versionInt;
        }
        
        os = getParam(PARAM_OS);
        if (os == null) {
            if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
                os = OS_IOS;
            } else if (userAgent.contains("Android")) { 
                os = OS_ANDROID;
            } else {
                os = DEFAULT_OS;
            }
        }
        
        appVersion = getParam(PARAM_APP_VERSION);
        if (appVersion != null)
            appVersion = os + " " + appVersion;
        
        root = NnNetUtil.getUrlRoot(httpReq);
        if (root.isEmpty()) {
            root = MsoConfigManager.getServerDomain();
        }
        mso = NNF.getMsoMngr().getByNameFromCache(getParam(PARAM_MSO));
        if (mso == null) {
            String domain = root.replaceAll("^http(s)?:\\/\\/", "");
            String[] split = domain.split("\\.");
            if (split.length > 2) {
                mso = NNF.getMsoMngr().findByName(split[0]);
            }
            if (mso == null) {
                mso = NNF.getMsoMngr().getByNameFromCache(Mso.NAME_9X9);
            }
        }
        
        System.out.println(String.format("[ApiContext] language = %s, mso = %s, version = %s, root = %s", language.getLanguage(), mso.getName(), version, root));
    }
    
    public Boolean isProductionSite() {
        
        if (productionSite != null) return productionSite; // speed up
        
        if (root == null || root.isEmpty()) {
            
            return (productionSite = false);
            
        } else if (root.matches(PRODUCTION_SITE_URL_REGEX)) {
            
            return (productionSite = true);
            
        } else {
            
            String domain = root.replaceAll("^http(s)?:\\/\\/", "");
            String[] splits = domain.split("\\.");
            if (splits.length == 3) {
                String subdomain = splits[0];
                log.info("sub-domain = " + subdomain);
                if (NNF.getMsoMngr().findByName(subdomain) != null) {
                    
                    return (productionSite = true);
                }
            }
        }
        return (productionSite = false);
    }
    
    public String getAppDomain() {
        
        String domain = root.replaceAll("^http(s)?:\\/\\/((cc|api|www)\\.)?", "");
        log.info("domain = " + domain);
        List<String> splits = new ArrayList<String>(Arrays.asList(domain.split("\\.")));
        
        if (splits.size() < 3)
            return MsoManager.isSystemMso(mso) ? "www." + domain : mso.getName() + "." + domain;
        
        log.info("sub-domain = " + splits.get(0));
        if (NNF.getMsoMngr().findByName(splits.get(0)) == null) {
            
            splits.remove(0);
        }
        String remain = Joiner.on(".").join(splits);
        
        return MsoManager.isSystemMso(mso) ? (splits.size() < 3 ? "www." + remain : remain) : mso.getName() + "." + remain;
    }
    
    public boolean isAndroid() {
        
        return OS_ANDROID.equalsIgnoreCase(os);
    }
    
    public boolean isIos() {
        
        return OS_IOS.equalsIgnoreCase(os);
    }
    
    public HttpServletRequest getReq() {
        
        return httpReq;
    }
    
    public static NnUser getAuthenticatedUser(HttpServletRequest req, long msoId) {
        
        String token = CookieHelper.getCookie(req, CookieHelper.USER);
        if (token == null) {
            log.info("not logged in");
            return null;
        }
        
        return NNF.getUserMngr().findByToken(token, msoId);
    }
    
    public static NnUser getAuthenticatedUser(HttpServletRequest req) {
        
        return getAuthenticatedUser(req, MsoManager.getSystemMsoId());
    }
    
    public NnUser getAuthenticatedUser(long msoId) {
        
        return getAuthenticatedUser(httpReq, msoId);
    }
    
    public NnUser getAuthenticatedUser() {
        
        return getAuthenticatedUser(httpReq, MsoManager.getSystemMsoId());
    }
}
