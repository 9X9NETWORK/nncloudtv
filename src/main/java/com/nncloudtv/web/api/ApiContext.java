package com.nncloudtv.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.base.Joiner;
import com.nncloudtv.lib.CookieHelper;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.LocaleTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.web.json.cms.Set;
import com.nncloudtv.web.json.cms.User;

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
    
    public static final short HTTP_200 = 200;
    public static final short HTTP_201 = 201;
    public static final short HTTP_400 = 400;
    public static final short HTTP_401 = 401;
    public static final short HTTP_403 = 403;
    public static final short HTTP_404 = 404;
    public static final short HTTP_500 = 500;
    
    public static final String TITLECARD_NOT_FOUND    = "TitleCard Not Found";
    public static final String PROGRAM_NOT_FOUND      = "Program Not Found";
    public static final String CATEGORY_NOT_FOUND     = "Category Not Found";
    public static final String SET_NOT_FOUND          = "Set Not Found";
    public static final String EPISODE_NOT_FOUND      = "Episode Not Found";
    public static final String USER_NOT_FOUND         = "User Not Found";
    public static final String CHANNEL_NOT_FOUND      = "Channel Not Found";
    public static final String MSO_NOT_FOUND          = "Mso Not Found";
    public static final String MISSING_PARAMETER      = "Missing Parameter";
    public static final String INVALID_PATH_PARAMETER = "Invalid Path Parameter";
    public static final String INVALID_PARAMETER      = "Invalid Parameter";
    public static final String INVALID_YOUTUBE_URL    = "Invalid YouTube URL";
    
    public static final String VND_APPLE_MPEGURL      = "application/vnd.apple.mpegurl";
    public static final String PLAIN_TEXT_UTF8        = "text/plain; charset=utf-8";
    public static final String APPLICATION_JSON_UTF8  = "application/json; charset=utf-8";
    
    public static final String OK                     = "OK";
    public static final String NULL                   = "null";
    public static final String BLACK_HOLE             = "Black Hole!";
    public static final String API_REF_URL            = "http://goo.gl/necjp"; // API reference document url
    public static final String API_REF                = "X-API-REF";
    public static final String API_DOC_URL            = "http://goo.gl/H7Jzl"; // API design document url
    public static final String API_DOC                = "X-API-DOC";
    
    protected HttpServletRequest httpReq;
    
    protected Locale language;
    protected Integer version;
    protected String appVersion;
    protected String os;
    protected String root;
    protected Mso mso;
    protected Boolean productionSite = null;
    protected short format;
    
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
    
    public ApiContext() {
        
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        init(req);
    }
    
    public ApiContext(HttpServletRequest req) {
        
        init(req);
    }
    
    private void init(HttpServletRequest req) {
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
    
    public void unauthorized(HttpServletResponse resp, String message) {
        try {
            resp.resetBuffer();
            resp.setContentType(PLAIN_TEXT_UTF8);
            resp.setHeader(API_DOC, API_DOC_URL);
            resp.setHeader(API_REF, API_REF_URL);
            if (message != null) {
                log.warning(message);
                resp.getWriter().println(message);
            }
            resp.setStatus(HTTP_401);
            resp.flushBuffer();
        } catch (IOException e) {
            internalError(resp, e);
        }
    }
    
    public void unauthorized(HttpServletResponse resp) {
        
        unauthorized(resp, null);
    }
    
    public void forbidden(HttpServletResponse resp, String message) {
        
        try {
            resp.resetBuffer();
            resp.setContentType(PLAIN_TEXT_UTF8);
            resp.setHeader(API_DOC, API_DOC_URL);
            resp.setHeader(API_REF, API_REF_URL);
            if (message != null) {
                log.warning(message);
                resp.getWriter().println(message);
            }
            resp.setStatus(HTTP_403);
            resp.flushBuffer();
        } catch (IOException e) {
            internalError(resp, e);
        }
    }
    
    public void forbidden(HttpServletResponse resp) {
        
        forbidden(resp, null);
    }
    
    public void notFound(HttpServletResponse resp, String message) {
        
        try {
            resp.resetBuffer();
            resp.setContentType(PLAIN_TEXT_UTF8);
            resp.setHeader(API_DOC, API_DOC_URL);
            resp.setHeader(API_REF, API_REF_URL);
            if (message != null) {
                log.warning(message);
                resp.getWriter().println(message);
            }
            resp.setStatus(HTTP_404);
            resp.flushBuffer();
        } catch (IOException e) {
            internalError(resp, e);
        }
        
    }
    
    public void notFound(HttpServletResponse resp) {
        
        notFound(resp, null);
    }
    
    public void badRequest(HttpServletResponse resp) {
        
        badRequest(resp, null);
    }
    
    public void badRequest(HttpServletResponse resp, String message) {
        
        try {
            resp.resetBuffer();
            resp.setContentType(PLAIN_TEXT_UTF8);
            resp.setHeader(API_DOC, API_DOC_URL);
            resp.setHeader(API_REF, API_REF_URL);
            if (message != null) {
                log.warning(message);
                resp.getWriter().println(message);
            }
            resp.setStatus(400);
            resp.flushBuffer();
        } catch (IOException e) {
            internalError(resp, e);
        }
        
    }
    
    public void internalError(HttpServletResponse resp) {
        
        internalError(resp, null);
    }
    
    @ExceptionHandler(Exception.class)
    public void internalError(HttpServletResponse resp, Exception e) {
        
        try {
            resp.resetBuffer();
            resp.setContentType(PLAIN_TEXT_UTF8);
            resp.setHeader(API_DOC, API_DOC_URL);
            resp.setHeader(API_REF, API_REF_URL);
            PrintWriter writer = resp.getWriter();
            if (e != null) {
                NnLogUtil.logException(e);
                writer.println(e.getMessage());
            }
            resp.setStatus(HTTP_500);
            resp.flushBuffer();
        } catch (Exception ex) {
        }
    }
    
    public void msgResponse(HttpServletResponse resp, String msg) {
    
        try {
            resp.setContentType(APPLICATION_JSON_UTF8);
            resp.getWriter().print(NnStringUtil.escapeDoubleQuote(msg));
            resp.flushBuffer();
        } catch (IOException e) {
            internalError(resp, e);
        }
    }
    
    public void nullResponse(HttpServletResponse resp) {
        
        try {
            resp.setContentType(APPLICATION_JSON_UTF8);
            resp.getWriter().print(NULL);
            resp.flushBuffer();
        } catch (IOException e) {
            internalError(resp, e);
        }
        
    }
    
    public User response(NnUser nnuser) {
        
        User user = new User();
        
        user.setId(nnuser.getId());
        user.setType(nnuser.getType());
        user.setShard(nnuser.getShard());
        user.setIdStr(nnuser.getIdStr());
        user.setCreateDate(nnuser.getCreateDate());
        user.setUpdateDate(nnuser.getUpdateDate());
        user.setUserEmail(nnuser.getUserEmail());
        user.setFbUser(nnuser.isFbUser());
        user.setPriv(NnUserProfile.PRIV_CMS);
        
        NnUserProfile profile = nnuser.getProfile();
        if (profile != null) {
            user.setName(NnStringUtil.revertHtml(profile.getName()));
            user.setIntro(NnStringUtil.revertHtml(profile.getIntro()));
            user.setImageUrl(profile.getImageUrl());
            user.setLang(profile.getLang());
            user.setProfileUrl(profile.getProfileUrl());
            user.setSphere(profile.getSphere());
            user.setCntSubscribe(profile.getCntSubscribe());
            user.setCntChannel(profile.getCntChannel());
            user.setCntFollower(profile.getCntFollower());
            user.setMsoId(profile.getMsoId());
            if (profile != null)
                user.setPriv(profile.getPriv());
            Mso mso = NNF.getMsoMngr().findById(profile.getMsoId());
            if (mso != null)
                user.setMsoName(mso.getName());
        }
        
        return user;
    }
    
    /** compose set response **/
    public Set response(SysTag set, SysTagDisplay setMeta) {
        
        Set setResp = new Set();
        
        setResp.setId(set.getId());
        setResp.setMsoId(set.getMsoId());
        setResp.setCntChannel(setMeta.getCntChannel());
        setResp.setLang(setMeta.getLang());
        setResp.setSeq(set.getSeq());
        setResp.setTag(setMeta.getPopularTag());
        setResp.setName(NnStringUtil.revertHtml(setMeta.getName()));
        setResp.setSortingType(set.getSorting());
        
        return setResp;
    }
}
