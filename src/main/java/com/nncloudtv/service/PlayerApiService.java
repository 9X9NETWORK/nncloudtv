package com.nncloudtv.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.mysql.jdbc.CommunicationsException;
import com.nncloudtv.dao.AppDao;
import com.nncloudtv.dao.UserInviteDao;
import com.nncloudtv.dao.YtProgramDao;
import com.nncloudtv.lib.AmazonLib;
import com.nncloudtv.lib.AuthLib;
import com.nncloudtv.lib.CookieHelper;
import com.nncloudtv.lib.FacebookLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.QueueFactory;
import com.nncloudtv.lib.SearchLib;
import com.nncloudtv.lib.VimeoLib;
import com.nncloudtv.lib.YouTubeLib;
import com.nncloudtv.model.App;
import com.nncloudtv.model.Captcha;
import com.nncloudtv.model.EndPoint;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.MsoIpg;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnContent;
import com.nncloudtv.model.NnDevice;
import com.nncloudtv.model.NnDeviceNotification;
import com.nncloudtv.model.NnEmail;
import com.nncloudtv.model.NnEpisode;
import com.nncloudtv.model.NnGuest;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserChannelSorting;
import com.nncloudtv.model.NnUserPref;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.model.NnUserReport;
import com.nncloudtv.model.NnUserShare;
import com.nncloudtv.model.NnUserSubscribe;
import com.nncloudtv.model.NnUserSubscribeGroup;
import com.nncloudtv.model.Poi;
import com.nncloudtv.model.PoiEvent;
import com.nncloudtv.model.PoiPdr;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.model.Tag;
import com.nncloudtv.model.UserInvite;
import com.nncloudtv.model.YtProgram;
import com.nncloudtv.validation.BasicValidator;
import com.nncloudtv.validation.NnUserValidator;
import com.nncloudtv.web.api.ApiContext;
import com.nncloudtv.web.api.NnStatusCode;
import com.nncloudtv.web.json.facebook.FacebookMe;
import com.nncloudtv.web.json.player.ApiStatus;
import com.nncloudtv.web.json.player.Category;
import com.nncloudtv.web.json.player.ChannelLineup;
import com.nncloudtv.web.json.player.PlayerChannelLineup;
import com.nncloudtv.web.json.player.PlayerProgramInfo;
import com.nncloudtv.web.json.player.ProgramInfo;
import com.nncloudtv.web.json.player.RelatedApp;
import com.nncloudtv.web.json.player.Search;
import com.nncloudtv.web.json.player.SubscribeSetInfo;
import com.nncloudtv.web.json.player.UserInfo;

@Service
public class PlayerApiService {
    
    protected static final Logger log = Logger.getLogger(PlayerApiService.class.getName());
    
    public final static int   LATEST_VERSION = 42;
    public final static short FORMAT_JSON    = 1;
    public final static short FORMAT_PLAIN   = 2;
    public final static int   PAGING_ROWS    = 50;
    public final static int   MAX_EPISODES   = 200;
    
    private Mso    mso;
    private int    version = 32;
    private Locale locale  = Locale.ENGLISH;
    private short  format  = FORMAT_PLAIN;
    private ApiContext context = null;
    HttpServletResponse resp;
    
    public int prepService(HttpServletRequest req, HttpServletResponse resp) {
        
        return prepService(req, resp, true);
    }
    
    public int prepService(HttpServletRequest req, HttpServletResponse resp, boolean toLog) {
        
        this.resp = resp;
        
        String returnFormat = req.getParameter("format");
        if (returnFormat == null || (returnFormat != null && !returnFormat.contains("json"))) {
            this.format = FORMAT_PLAIN;
        } else {
            this.format = FORMAT_JSON;
        }
        
        req.getSession().setMaxInactiveInterval(60);
        
        if (toLog) 
            NnNetUtil.logUrl(req);
        
        context = new ApiContext(req);
        this.locale  = context.getLocale();
        this.mso     = context.getMso();
        this.version = context.getVersion();
        log.info("mso entrance: " + mso.getId());
        
        if (this.version < checkApiMinimal())
            return NnStatusCode.API_FORCE_UPGRADE;
        else
            return checkRO();
    }
    
    public Mso getMso() {
        return mso;
    }
    
    public Object response(Object output) {
        
        if (this.format == FORMAT_PLAIN) {
            try {
                resp.setContentType("text/plain;charset=utf-8");                
                resp.getWriter().print(output);
                resp.flushBuffer();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }        
        return output;        
    }
        
    public Object handleException (Exception e) {
        if (e.getClass().equals(NumberFormatException.class)) {
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);            
        } else if (e.getClass().equals(CommunicationsException.class)) {
            log.info("return db error");
            return this.assembleMsgs(NnStatusCode.DATABASE_ERROR, null);
        } 
        NnLogUtil.logException(e);
        return this.assembleMsgs(NnStatusCode.ERROR, null);
    }    

    public long addMsoInfoVisitCounter(boolean readOnly) {
        if (!readOnly) {
            if (NNF.getConfigMngr().isQueueEnabled(true)) {
            } else {
                log.info("quque not enabled");
                return NNF.getMsoMngr().addMsoVisitCounter(readOnly);
            }
        }
        return NNF.getMsoMngr().addMsoVisitCounter(readOnly);                                     
    }
            
    //assemble key and value string
    public static String assembleKeyValue(String key, String value) {
        return key + "\t" + value + "\n";
    }
    
    /**
     * assemble final output to player 
     * 1. status line in the front
     * 2. raw: for each section needs to be separated by separator string, "--\n"
     */
    public Object assembleMsgs(int status, Object data) {
        if (this.format == FORMAT_JSON) {
            ApiStatus apiStatus = new ApiStatus();
            apiStatus.setCode(status);
            apiStatus.setMessage(NnStatusMsg.getPlayerMsgText(status, locale));
            apiStatus.setData(data);
            return apiStatus;
        }
        String result = NnStatusMsg.getPlayerMsg(status, locale);
        String[] raw = (String[]) data;        
        String separatorStr = "--\n";
        if (raw != null && raw.length > 0) {
            result = result + separatorStr;
            for (String s : raw) {
                if (s != null) {
                    s = s.replaceAll("null", "");
                }
                result += s + separatorStr;
            }
        }
        if (result.substring(result.length()-3, result.length()).equals(separatorStr)) {
            result = result.substring(0, result.length()-3);
        }
        return result;
    }
    
    //Prepare user info, it is used by login, guest register, userTokenVerify
    //object, UserInfo
    public Object prepareUserInfo(NnUser user, NnGuest guest, HttpServletRequest req, boolean login) {
        return NNF.getUserMngr().getPlayerUserInfo(user, guest, req, login, this.format);
    }

    public void setUserCookie(HttpServletResponse resp, String cookieName, String userId) {        
        CookieHelper.setCookie(resp, cookieName, userId);
    }    
    public Object relatedApps(String mso, String os, String stack, String sphere, HttpServletRequest req) {     
        sphere = this.checkLang(sphere);    
        ApiContext context = new ApiContext(req);
        short type = App.TYPE_IOS;
        if (os == null) {      
           if (context.isAndroid()) {
              type = App.TYPE_ANDROID;
           }
        } else {
            if (os.equals("android"))
                type = App.TYPE_ANDROID;
        }
        AppDao dao = new AppDao();
        
        List<App> featuredApps = dao.findFeaturedBySphere(sphere, this.mso.getId());
        List<App> apps = new ArrayList<App>();
        apps.addAll(featuredApps);
        apps.addAll(dao.findAllBySphere(sphere, this.mso.getId()));
        /*
          if (stack != null && stack.equals("featured")) {
              apps.addAll(dao.findFeaturedByOsAndSphere(type, sphere));
          } else {
            apps.addAll(dao.findAllByOsAndSphere(type, sphere));
          }
       */
        String[] result = {"", ""};
        List<App> myapps = new ArrayList<App>();
        myapps.addAll(featuredApps);
        for (int i=0; i<2; i++) {
           if (i==1) {
               myapps.clear();
               myapps.addAll(apps);
           }
           for (App a : myapps) {
               String storeUrl = a.getIosStoreUrl();
               if (type == App.TYPE_ANDROID)
                   storeUrl = a.getAndroidStoreUrl();
               String[] obj = {
                 a.getName(),
                 a.getIntro(),
                 a.getImageUrl(),
                 storeUrl,
                 a.getMsoName(),
                 a.getAndroidPackageName(),
              };
              if (storeUrl != null)
                 result[i] += NnStringUtil.getDelimitedStr(obj) + "\n";
           }         
        }
        if (format == PlayerApiService.FORMAT_JSON)
            return this.assembleMsgs(NnStatusCode.SUCCESS, new RelatedApp());
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
     }
    
    public Object listRecommended(String lang) {
        lang = this.checkLang(lang);    
        if (lang == null)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        List<SysTagDisplay> sets = new ArrayList<SysTagDisplay>();
        if (version < 40) {
            sets.addAll(NNF.getDisplayMngr().find33RecommendedSets(lang, mso.getId()));
        } else {
            sets.addAll(NNF.getDisplayMngr().findRecommendedSets(lang, mso.getId()));            
        }
        String[] result = {""};
        for (SysTagDisplay set : sets) {
            String[] obj = {
                String.valueOf(set.getId()),
                set.getName(),
                "",
                set.getImageUrl(),
                String.valueOf(set.getCntChannel()),
            };
            result[0] += NnStringUtil.getDelimitedStr(obj) + "\n";          
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);        
    }
    
    public Object fbDeviceSignup(FacebookMe me, String expire, String msoString, HttpServletRequest req, HttpServletResponse resp) {
        if (me.getAccessToken() == null || me.getId() == null || me.getEmail() == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }        
        return this.fbSignup(me, expire, msoString, req, resp);
    }
    
    private Object fbSignup(FacebookMe me, String expires, String msoName, HttpServletRequest req, HttpServletResponse resp) {
        long expire = 0;
        if (expires != null && expires.length() > 0)
            expire = Long.parseLong(expires); //TODO pass to FacebookMe
        
        Mso mso = NNF.getMsoMngr().findByName(msoName);
        if (mso == null) {
           mso = MsoManager.getSystemMso();
        }
        NnUserManager userMngr = NNF.getUserMngr();
        NnUser user = userMngr.findByEmail(me.getId(), mso.getId(), req);        
        log.info("find user in db from fbId:" + me.getId());
        if (user == null) {
            log.info("FACEBOOK: signup with fb account:" + me.getEmail() + "(" + me.getId() + ")");
            user = new NnUser(me.getEmail(), me.getId(), me.getAccessToken());
            NnUserProfile profile = new NnUserProfile(mso.getId(), me.getName(), null, null, null);
            user.setProfile(profile);            
            user.setExpires(new Date().getTime() + expire);
            user.setTemp(false);
            user.setMsoId(mso.getId());
            user = userMngr.setFbProfile(user, me);
            int status = userMngr.create(user, req, (short)0);
            if (status != NnStatusCode.SUCCESS)
                return this.assembleMsgs(status, null);            
            //userMngr.subscibeDefaultChannels(user);
        } else {
            user = userMngr.setFbProfile(user, me);            
            log.info("FACEBOOK: original FB user login with fbId - " + user.getEmail() + ";email:" + user.getFbId());
            userMngr.save(user);
        }        
        
        Object result = this.prepareUserInfo(user, null, req, true);
        this.setUserCookie(resp, CookieHelper.USER, user.getToken());
        if (this.format == PlayerApiService.FORMAT_PLAIN) {
            String[] value = {(String) result};
            return this.assembleMsgs(NnStatusCode.SUCCESS, value);                        
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);        
    }
        
    //TODO move to usermanager
    public Object fbWebSignup(String accessToken, String expires, String msoString, HttpServletRequest req, HttpServletResponse resp) {
        log.info("msoString:" + msoString);
        FacebookMe me = new FacebookLib().getFbMe(accessToken);
        return this.fbSignup(me, expires, msoString, req, resp);
    }
    
    public Object signup(String email, String password, String name, String token,
                         String captchaFilename, String captchaText,
                         String sphere, String lang,
                         String year, String gender,
                         boolean isTemp,
                         HttpServletRequest req, HttpServletResponse resp) {        
        //validate basic inputs
        int status = NnUserValidator.validate(email, password, name, req);
        if (status != NnStatusCode.SUCCESS) 
            return this.assembleMsgs(status, null);
        lang = this.checkLang(lang);    
        sphere = this.checkLang(sphere);
        if (lang == null || sphere == null)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);                
        
        //convert from guest
        NnGuestManager guestMngr = new NnGuestManager();
        NnGuest guest = guestMngr.findByToken(token);
        if (guest == null && captchaFilename != null) {
            log.info("such guest does not exist, where does this token from");
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
        }
        if (guest != null) {
            if (captchaFilename != null || captchaText != null) {
                status = this.checkCaptcha(guest, captchaFilename, captchaText);
                if (status != NnStatusCode.SUCCESS) 
                    return this.assembleMsgs(status, null);
            }
        }
        //Convert email "tdell9x9@gmail.com" to "tdell9x9-AT-gmail.com@9x9.tv",
        //This logic is weak, make something else in the future
        short type = NnUser.TYPE_USER;
        if ((email.contains("-AT-") || email.contains("-at-"))
                && email.contains("@9x9.tv")) {
            type = NnUser.TYPE_YOUTUBE_CONNECT;
        }        
        NnUser user = new NnUser(email, password, type, mso.getId());
        user.setTemp(isTemp);        
        NnUserProfile profile = new NnUserProfile(mso.getId(), name, sphere, lang, year);
        if (gender != null)
            profile.setGender(Short.parseShort(gender));                        
        user.setProfile(profile);
        status = NNF.getUserMngr().create(user, req, (short)0);
        if (status != NnStatusCode.SUCCESS)
            return this.assembleMsgs(status, null);
        
        //userMngr.subscibeDefaultChannels(user);
        /*
        String[] result = {(String) this.prepareUserInfo(user, null, req, false)};        
        this.setUserCookie(resp, CookieHelper.USER, user.getToken());
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
        */
        
        Object result = this.prepareUserInfo(user, guest, req, true);
        this.setUserCookie(resp, CookieHelper.USER, user.getToken());
        if (this.format == PlayerApiService.FORMAT_PLAIN) {
            String[] value = {(String) result};
            return this.assembleMsgs(NnStatusCode.SUCCESS, value);                        
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);        
    }
    
    public int checkRO() {
        
        MsoConfig config = NNF.getConfigMngr().findByItem(MsoConfig.RO);
        if (config != null && config.getValue().equals("1"))            
            return NnStatusCode.DATABASE_READONLY;
        return NnStatusCode.SUCCESS;
    }
    
    public int checkApiMinimal() {
        
        MsoConfig config = NNF.getConfigMngr().findByItem(MsoConfig.API_MINIMAL);  
        if (config == null)
            return 0;
        return Integer.parseInt(config.getValue());
    }
    
    public Object guestRegister(HttpServletRequest req, HttpServletResponse resp) {
        //verify input        
        NnGuestManager mngr = new NnGuestManager();
        NnGuest guest = new NnGuest(NnGuest.TYPE_GUEST);
        mngr.save(guest, req);        

        Object result = mngr.getPlayerGuestRegister(guest, this.format, req);
        this.setUserCookie(resp, CookieHelper.USER, guest.getToken());
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);        
    }
    
    public Object userTokenVerify(String token, HttpServletRequest req, HttpServletResponse resp) {
        if (token == null) {return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);}
        
        NnUserManager userMngr = NNF.getUserMngr();
        NnGuestManager guestMngr = new NnGuestManager();
        NnUser user = userMngr.findByToken(token, mso.getId());
        NnGuest guest = guestMngr.findByToken(token);
        if (user == null && guest == null) {
            CookieHelper.deleteCookie(resp, CookieHelper.USER);
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
        }
        if (user != null) {
            if (user.getEmail().equals(NnUser.GUEST_EMAIL))
                return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
            if (user.isFbUser()) {
                FacebookLib lib = new FacebookLib();
                FacebookMe me = lib.getFbMe(user.getToken());
                if (me.getStatus() == FacebookMe.STATUS_ERROR) {
                    log.info("FACEBOOK: access token invalid");
                    CookieHelper.deleteCookie(resp, CookieHelper.USER);
                    return this.assembleMsgs(NnStatusCode.USER_INVALID, null);                    
                } else {
                    user = userMngr.setFbProfile(user, me);
                    log.info("reset fb info:" + user.getId());
                }
            }
            userMngr.save(user); //FIXME: change last login time (ie updateTime)
        }
        Object result = this.prepareUserInfo(user, guest, req, true);
        if (this.format == PlayerApiService.FORMAT_PLAIN) {
            String[] value = {(String) result};
            return this.assembleMsgs(NnStatusCode.SUCCESS, value);                        
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object category(String id, String lang, boolean flatten) {
        lang = this.checkLang(lang);    
        if (lang == null)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);                        
        if (id == null)
            id = "0";
        String[] result = {"", "", ""};
        result[0] = "id" + "\t" + id + "\n";
        
        if (!id.equals("0")) { //v30 compatibilities            
            long displayId = Long.parseLong(id);
            SysTagDisplay display = NNF.getDisplayMngr().findById(displayId);
            List<NnChannel> channels = NNF.getSysTagMngr().findPlayerChannelsById(display.getSystagId(), display.getLang(), 0, 200, SysTag.SORT_DATE, mso.getId());  
            result[2] = (String) NNF.getChannelMngr().composeChannelLineup(channels, version, this.format);
            return this.assembleMsgs(NnStatusCode.SUCCESS, result);
        }        
        
        MsoConfig mask = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.SYSTEM_CATEGORY_MASK);
        boolean disableAll = false;
        HashMap<Long, Long> map = new HashMap<Long, Long>();
        if (mask != null && mask.getValue() != null && mask.getValue().length() > 0) {
            String maskStr = mask.getValue();
            String[] str = maskStr.split(",");            
            for (int i=0; i<str.length; i++) {
                if (str[i].equalsIgnoreCase(MsoConfig.DISABLE_ALL_SYSTEM_CATEGORY)) {
                    disableAll = true;
                    i = str.length+1;
                } else { 
                    Long number = Long.parseLong(str[i].trim());
                    map.put(number, number);
                }
            }
        }
        List<SysTagDisplay> categories = new ArrayList<SysTagDisplay>();
        categories.addAll(NNF.getDisplayMngr().findPlayerCategories(lang, mso.getId()));
        
        if (!disableAll && !MsoManager.isNNMso(mso)) {
            List<SysTagDisplay> systemCategories = NNF.getDisplayMngr().findPlayerCategories(lang, MsoManager.getSystemMsoId());
            categories.addAll(systemCategories);
            for (SysTagDisplay d : systemCategories) {
                if (map.get(d.getSystagId()) != null) {
                    log.info("removing category:" + d.getSystagId());
                    categories.remove(d);        
                }
            }
         }
        
        List<Category> playerCategories = new ArrayList<Category>();
        for (SysTagDisplay display : categories) {
            String cId = String.valueOf(display.getId());
            String name = display.getName();
            int cntChannel = display.getCntChannel();
            String subItemHint = "ch"; //what's under this level
            if (format == PlayerApiService.FORMAT_PLAIN) {
                String[] str = {cId, 
                                name, 
                                String.valueOf(cntChannel), 
                                subItemHint};                
                result[1] += NnStringUtil.getDelimitedStr(str) + "\n";
            } else {
                Category playerCategory = new Category();
                playerCategory.setId(id);
                playerCategory.setName(name);
                playerCategory.setNextLevel(subItemHint);
                playerCategories.add(playerCategory);
            }
        }
                         
        //flatten result process
        if (id.equals("0") && flatten && format == PlayerApiService.FORMAT_PLAIN) {
            log.info("return flatten data");
            List<String> flattenResult = new ArrayList<String>();
            flattenResult.add(result[0]);
            flattenResult.add(result[1]);
            String size[] = new String[flattenResult.size()];
            return this.assembleMsgs(NnStatusCode.SUCCESS, flattenResult.toArray(size));
        } else {
            if (format == PlayerApiService.FORMAT_PLAIN)
                return this.assembleMsgs(NnStatusCode.SUCCESS, result);
            else
                return this.assembleMsgs(NnStatusCode.SUCCESS, playerCategories);
        }
    }
     
    public Object brandInfo(String os, HttpServletRequest req) {
        //locale
        String locale = this.findLocaleByHttpRequest(req);
        long counter = 0;
        String acceptLang = req.getHeader("Accept-Language");
        String piwik = "http://piwik.9x9.tv/";
        Object result = NNF.getMsoMngr().getBrandInfo(req, mso, os, this.format, locale, counter, piwik, acceptLang);
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }    
    
    public String findLocaleByHttpRequest(HttpServletRequest req) {
        
        return NNF.getUserMngr().findLocaleByHttpRequest(req);
    }
    
    public Object pdr(String userToken, String deviceToken,             
                      String session, String pdr,
                      HttpServletRequest req) {
        if (userToken == null && deviceToken == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        if (pdr == null || pdr.length() == 0) 
            return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);    
        
        //pdr process
        PdrManager pdrMngr = new PdrManager(); 
        NnUser user = null;
        if (userToken != null) { 
            //verify input
            @SuppressWarnings("rawtypes")
            HashMap map = this.checkUser(userToken, false);
            user = (NnUser) map.get("u");
        }
        List<NnDevice> devices = new ArrayList<NnDevice>();
        NnDevice device = null;
        if (deviceToken != null) {
            devices = NNF.getDeviceMngr().findByToken(deviceToken);
            if (devices.size() > 0)
                device = devices.get(0);
        }
        if (device == null && user == null)
            return this.assembleMsgs(NnStatusCode.ACCOUNT_INVALID, null);
        
        String ip = NnNetUtil.getIp(req);        
        pdrMngr.processPdr(user, device, session, pdr, ip);
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }    
        
    public Object unsubscribe(String userToken, String channelId, String grid, String pos) {
        //verify input
        if (userToken == null || userToken.equals("undefined"))
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        if (channelId == null && pos == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        //verify user
        NnUser user = NNF.getUserMngr().findByToken(userToken, mso.getId());
        if (user == null)
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);        
        //unsubscribe
        NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
        if (channelId != null) {
            String[] chArr = channelId.split(",");
            if (chArr.length == 1) {
                log.info("unsubscribe single channel");
                NnUserSubscribe s = null;
                s = subMngr.findByUserAndChannel(user, channelId);
                /*
                if (grid == null) {
                    s = subMngr.findByUserAndChannel(user, Long.parseLong(channelId));
                } else {
                    s = subMngr.findChannelSubscription(user, Long.parseLong(channelId), Short.parseShort(grid));
                }
                */                
                subMngr.unsubscribeChannel(user, s);                
                /*
                NnUserWatchedManager watchedMngr = new NnUserWatchedManager();
                NnUserWatched watched = watchedMngr.findByUserTokenAndChannel(user.getToken(), Long.parseLong(channelId));
                if (watched != null) {
                    watchedMngr.delete(user, watched);
                } 
                */                               
            } else {
                log.info("unsubscribe multiple channels but not supported");
            }
        }               
        if (pos != null) {
            NnUserSubscribeGroupManager groupMngr = new NnUserSubscribeGroupManager();
            NnUserSubscribeGroup group = groupMngr.findByUserAndSeq(user, Short.parseShort(pos));
            if (group != null)
                groupMngr.delete(user, group);
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }        
    
    public Object subscribe(String userToken, String channelId, String gridId) {        
        //verify input
        @SuppressWarnings("rawtypes")
        HashMap map = this.checkUser(userToken, false);
        if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
            return this.assembleMsgs((Integer)map.get("s"), null);
        }
        if (channelId == null) 
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);        
        NnUser user = (NnUser) map.get("u");                
        NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
        //verify channel and grid
        if (channelId.contains("f-")) {
            String profileUrl = channelId.replaceFirst("f-", "");
            NnUser curator = NNF.getUserMngr().findByProfileUrl(profileUrl, mso.getId());
            if (curator == null)
                return this.assembleMsgs(NnStatusCode.CHANNEL_ERROR, null);
            NnChannel favoriteCh = NNF.getChannelMngr().createFavorite(curator);
            channelId = String.valueOf(favoriteCh.getId());
        }
        NnChannel channel = null;
        if (channelId.contains("yt")) {
            channel = new YtChannelManager().convert(channelId);            
        } else {
            long cId = Long.parseLong(channelId);            
            channel = NNF.getChannelMngr().findById(cId);
        }
        if (channel == null || channel.getStatus() == NnChannel.STATUS_ERROR)
            return this.assembleMsgs(NnStatusCode.CHANNEL_ERROR, null);
        
        short seq = 0;
        if (gridId == null) {
            seq = subMngr.findFirstAvailableSpot(user);
            if (seq == 0)
                return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        } else {
            seq = Short.parseShort(gridId);
        }
        if (seq > 72) {
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        }
        log.info("input token:" + userToken + ";user token:" + user.getToken() + 
                ";userId:" + user.getId() + ";user type:" + user.getType() + ";seq:" + seq);
        //if (user.getType() != NnUser.TYPE_YOUTUBE_CONNECT) {
        if (seq != 0) {
            NnUserSubscribe s = subMngr.findByUserAndSeq(user, seq);
            if (s != null)
                return this.assembleMsgs(NnStatusCode.SUBSCRIPTION_POS_OCCUPIED, null);
        }
        NnUserSubscribe s = subMngr.findByUserAndChannel(user, String.valueOf(channel.getId()));        
        if (s != null)
            return this.assembleMsgs(NnStatusCode.SUBSCRIPTION_DUPLICATE_CHANNEL, null);        
        s = subMngr.subscribeChannel(user, channel.getId(), seq, MsoIpg.TYPE_GENERAL);
        String result[] = {""};
        result[0] = s.getSeq() + "\t" + s.getChannelId();
        
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object categoryInfo(String id, String tagStr, String start, String count, String sort, boolean programInfo) {
        if (id == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        long cid = Long.parseLong(id);
        SysTagDisplay display = NNF.getDisplayMngr().findById(cid);
        if (display == null)
            return this.assembleMsgs(NnStatusCode.CATEGORY_INVALID, null);
        List<NnChannel> channels = new ArrayList<NnChannel>();
        if (start == null)
            start = "0";
        long limit = 0;
        if (count != null) {
            limit = Integer.valueOf(count);
        } else {
            limit = 200;
        }
        if (limit > 200)
            limit = 200;
        if (programInfo)
            limit = 20;        
        TagManager tagMngr = new TagManager();
        if (tagStr != null) {
            channels = tagMngr.findChannelsByTag(tagStr, true); //TODO removed            
        } else {
            channels = NNF.getSysTagMngr().findPlayerChannelsById(display.getSystagId(), display.getLang(), Integer.parseInt(start), 
                    Integer.parseInt(String.valueOf(limit)), SysTag.SORT_DATE, mso.getId());
        }
        long longTotal = NNF.getSysTagMngr().findPlayerChannelsCountById(display.getSystagId(), display.getLang(), mso.getId());
        Object result = NNF.getDisplayMngr().getPlayerCategoryInfo(display, programInfo, channels, Long.valueOf(start), limit, longTotal, version, this.format); 
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
     
    
    public Object channelStack(String stack, String lang, String userToken, String channel, boolean isReduced) {
        if (stack == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        lang = this.checkLang(lang);        
        List<String> result = new ArrayList<String>();
        String[] cond = stack.split(",");
        for (String s : cond) {
            /*
            String stackName = TagManager.assembleStackName(s, lang, mso.getName());
            log.info("stackName:" + stackName);
            */
            List<NnChannel> chs = new ArrayList<NnChannel>();
            if (s.equals(Tag.RECOMMEND)) {
                //chs = new RecommendService().findRecommend(userToken, mso.getId(), lang);
                Object obj = new YtChannelManager().findRecommend(userToken, lang, version, this.format);
                return this.assembleMsgs(NnStatusCode.SUCCESS, obj);
            } else if (s.equals("mayLike")) {            
                chs = new RecommendService().findMayLike(userToken, mso.getId(), channel, lang);                
            } else {                
                SysTagDisplay set = null;
                if (!Pattern.matches("^\\d*$", stack)) {
                    log.info("channelStack findbyname:" + stack);
                    set = NNF.getDisplayMngr().findByName(stack, mso.getId());                    
                } else {
                    log.info("channelStack findbyid:" + stack);
                    set = NNF.getDisplayMngr().findById(Long.parseLong(stack)); 
                }
                if (set != null)
                    chs = NNF.getSysTagMngr().findPlayerChannelsById(set.getSystagId(), set.getLang(), SysTag.SORT_DATE, 0);                
            }
            
            String output = "";
            if (isReduced) {
                output = (String) NNF.getChannelMngr().composeReducedChannelLineup(chs, PlayerApiService.FORMAT_PLAIN);
            } else {
                output = (String) NNF.getChannelMngr().composeChannelLineup(chs, version, PlayerApiService.FORMAT_PLAIN);
            }
            result.add(output);
        }
        String size[] = new String[result.size()];
        return this.assembleMsgs(NnStatusCode.SUCCESS, result.toArray(size));
    }

    public Object subscriberLineup(String userToken, String curatorIdStr) {
        NnUser curator = NNF.getUserMngr().findByProfileUrl(curatorIdStr, mso.getId());
        if (curator == null)
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
        List<NnChannel> channels = new ArrayList<NnChannel>();
        if (curator.getToken().equals(userToken)) {
            log.info("find channels curator himself created");
            channels = NNF.getChannelMngr().findByUserAndHisFavorite(curator, 0, true);
        } else {
            log.info("find curator channels");
            channels = NNF.getChannelMngr().findByUserAndHisFavorite(curator, 0, false);
        }
        NNF.getUserMngr().composeSubscriberInfoStr(channels);
        return "";
    }

    //TODO rewrite
    public Object channelLineup(String userToken,
                                String curatorIdStr,
                                String subscriptions,
                                boolean userInfo,
                                String channelIds, 
                                boolean setInfo, 
                                boolean isRequired,
                                boolean isReduced,
                                boolean programInfo,   
                                String sort,
                                HttpServletRequest req) {
        //verify input
        if (((userToken == null && userInfo == true) || 
            (userToken == null && channelIds == null) || 
            (userToken == null && setInfo == true))) {
            if (curatorIdStr == null && subscriptions == null)
                return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        List<String> result = new ArrayList<String>();
        NnUser user = null;
        NnGuest guest = null;
        if (userToken != null || subscriptions != null) {
            if (subscriptions != null) {
                user = NNF.getUserMngr().findByProfileUrl(subscriptions, mso.getId());
            } else {
                user = NNF.getUserMngr().findByToken(userToken, mso.getId());
            }
            if (user == null) {
                guest = new NnGuestManager().findByToken(userToken);
                if (guest == null)
                    return this.assembleMsgs(NnStatusCode.USER_INVALID, null);                
                /*else
                    return this.assembleMsgs(NnStatusCode.SUCCESS, null);*/                                        
            }
        }
        PlayerChannelLineup playerChannelLineup = new PlayerChannelLineup();
        //user info
        if (userInfo) {
            log.info("userInfo is added");
            if (this.format == PlayerApiService.FORMAT_PLAIN)
                result.add((String) this.prepareUserInfo(user, guest, req, true));
            else
                playerChannelLineup.setUserInfo(((UserInfo)this.prepareUserInfo(user, guest, req, true)));
        }
        NnUserSubscribeGroupManager groupMngr = new NnUserSubscribeGroupManager();
        //if (curatorIdStr == null && user != null ) {
        if (curatorIdStr == null) {
            //set info
            if (setInfo) {
                String setOutput = "";
                List<NnUserSubscribeGroup> groups = new ArrayList<NnUserSubscribeGroup>();
                String[] list = new String[9];
                if (user != null) {
                    groups.addAll(groupMngr.findByUser(user));
                    if (format == PlayerApiService.FORMAT_PLAIN) {
                        for (NnUserSubscribeGroup g : groups) {
                            String[] obj = {
                                    String.valueOf(g.getSeq()),
                                    String.valueOf(g.getId()),
                                    g.getName(),                        
                                    g.getImageUrl(),
                                    String.valueOf(g.getType()),
                            };
                            list[g.getSeq() - 1] = NnStringUtil.getDelimitedStr(obj);
                            //setOutput += NnStringUtil.getDelimitedStr(obj) + "\n";
                        }
                    } else {                        
                        List<SubscribeSetInfo> setInfoList = new ArrayList<SubscribeSetInfo>();
                        for (NnUserSubscribeGroup g : groups) {
                            SubscribeSetInfo s = new SubscribeSetInfo();
                            s.setSeq(g.getSeq());
                            s.setId(g.getId());
                            s.setName(g.getName());
                            s.setImageUrl(g.getImageUrl());
                            setInfoList.add(s);
                        }                        
                    }
                }
                //overwrite user's
                List<MsoIpg> ipgs = new MsoIpgManager().findSetsByMso(mso.getId());
                for (MsoIpg i : ipgs) {
                    String[] obj = {
                            String.valueOf(i.getSeq()),
                            String.valueOf(0),
                            i.getGroupName(),                        
                            "",
                            String.valueOf(i.getType()),
                    };
                    list[i.getSeq() - 1] = NnStringUtil.getDelimitedStr(obj);
                    //setOutput += NnStringUtil.getDelimitedStr(obj) + "\n";                        
                }
                for (int i=0; i < list.length; i++) {
                    if (list[i] != null)
                        setOutput += list[i] + "\n"; 
                }
                result.add(setOutput);
            }
        }
        //find channels
        List<NnChannel> channels = new ArrayList<NnChannel>();
        boolean channelPos = true;
        if (channelIds == null && curatorIdStr == null) {
            //find subscribed channels
            if (user != null) {
                NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
                channels = subMngr.findSubscribedChannels(user);
            }
            //default channels goes after channels, to make sure it overwrites original ones
            List<NnChannel> defaultChannels = NNF.getChannelMngr().findMsoDefaultChannels(mso.getId(), false);
            channels.addAll(defaultChannels);
            log.info("user: " + userToken + " find subscribed size:" + channels.size());
        } else if (curatorIdStr != null) {
            channelPos = false;
            NnUser curator = NNF.getUserMngr().findByProfileUrl(curatorIdStr, mso.getId());
            if (curator == null)
                return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
            List<NnChannel> curatorChannels = new ArrayList<NnChannel>();
            if (curator.getToken().equals(userToken)) {
                log.info("find channels curator himself created");
                curatorChannels = NNF.getChannelMngr().findByUserAndHisFavorite(curator, 0, true);
            } else {
                log.info("find channels curator created for public");
                curatorChannels = NNF.getChannelMngr().findByUserAndHisFavorite(curator, 0, false);
            }
            //List<NnChannel> curatorChannels = chMngr.findByUserAndHisFavorite(curator, 0, true);
            for (NnChannel c : curatorChannels) {
                if (c.isPublic() && c.getStatus() == NnChannel.STATUS_SUCCESS) {
                    channels.add(c);
                }
            }
        } else {
            //find specific channels
            channelPos = false;
            String[] chArr = channelIds.split(",");
            if (chArr.length > 1) {
                List<Long> list = new ArrayList<Long>();
                for (int i=0; i<chArr.length; i++) { list.add(Long.valueOf(chArr[i]));}
                channels = NNF.getChannelMngr().findByIds(list);
            } else {
                NnChannel channel = NNF.getChannelMngr().findById(Long.parseLong(channelIds));
                if (channel != null) channels.add(channel);                    
            }
        }
        if (isRequired && channels.size() == 0)
            return this.assembleMsgs(NnStatusCode.CHANNEL_INVALID, null);
        //sort by seq
        if (channelPos) {
            if (user == null || user.getType() != NnUser.TYPE_YOUTUBE_CONNECT) {
                if (sort != null && sort.equals(NnUserSubscribe.SORT_DATE)) {
                    log.info("sort by date");
                    Collections.sort(channels, NnChannelManager.getComparator("updateDate"));
                } else {
                    Collections.sort(channels, NnChannelManager.getComparator("seq"));
                }
            }
        }
        if (format == PlayerApiService.FORMAT_JSON) {
            @SuppressWarnings("unchecked")
            List<ChannelLineup> lineup = (List<ChannelLineup>) NNF.getChannelMngr().getPlayerChannelLineup(channels, channelPos, programInfo, isReduced, version, format, null);
            playerChannelLineup.setChannelLineup(lineup);
            return this.assembleMsgs(NnStatusCode.SUCCESS, playerChannelLineup);
        }
        Object channelLineup = NNF.getChannelMngr().getPlayerChannelLineup(channels, channelPos, programInfo, isReduced, version, format, result); 
        return this.assembleMsgs(NnStatusCode.SUCCESS, channelLineup);
    }

    public Object channelSubmit(String categoryIds, String userToken, 
                                String url, String grid,
                                String name, String image,
                                String tags, String lang, 
                                HttpServletRequest req) {
        //verify input
        if (url == null || url.length() == 0 ||  grid == null || grid.length() == 0 ||
             userToken== null || userToken.length() == 0) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        if (Integer.parseInt(grid) < 0 || Integer.parseInt(grid) > 81) {            
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        }
        url = url.trim();
        //verify user
        NnUser user = NNF.getUserMngr().findByToken(userToken, mso.getId());
        if (user == null) 
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);        
        if (user.getEmail().equals(NnUser.GUEST_EMAIL))
            return this.assembleMsgs(NnStatusCode.USER_PERMISSION_ERROR, null);
                
        //verify url, also converge youtube url
        url = NNF.getChannelMngr().verifyUrl(url);         
        if (url == null)
            return this.assembleMsgs(NnStatusCode.CHANNEL_URL_INVALID, null);            
        
        //verify channel status for existing channel
        NnChannel channel = NNF.getChannelMngr().findBySourceUrl(url);                                        
        if (channel != null && (channel.getStatus() == NnChannel.STATUS_ERROR)) {
            log.info("channel id and status :" + channel.getId()+ ";" + channel.getStatus());
            this.assembleMsgs(NnStatusCode.CHANNEL_STATUS_ERROR, null);
        }
        
        //create a new channel
        lang = this.checkLang(lang);
        if (channel == null) {
            if (name != null && image != null) {
                channel = NNF.getChannelMngr().createYouTubeWithMeta(url, name, null, lang, image, req);
            } else {
                channel = NNF.getChannelMngr().create(url, null, lang, req);
            }
            if (channel == null) {
                return this.assembleMsgs(NnStatusCode.CHANNEL_URL_INVALID, null);
            }            
            channel.setTag(tags);
            log.info("User throws a new url:" + url);
        }
        
        //subscribe
        NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
        short seq = Short.parseShort(grid);
        if (seq != 0) {
            NnUserSubscribe s = subMngr.findByUserAndSeq(user, seq);
            if (s != null)
                return this.assembleMsgs(NnStatusCode.SUBSCRIPTION_POS_OCCUPIED, null);
        }
        NnUserSubscribe s = subMngr.findByUserAndChannel(user, String.valueOf(channel.getId()));        
        if (s != null)
            return this.assembleMsgs(NnStatusCode.SUBSCRIPTION_DUPLICATE_CHANNEL, null);        
        s = subMngr.subscribeChannel(user, channel.getId(), seq, MsoIpg.TYPE_GENERAL);
        String result[] = {""};
        String channelName = "";
        if (channel.getSourceUrl() != null && channel.getSourceUrl().contains("http://www.youtube.com") && name == null)
            channelName = YouTubeLib.getYouTubeChannelName(channel.getSourceUrl());
        if (channel.getContentType() == NnChannel.CONTENTTYPE_FACEBOOK) 
            channelName = channel.getSourceUrl();            
        String output[]= {String.valueOf(channel.getId()),                             
                          channel.getName(),
                          channel.getImageUrl(),
                          String.valueOf(channel.getContentType()),
                          channelName};
        result[0] = NnStringUtil.getDelimitedStr(output);
        
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
    
    //TODO if use if fb user, redirect to facebook
    public Object login(String email, String password, HttpServletRequest req, HttpServletResponse resp) {
        log.info("login player api service:" + mso.getId());
        log.info("login: email=" + email + ";password=" + password);
        if (!BasicValidator.validateRequired(new String[] {email, password}))
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);;        
            
        NnUser user = NNF.getUserMngr().findAuthenticatedUser(email, password, mso.getId(), req);
        if (user != null) {
            Object result = this.prepareUserInfo(user, null, req, true);
            NNF.getUserMngr().save(user); //change last login time (ie updateTime)
            this.setUserCookie(resp, CookieHelper.USER, user.getToken());
            if (this.format == PlayerApiService.FORMAT_PLAIN) {
                String[] raw = {(String)result};
                return this.assembleMsgs(NnStatusCode.SUCCESS, raw);            
            }
            return this.assembleMsgs(NnStatusCode.SUCCESS, result);            
        } else {
            return this.assembleMsgs(NnStatusCode.USER_LOGIN_FAILED, null);
        }
    }
    
    public Object setProgramProperty(String program, String property, String value) {
        if (program == null || property == null || value == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        NnProgram p = NNF.getProgramMngr().findById(Long.parseLong(program));
        if (p == null)
            return this.assembleMsgs(NnStatusCode.PROGRAM_INVALID, null);
        if (property.equals("duration")) {
            p.setDuration(value);
            NNF.getProgramMngr().save(p);
        } else {
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, null); 
    }

    public Object setChannelProperty(String channel, String property, String value) {
        if (channel == null || property == null || value == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        NnChannel c = NNF.getChannelDao().findById(Long.parseLong(channel));
        if (c == null)
            return this.assembleMsgs(NnStatusCode.CHANNEL_INVALID, null);
        if (property.equals("count")) {
            c.setCntEpisode(Integer.valueOf(value));
        } else if (property.equals("updateDate")) {
            long epoch = Long.parseLong(value);
            Date date = new Date (epoch*1000);
            log.info("Date:" + date);
            c.setUpdateDate(date);
        } else {
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        }
        NNF.getChannelDao().save(c);
        return this.assembleMsgs(NnStatusCode.SUCCESS, null); 
    }
    
    //TODO real range search
    public Object tagInfo(String name, String start, String count) {
        if (name == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        String[] result = {"", ""};
        TagManager tagMngr = new TagManager();
        Tag tag = tagMngr.findByName(name);
        if (tag == null)
            return this.assembleMsgs(NnStatusCode.TAG_INVALID, null);
                        
        List<NnChannel> channels = new TagManager().findChannelsByTag(name, true);
        start = start == null ? "1" : start;
        count = count == null ? String.valueOf(channels.size()) : count;        
        int startIndex = Integer.parseInt(start) - 1;
        int endIndex = Integer.parseInt(count);
        startIndex = startIndex < 0 ? 0 : startIndex;
        endIndex = endIndex > channels.size() ? channels.size() : endIndex;
        int total = channels.size();
        channels = channels.subList(startIndex, startIndex + Integer.parseInt(count));
        log.info("startIndex:" + startIndex + ";endIndex:" + endIndex);
        result[1] += NNF.getChannelMngr().composeChannelLineup(channels, version, this.format);
        
        result[0] += assembleKeyValue("id", String.valueOf(tag.getId()));
        result[0] += assembleKeyValue("name", tag.getName());
        result[0] += assembleKeyValue("start", start);
        result[0] += assembleKeyValue("count", count);
        result[0] += assembleKeyValue("total", String.valueOf(total));
        
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
    
    @SuppressWarnings("unchecked")
    public Object programInfo(String channelIds, String episodeIdStr, 
                                  String userToken, String ipgId,
                                  boolean userInfo, String sidx, String limit,
                                  String start, String count,
                                  String time) {
        if (channelIds == null || (channelIds.equals("*") && userToken == null && ipgId == null)) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        if (start != null && Integer.valueOf(start) > 200) {
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        }
        PlayerProgramInfo playerProgramInfo = new PlayerProgramInfo();
        String[] chArr = channelIds.split(",");
        NnUser user = null;
        
        boolean pagination = false;
        NnEpisode orphanEpisode = null;
        if (start != null)
            pagination = true;
        if (sidx != null) {
            start = sidx;
            count = limit;
        }       
        int startI = 1;
        int countI = PAGING_ROWS;
        if (start != null) { startI = Integer.parseInt(start); }
        if (count != null) { countI = Integer.parseInt(count); }
        
        int end = PAGING_ROWS;
        countI = PAGING_ROWS; //overwrite the input value
        startI = startI - 1;
        startI = startI / PAGING_ROWS;
        startI = startI * PAGING_ROWS;
        end = startI + countI; 
        
        Short shortTime = 24;
        if (time != null)
            shortTime = Short.valueOf(time);
        
        String programInfoStr = "";
        String paginationStr = "";
        
        // to rewrite start/end, if episode was specified
        if (episodeIdStr != null && !episodeIdStr.isEmpty()) {
            
            NnEpisodeManager epMngr = NNF.getEpisodeMngr();
            NnEpisode episode = epMngr.findById(Long.valueOf(episodeIdStr));
            
            if (episode != null && episode.getSeq() > 0) {
                
                if (channelIds.equals(String.valueOf(episode.getChannelId()))) {
                    
                    pagination = true;
                    int unPublicCnt = epMngr.total("channelId == " + channelIds + " && seq < " + episode.getSeq() + " && isPublic == false");
                    startI = (episode.getSeq() <= PAGING_ROWS) ? 0 : (episode.getSeq() - 1) - unPublicCnt;
                    
                    NnChannel channel = NNF.getChannelMngr().findById(channelIds);
                    if (channel != null && channel.getSorting() == NnChannel.SORT_POSITION_REVERSE) {
                        
                        // reversed playlist
                        int totalCnt = epMngr.total("channelId == " + channelIds);
                        unPublicCnt = epMngr.total("channelId == " + channelIds + " && seq > " + episode.getSeq() + " && isPublic == false");
                        startI = totalCnt - episode.getSeq() - unPublicCnt;
                        if (startI < PAGING_ROWS) startI = 0;
                    }
                    
                    if (startI < 0) startI = 0;
                    end = startI + PAGING_ROWS;
                    
                } else if (episode.getChannelId() == 0) {
                    
                    // orphan episode
                    pagination = true;
                    orphanEpisode = episode;
                    log.info("orphan episode " + episode.getId());
                }
            }
        }
        
        log.info("sidx = " + startI + ";" + "end = " + end);
        
        List<ProgramInfo> programInfoJson = new ArrayList<ProgramInfo>();
        if (channelIds.equals("*")) {
            user = NNF.getUserMngr().findByToken(userToken, mso.getId());
            if (user == null) {
                NnGuest guest = new NnGuestManager().findByToken(userToken);
                if (guest == null)
                    return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
                else
                    return this.assembleMsgs(NnStatusCode.SUCCESS, null);
            }
        } else if (chArr.length > 1) {
            
            List<Long> list = new ArrayList<Long>();
            for (int i = 0; i < chArr.length; i++) { list.add(Long.valueOf(chArr[i])); }
            for (Long l : list) {
                if (version < 32) {
                    programInfoStr = new IosService().findPlayerProgramInfoByChannel(l, startI, end);
                } else {
                    if (format == PlayerApiService.FORMAT_PLAIN) {
                        programInfoStr += (String) NNF.getProgramMngr().findPlayerProgramInfoByChannel(l, startI, end, version, this.format, shortTime, mso);
                        if (pagination) {
                            NnChannel c = NNF.getChannelMngr().findById(l);
                            if (c != null)
                                paginationStr += assembleKeyValue(c.getIdStr(), String.valueOf(countI) + "\t" + String.valueOf(c.getCntEpisode()));
                        }
                    } else {
                        programInfoJson = (List<ProgramInfo>) NNF.getProgramMngr().findPlayerProgramInfoByChannel(l, startI, end, version, this.format, shortTime, mso);
                    }
                }
            }
        } else if (orphanEpisode != null) {
            
            long cId = Long.parseLong(channelIds);
            NnChannel channel = NNF.getChannelMngr().findById(cId);
            if (format == PlayerApiService.FORMAT_PLAIN) {
                
                programInfoStr = (String) NNF.getProgramMngr().findPlayerProgramInfoByEpisode(orphanEpisode, channel, format);
                if (pagination && channel != null) {
                    paginationStr += assembleKeyValue(channel.getIdStr(), "1\t1");
                }
            } else {
                
                programInfoJson = (List<ProgramInfo>) NNF.getProgramMngr().findPlayerProgramInfoByEpisode(orphanEpisode, channel, format);
                playerProgramInfo.setProgramInfo(programInfoJson);
            }
        } else {
            if (version < 32) {
                programInfoStr = new IosService().findPlayerProgramInfoByChannel(Long.parseLong(channelIds), startI, end);
                if (programInfoStr != null && context.isIos()) {
                    String[] lines = programInfoStr.split("\n");
                    String debugStr = "";
                    if (lines.length > 0) {
                        for (int i=0; i<lines.length; i++) {
                            String[] tabs = lines[i].split("\t");
                            if (tabs.length > 1)
                                debugStr += tabs[1] + "; ";
                        }
                    }
                    log.info("ios program info debug string:" + debugStr);
                }
            } else {
                if (format == PlayerApiService.FORMAT_PLAIN) {
                    long cId = Long.parseLong(channelIds);
                    programInfoStr = (String) NNF.getProgramMngr().findPlayerProgramInfoByChannel(cId, startI, end, version, this.format, shortTime, mso);
                    if (pagination) {
                        NnChannel c = NNF.getChannelMngr().findById(cId);
                        if (c != null)
                            paginationStr += assembleKeyValue(c.getIdStr(), String.valueOf(countI) + "\t" + String.valueOf(c.getCntEpisode()));
                    }
                } else {
                    programInfoJson = (List<ProgramInfo>) NNF.getProgramMngr().findPlayerProgramInfoByChannel(Long.parseLong(channelIds), startI, end, version, this.format, shortTime, mso);
                    playerProgramInfo.setProgramInfo(programInfoJson);
                }
            }
        }
        
        String userInfoStr = "";
        List<String> result = new ArrayList<String>();
        if (userInfo) {
            if (user == null && userToken != null)  {
                user = NNF.getUserMngr().findByToken(userToken, mso.getId());
                if (this.format == PlayerApiService.FORMAT_PLAIN) {
                    userInfoStr = (String) this.prepareUserInfo(user, null, context.getHttpRequest(), true);
                    result.add(userInfoStr);
                } else {
                    playerProgramInfo.setUserInfo((UserInfo) this.prepareUserInfo(user, null, context.getHttpRequest(), true));
                }
            }
        }
        if (pagination && this.format == PlayerApiService.FORMAT_PLAIN)
            result.add(paginationStr);
            
        if (this.format == PlayerApiService.FORMAT_JSON) {
            return this.assembleMsgs(NnStatusCode.SUCCESS, playerProgramInfo);
        } else {
            result.add(programInfoStr);
            String size[] = new String[result.size()];
            return this.assembleMsgs(NnStatusCode.SUCCESS, result.toArray(size));
        }
    }    
    
    public Object saveIpg(String userToken, String channelId, String programId) {
        //obsolete
        return this.assembleMsgs(NnStatusCode.ERROR, null);                 
    }    

    public Object loadIpg(long ipgId) {
        //obsolete
        return this.assembleMsgs(NnStatusCode.ERROR, null);                 
    }            

    public Object moveChannel(String userToken, String grid1, String grid2) {        
        //verify input
        if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") || grid1 == null || grid2 == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        if (!Pattern.matches("^\\d*$", grid1) || !Pattern.matches("^\\d*$", grid2) ||
            Integer.parseInt(grid1) < 0 || Integer.parseInt(grid1) > 81 ||
            Integer.parseInt(grid2) < 0 || Integer.parseInt(grid2) > 81) {
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        }        
        NnUser user = NNF.getUserMngr().findByToken(userToken, mso.getId());
        if (user == null) 
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
        
        NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
        boolean success = subMngr.moveSeq(user, Short.parseShort(grid1), Short.parseShort(grid2));
        
        if (!success) { return this.assembleMsgs(NnStatusCode.SUBSCRIPTION_ERROR, null); }
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }

    public Object setSetInfo(String userToken, String name, String pos) {
        //verify input
        if (name == null || pos == null)  {            
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        if (!Pattern.matches("^\\d*$", pos) || Integer.parseInt(pos) < 0 || Integer.parseInt(pos) > 9) {            
            return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);
        }
        
        NnUser user = NNF.getUserMngr().findByToken(userToken, mso.getId());
        if (user == null)
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);    
        
        NnUserSubscribeGroupManager subSetMngr = new NnUserSubscribeGroupManager();
        short position = Short.valueOf(pos);
        NnUserSubscribeGroup subSet = subSetMngr.findByUserAndSeq(user, Short.valueOf(position));
        if (subSet!= null) {
            subSet.setName(name);
            subSetMngr.save(user, subSet);
        } else {
            subSet = new NnUserSubscribeGroup();
            subSet.setUserId(user.getId());
            subSet.setName(name);                
            subSet.setSeq(position);
            subSetMngr.create(user, subSet);            
        }
        
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }
    
    public String checkLang(String lang) {
        if (lang == null || lang.length() == 0)
            return LangTable.LANG_EN;
        if (lang != null && !lang.equals(LangTable.LANG_EN) && !lang.equals(LangTable.LANG_ZH))
            return null;
        return lang;
    }
    
    public Object staticContent(String key, String lang) {
        NnContentManager contentMngr = new NnContentManager();
        NnContent content = contentMngr.findByItemAndLang(key, lang, mso.getId());        
        if (content == null)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        lang = this.checkLang(lang);
        if (lang == null)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);        
        String[] result = {content.getValue()};
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object deviceRegister(String userToken, String type, HttpServletRequest req, HttpServletResponse resp) {
        
        NnDevice device = null;
        NnUser user = null;
        if (userToken != null) {
            @SuppressWarnings({ "rawtypes"})
            HashMap map = this.checkUser(userToken, false);
            if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
                return this.assembleMsgs((Integer)map.get("s"), null);
            }
            user = (NnUser) map.get("u");
        }
        
        if (type == null) {
        } else if (type.equalsIgnoreCase(NnDevice.TYPE_APNS) || type.equalsIgnoreCase(NnDevice.TYPE_GCM)) {
            
            String token = req.getParameter("token");
            if (token == null) {
                return this.assembleMsgs(NnStatusCode.ERROR, "missing param token");
            }
            
            ApiContext context = new ApiContext(req);
            Mso mso = context.getMso();
            
            device = NNF.getDeviceMngr().findDuplicated(token, mso.getId(), type);
            if (device == null) {
                device = new NnDevice(token, mso.getId(), type);
            }
            device.setBadge(0);
        }
        NnDeviceManager deviceMngr = NNF.getDeviceMngr();
        deviceMngr.setReq(req); //!!!
        device = deviceMngr.create(device, user, type);
        
        if (type == null || type.equalsIgnoreCase(NnDevice.TYPE_FLIPR)) {
            setUserCookie(resp, CookieHelper.DEVICE, device.getToken());
        }
        Object result = deviceMngr.getPlayerDeviceInfo(device, this.format, null);
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object deviceTokenVerify(String token, HttpServletRequest req) {
        if (token == null)
            return this.assembleMsgs(NnStatusCode.SUCCESS, null);
        NnDeviceManager deviceMngr = NNF.getDeviceMngr();
        deviceMngr.setReq(req); //!!!
        List<NnDevice> devices = deviceMngr.findByToken(token);
        if (devices.size() == 0)
            return this.assembleMsgs(NnStatusCode.DEVICE_INVALID, null);
        List<NnUser> users = new ArrayList<NnUser>(); 
        log.info("device size" + devices.size());
        for (NnDevice d : devices) {
            if (d.getUserId() != 0) {
                NnUser user = NNF.getUserMngr().findById(d.getUserId(), (short)1);
                if (user != null && user.getMsoId() == mso.getId())
                    users.add(user);
                else
                    log.info("bad data in device:" + d.getToken() + ";userId:" + d.getUserId());
            }    
        }
        String[] result = {""};
        for (NnUser u : users) {
            result[0] += u.getToken() + "\t" + u.getProfile().getName() + "\t" + u.getUserEmail() + "\n";
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }

    public Object deviceAddUser(String deviceToken, String userToken, HttpServletRequest req) {
        NnUser user = null;
        if (userToken != null) {
            @SuppressWarnings("rawtypes")
            HashMap map = this.checkUser(userToken, false);
            if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
                return this.assembleMsgs((Integer)map.get("s"), null);
            }
            user = (NnUser) map.get("u");
        }
        if (deviceToken == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        NnDevice device = NNF.getDeviceMngr().addUser(deviceToken, user);
        if (device == null)
            return this.assembleMsgs(NnStatusCode.DEVICE_INVALID, null);
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }

    public Object deviceRemoveUser(String deviceToken, String userToken, HttpServletRequest req) {
        NnUser user = null;
        if (userToken != null) {
            @SuppressWarnings("rawtypes")
            HashMap map = this.checkUser(userToken, false);
            if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
                return this.assembleMsgs((Integer)map.get("s"), null);
            }
            user = (NnUser) map.get("u");
        }
        if (deviceToken == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        boolean success = NNF.getDeviceMngr().removeUser(deviceToken, user);
        if (!success) 
            return this.assembleMsgs(NnStatusCode.DEVICE_INVALID, null);
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private HashMap checkUser(String userToken, boolean guestOK) {
        HashMap map = new HashMap();        
        //verify input
        if (userToken == null || userToken.length() == 0 || userToken.equals("undefined"))
            map.put("s", NnStatusCode.INPUT_MISSING);
        if (guestOK) {
            map.put("s", NnStatusCode.SUCCESS);
            return map;
        }
        //verify user
        NnUser user = NNF.getUserMngr().findByToken(userToken, mso.getId());
        if (user == null) {
            map.put("s", NnStatusCode.USER_INVALID);
            return map;
        }
        if (!guestOK && user.getUserEmail().equals(NnUser.GUEST_EMAIL) ) {
            map.put("s", NnStatusCode.USER_PERMISSION_ERROR);
            return map;
        }
        map.put("s", NnStatusCode.SUCCESS);
        map.put("u", user);
        return map;
    }

    private int checkCaptcha(NnGuest guest, String fileName, String name) {
        NnGuestManager guestMngr = new NnGuestManager();
        if (guest == null)
            return NnStatusCode.CAPTCHA_INVALID;
        if (guest.getCaptchaId() == 0)
            return NnStatusCode.CAPTCHA_INVALID;
        Captcha c = new CaptchaManager().findById(guest.getCaptchaId());
        log.info(guest.getGuessTimes() + ";" + NnGuest.GUESS_MAXTIMES);
        if (guest.getGuessTimes() >= NnGuest.GUESS_MAXTIMES)
            return NnStatusCode.CAPTCHA_TOOMANY_TRIES;
        if (!c.getFileName().equals(fileName) || 
            !c.getName().equals(name)) {
            guest.setGuessTimes(guest.getGuessTimes()+1);
            guestMngr.save(guest, null);
            return NnStatusCode.CAPTCHA_FAILED;
        }
        Date now = new Date();
        if (now.after(guest.getExpiredAt()))
            return NnStatusCode.CAPTCHA_FAILED;
        return NnStatusCode.SUCCESS;
    }
     
    public Object recentlyWatched(String userToken, String count, boolean channelInfo, boolean episodeIndex) {
        @SuppressWarnings("rawtypes")
        HashMap map = this.checkUser(userToken, false);
        if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
            return this.assembleMsgs((Integer)map.get("s"), null);
        }
        if (count == null) 
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);        
        int cnt = Integer.parseInt(count);
        if (episodeIndex) {
            if (cnt > 5) {
                return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
            }
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }
    
    public Object userReport(String userToken, String deviceToken, 
            String session, String type, String item, String comment,
            HttpServletRequest req) {
        if (comment == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        /*
        if (userToken == null && deviceToken == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
            */
        if (comment.length() > 500)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        
        NnUser user = null;
        if (userToken != null) { 
            //verify input
            @SuppressWarnings("rawtypes")
            HashMap map = this.checkUser(userToken, false);
            user = (NnUser) map.get("u");
        }
        List<NnDevice> devices = new ArrayList<NnDevice>();
        NnDevice device = null;
        if (deviceToken != null) {
            devices = NNF.getDeviceMngr().findByToken(deviceToken);
            if (devices.size() > 0)
                device = devices.get(0);
        }
        if (device == null && user == null) {
            user = NNF.getUserMngr().findByEmail(NnUser.ANONYMOUS_EMAIL, 1, req);
            //return this.assembleMsgs(NnStatusCode.ACCOUNT_INVALID, null);
        }
        String content = "";
        if (item != null) {
            String[] key = item.split(",");
            String[] value = comment.split(",");
            if (key.length != value.length)
                return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);
            for (int i=0; i<key.length; i++) {
                content += key[i] + ":" + value[i] + "\n";
            }
        } else {
            content = comment; //backward compatibility
        }
        
        NnUserReportManager reportMngr = new NnUserReportManager();
        String[] result = {""};        
        NnUserReport report = reportMngr.save(user, device, session, type, item, content);
        if (report != null && user != null) {
            result[0] = PlayerApiService.assembleKeyValue("id", String.valueOf(report.getId()));
            EmailService service = new EmailService();
            String toEmail = "feedback@9x9.tv";
            String toName = "feedback";
            String subject = "User send a report";
            NnUserProfile profile = user.getProfile();
            String body = "user ui-lang:" + profile.getLang() + "\n";                
            body += "user region:" + profile.getSphere() + "\n\n";
            body += content;
            try {
                body = URLDecoder.decode(body, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }                
            
            subject += (type != null) ? (" (" + type + ")") : "" ;

            log.info("subject:" + subject);
            log.info("content:" + body);
            NnEmail mail = new NnEmail(toEmail, toName, 
                                       user.getUserEmail(), profile.getName(), 
                                       user.getUserEmail(), subject, body);
            service.sendEmail(mail, "userfeedback@9x9.tv", "userfeedback");
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }

    public Object setUserProfile(String userToken, String items, String values, HttpServletRequest req) {
        
        NnUserProfileManager profileMngr = NNF.getProfileMngr();
        
        //verify input
        if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") ||
            items == null || values == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        //verify user
        NnUser user = NNF.getUserMngr().findByToken(userToken, mso.getId());
        if (user == null) 
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);        
        if (user.getUserEmail().equals(NnUser.GUEST_EMAIL))
            return this.assembleMsgs(NnStatusCode.USER_PERMISSION_ERROR, null);
        NnUserProfile profile = profileMngr.findByUser(user);
        if (profile == null) profile = new NnUserProfile(user.getId(), mso.getId());
        String[] key = items.split(",");
        String[] value = values.split(",");
        String password = "";
        String oldPassword = "";
        if (key.length != value.length) {
            log.info("key and value length mismatches!");
            return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);
        }
        String[] valid = {"name", "year", "password", 
                "oldPassword", "sphere", "ui-lang", "gender", "description", "image", "phone"};
        //description,lang,name
        HashSet<String> dic = new HashSet<String>();
        for (int i=0; i<valid.length; i++) {
            dic.add(valid[i]);
        }
        for (int i=0; i<key.length; i++) {
           if (!dic.contains(key[i])) {
                log.info("contains not valid key value!");
                return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);
            }
           if (user.isFbUser()) {
               if (!key[i].equals("sphere") && !key[i].equals("ui-lang")) {
                   log.info("fbuser: not permitted key:" + key);
                   return this.assembleMsgs(NnStatusCode.USER_PERMISSION_ERROR, null);
               }
               log.info("fbuser: otherwise key:" + key);
           }
            String theValue = value[i];
            try {
                theValue = URLDecoder.decode(theValue, "utf-8");
                theValue = NnStringUtil.htmlSafeAndTruncated(theValue);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }                
            
            if (key[i].equals("name")) {
                if (theValue.equals(NnUser.GUEST_NAME))
                    return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);
                profile.setName(theValue);
            }
            if (key[i].equals("phone")) {
                if (!Pattern.matches("^\\d*$", theValue)) 
                    return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
                profile.setPhoneNumber(theValue);
            }
            if (key[i].equals("image"))
                profile.setImageUrl(theValue);
            if (key[i].equals("year"))
                profile.setDob(theValue);
            if (key[i].equals("description"))
                profile.setIntro(theValue);
            if (key[i].equals("password"))
                password = theValue;                
            if (key[i].equals("oldPassword"))
                oldPassword = theValue;                
            if (key[i].equals("sphere")) {
                if ((theValue == null) || (this.checkLang(theValue) == null))
                    return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
                profile.setSphere(theValue);
            }
            if (key[i].equals("gender"))
                profile.setGender(Short.parseShort(theValue));                        
            if (key[i].equals("ui-lang")) {
                if ((theValue == null) || (this.checkLang(theValue) == null))
                    return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
                profile.setLang(theValue);
            }
        }        
        int status = NnUserValidator.validateProfile(user);
        if (status != NnStatusCode.SUCCESS) {
            log.info("profile fail");
            return this.assembleMsgs(status, null);
        }
        if (password.length() > 0 && oldPassword.length() > 0) {
            log.info("password:" + password + ";oldPassword:" + oldPassword);
            NnUser authenticated = NNF.getUserMngr().findAuthenticatedUser(user.getEmail(), oldPassword, mso.getId(), req);
            if (authenticated == null)
                return this.assembleMsgs(NnStatusCode.USER_LOGIN_FAILED, null);
            status = NnUserValidator.validatePassword(password);
            if (status != NnStatusCode.SUCCESS)
                return this.assembleMsgs(status, null);
            user.setPassword(password);
            user.setSalt(AuthLib.generateSalt());
            user.setCryptedPassword(AuthLib.encryptPassword(user.getPassword(), user.getSalt()));            
            NNF.getUserMngr().save(user);
        }
        profileMngr.save(user, profile);
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }
    
    public Object getUserProfile(String userToken) {
        //verify input
        if (userToken == null || userToken.length() == 0 || userToken.equals("undefined"))
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        //verify user
        NnUser user = NNF.getUserMngr().findByToken(userToken, mso.getId());
        if (user == null) 
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
        Object result = NNF.getProfileMngr().getPlayerProfile(user, this.format);
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object shareByEmail(String userToken, String toEmail, String toName, 
            String subject, String content, 
            String captcha, String text) {        
        @SuppressWarnings("rawtypes")
        HashMap map = this.checkUser(userToken, false);
        if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
            return this.assembleMsgs((Integer)map.get("s"), null);
        }
        boolean isIos = context.isIos();
        if (!isIos) {
            if (captcha == null || text == null)
                return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        if (toEmail == null || content == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        NnUser user = (NnUser) map.get("u");
        if (captcha != null) {
            NnGuestManager guestMngr = new NnGuestManager();
            NnGuest guest = guestMngr.findByToken(userToken);
            int status = this.checkCaptcha(guest, captcha, text);
            if (status != NnStatusCode.SUCCESS)
                return this.assembleMsgs(status, null);
            guestMngr.delete(guest);
        }
        NnEmail mail = new NnEmail(toEmail, toName, NnEmail.SEND_EMAIL_SHARE, user.getProfile().getName(), user.getUserEmail(), subject, content);        
        NNF.getEmailService().sendEmail(mail, null, null);
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }
    
    public Object setUserPref(String token, String item, String value) {
        //verify input
        if (token == null || token.length() == 0 || token.equals("undefined") ||
            item == null || value == null || item.length() == 0 || value.length() == 0) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }        
        //verify user
        NnUser user = NNF.getUserMngr().findByToken(token, mso.getId());
        if (user == null) 
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);        
        //get preference
        NnUserPref pref = NNF.getPrefMngr().findByUserAndItem(user, item);
        if (pref != null) {
            pref.setValue(value);            
            NNF.getPrefMngr().save(user, pref);
        } else {
            pref = new NnUserPref(user, item, value);
            NNF.getPrefMngr().save(user, pref);
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }
    
    public Object requestCaptcha(String token, String action, HttpServletRequest req) {
        if (token == null || action == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        CaptchaManager mngr = new CaptchaManager();
        Captcha c = mngr.getRandom();
        if (c == null)
            return this.assembleMsgs(NnStatusCode.CAPTCHA_ERROR, null);
        short a = Short.valueOf(action);   
        Calendar cal = Calendar.getInstance();        
        cal.add(Calendar.MINUTE, 5);        
        NnGuestManager guestMngr = new NnGuestManager();
        NnGuest guest = guestMngr.findByToken(token);                
        if (a == Captcha.ACTION_SIGNUP) {
            if (guest == null)
                return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
        } else if (a == Captcha.ACTION_EMAIL) {
            if (guest == null) {
                guest = new NnGuest(NnGuest.TYPE_USER);
                guest.setToken(token);
            }
        }
        guest.setCaptchaId(c.getId());
        guest.setExpiredAt(cal.getTime());
        guest.setGuessTimes(0);
        guestMngr.save(guest, req);        
        return this.assembleMsgs(NnStatusCode.SUCCESS, new String[] {c.getFileName()});
    }    

    public Object saveSorting(String token, String channelId, String sort) {
        @SuppressWarnings("rawtypes")
        HashMap map = this.checkUser(token, false);
        if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
            return this.assembleMsgs((Integer)map.get("s"), null);
        }
        NnUser user = (NnUser) map.get("u");
        NnUserChannelSorting sorting = new NnUserChannelSorting(user.getId(), 
                                           Long.parseLong(channelId), Short.parseShort(sort));
        NnUserChannelSortingManager sortingMngr = new NnUserChannelSortingManager();
        sortingMngr.save(user, sorting);        
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }

    public Object saveShare(String userToken, String channelId, String programId, String setId) {
        if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") ||
            channelId == null || programId == null || channelId.length() == 0 || programId.length() == 0) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        if (!Pattern.matches("^\\d*$", channelId))
            return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);
        
        NnUser user = NNF.getUserMngr().findByToken(userToken, mso.getId());
        if (user == null) 
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
        
        NnUserShare share = new NnUserShare();
        share.setChannelId(Long.parseLong(channelId));
        if (Pattern.matches("^\\d*$", programId)) {
            share.setProgramId(Long.parseLong(programId));
        } else {
            share.setProgramIdStr(programId);
        }
        share.setUserId(user.getId());
        NnUserShareManager shareMngr = new NnUserShareManager();
        shareMngr.create(share);
        String result[] = {String.valueOf(share.getId())};
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);                
    }

    //deprecated
    public Object loadShare(long id) {
        NnUserShareManager shareMngr = new NnUserShareManager();
        NnUserShare share= shareMngr.findById(id);
        if (share== null)
            return this.assembleMsgs(NnStatusCode.IPG_INVALID, null);
        
        String[] result = {"", ""};
        NnProgram program = NNF.getProgramMngr().findById(share.getProgramId());
        if (program != null) {
            List<NnProgram> programs = new ArrayList<NnProgram>();
            programs.add(program);
            result[0] = ""; //need to be something if not deprecated
        } else {            
            result[0] = share.getChannelId() + "\t" + share.getProgramIdStr() + "\n";            
        }
        NnChannel channel = NNF.getChannelMngr().findById(share.getChannelId());        
        if (channel != null) {
            result[1] = NNF.getChannelMngr().composeEachChannelLineup(channel, version, PlayerApiService.FORMAT_PLAIN) + "\n";
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }

    public Object personalHistory(String userToken) {
        @SuppressWarnings("rawtypes")
        HashMap map = this.checkUser(userToken, false);
        if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
            return this.assembleMsgs((Integer)map.get("s"), null);
        }
        NnUser user = (NnUser) map.get("u");
        List<NnChannel> channels = NNF.getChannelMngr().findPersonalHistory(user.getId(), user.getMsoId());
        String result[] = {(String) NNF.getChannelMngr().composeChannelLineup(channels, version, this.format)};
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object userWatched(String userToken, String count, boolean channelInfo, boolean episodeIndex, String channel) {
        /*
        @SuppressWarnings("rawtypes")
        HashMap map = this.checkUser(userToken, false);
        if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
            return this.assembleMsgs((Integer)map.get("s"), null);
        }
        if (count == null) 
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);        
        int cnt = Integer.parseInt(count);
        if (episodeIndex) {
            if (cnt > 5) {
                return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
            }
        }
        String[] result = {"", ""};
        NnUserWatchedManager watchedMngr = new NnUserWatchedManager();
        NnProgramManager programMngr = NNF.getProgramMngr();
        List<NnUserWatched> watched = new ArrayList<NnUserWatched>();
        if (channel == null) {
            watched = watchedMngr.findByUserToken(userToken);
        } else {
            NnUserWatched w = watchedMngr.findByUserTokenAndChannel(userToken, Long.parseLong(channel));
            if (w != null) { watched.add(w); }                
        }
        List<NnChannel> channels = new ArrayList<NnChannel>();
        int i = 1;
        for (NnUserWatched w : watched) {
            if (i > cnt)
                break;
            int index = 0;            
            if (episodeIndex && Pattern.matches("^\\d*$", w.getProgram())) {
                String programInfo = programMngr.findPlayerProgramInfoByChannel(w.getChannelId());
                if (programInfo != null && programInfo.length() > 0) {
                    index = programMngr.getEpisodeIndex(programInfo, w.getProgram());
                }
            }
            
            result[0] += w.getChannelId() + "\t" + w.getProgram() + "\t" + index + "\n";
            NnChannel c = chMngr.findById(w.getChannelId());
            if (c != null) { 
                channels.add(c);
            }
            i++;
        }
        if (channelInfo) {
            chMngr.composeChannelLineup(channels, version);
        }
        */
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }

    public Object copyChannel(String userToken, String channelId, String grid) {        
        //verify input
        if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") || grid == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        if (!Pattern.matches("^\\d*$", grid) ||
            Integer.parseInt(grid) < 0 || Integer.parseInt(grid) > 81)
            return this.assembleMsgs(NnStatusCode.INPUT_ERROR, null);
        NnUser user = NNF.getUserMngr().findByToken(userToken, mso.getId());
        if (user == null)
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null); 
        
        NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
        boolean success = false;
        success = subMngr.copyChannel(user, Long.parseLong(channelId), Short.parseShort(grid));
        if (!success) 
            return this.assembleMsgs(NnStatusCode.SUBSCRIPTION_ERROR, null);
        else
            return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }

    public Object forgotpwd(String email, HttpServletRequest req) {
        NnUser user = NNF.getUserMngr().findByEmail(email, mso.getId(), req);
        if (user == null) {
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
        }
        if (user.isFbUser())
            return this.assembleMsgs(NnStatusCode.USER_PERMISSION_ERROR, null);        
        
        //String link = NnNetUtil.getUrlRoot(req) + "/#!resetpwd!e=" + email + "!pass=" + userMngr.forgotPwdToken(user);
        //signin.html?ac=resetpwd&e=marshsu.9x9@gmail.com&pass=b38ea3c1e56135827a6e4343d6ac4ea3
        String link = NnNetUtil.getUrlRoot(req).replaceAll("^http:\\/\\/", "https://");
        link += "/cms/signin.html?ac=resetpwd&e=" + email + "&pass=" + NNF.getUserMngr().forgotPwdToken(user);
        log.info("link:" + link);
        
        NnContentManager contentMngr = new NnContentManager();
        NnUserProfile profile = user.getProfile();
        String lang = profile.getLang();
        lang = this.checkLang(lang);
        log.info("user language:" + lang);
        NnContent content = contentMngr.findByItemAndLang("resetpwd", lang, mso.getId());        
        String subject = "Forgotten Password";
        if (lang.equals(LangTable.LANG_ZH))
            subject = "忘記密碼";
        String sentense = "<p>To reset the password, click on the link or copy and paste the following link into the address bar of your browser</p>";
        String body = sentense + "<p><a href = '" + link  + "'>" + link +  "</a></p>";
        if (content != null) {
            log.info("get email template from admin portal");
            body = content.getValue(); 
            body = body.replace("(9x9name)", profile.getName());
            body = body.replaceAll("\\(9x9link\\)", link);
        }
        
        EmailService service = new EmailService();
        NnEmail mail = new NnEmail(
                email, profile.getName(), 
                NnEmail.SEND_EMAIL_NOREPLY, "noreply", NnEmail.SEND_EMAIL_NOREPLY,                                     
                subject, body);
        
        mail.setHtml(true);
        service.sendEmail(mail, null, null);
        
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }

    public Object resetpwd(String email, String token, String password, HttpServletRequest req) {
        if (email == null || token == null || password == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        NnUserManager userMngr = NNF.getUserMngr();
        NnUser user = userMngr.findByEmail(email, mso.getId(), req);
        if (user == null) {
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
        }
        String forgotPwdToken = userMngr.forgotPwdToken(user);
        log.info("forgotPwdToken:" + forgotPwdToken + ";token user passes:" + token);        
        if (forgotPwdToken.equals(token)) {
            int status = NnUserValidator.validatePassword(password);
            if (status != NnStatusCode.SUCCESS)
                return this.assembleMsgs(status, null);
            user.setPassword(password);
            userMngr.resetPassword(user);
            userMngr.save(user);
            log.info("reset password success:" + user.getEmail());
        } else {
            log.info("reset password token mismatch");
            return this.assembleMsgs(NnStatusCode.USER_PERMISSION_ERROR, null);
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }

    @SuppressWarnings("unchecked")
    public Object search(String text, String stack, String type, String start, String count, HttpServletRequest req) {                       
        if (text == null || text.length() == 0)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        if (version < 32) {
            return new IosService().search(text);
        }
        
        //matched channels
        //List<NnChannel> channels = NnChannelManager.search(text, false);
        /*
        String userToken = CookieHelper.getCookie(req, CookieHelper.USER);
        boolean guest = NnUserManager.isGuestByToken(userToken);
        short userShard = 1;
        long userId = 1;
        String sphere = LangTable.LANG_EN;
        if (!guest) {
            NnUser u = userMngr.findByToken(userToken);
            if (u != null) {
                userShard = u.getShard();
                userId = u.getId();
                sphere = u.getSphere();
            }
        }
        */
        //List<NnChannel> channels = chMngr.searchBySvi(text, userShard, userId, sphere);
        if (start == null)
            start = "1";
        if (count == null)
            count = "9";
        int startIndex = Integer.parseInt(start);
        int limit = Integer.parseInt(count);
        if (startIndex < 1)
            startIndex = 1;
        if (limit < 0 || limit > 75)
            limit = 75;
        startIndex = startIndex - 1;
        
        String searchContent = "store_only";
        if (type != null && type.equals("9x9")) {
            searchContent = searchContent + ",9x9";
        }
        if (type != null && type.equals("youtube")) {
            searchContent = searchContent + ",youtube";
        }
        
        @SuppressWarnings("rawtypes")
        Stack st = NnChannelManager.searchSolr(SearchLib.CORE_NNCLOUDTV, text, searchContent, null, false, startIndex, limit);        
        List<NnChannel> channels = (List<NnChannel>) st.pop();
        System.out.println("solr search channel size:" + channels.size());
        long numOfChannelTotal = (Long) st.pop();        
        
        List<NnUser> users = NNF.getUserMngr().search(null, null, text, mso.getId());
        int endIndex = (users.size() > 9) ? 9: users.size();
        users = users.subList(0, endIndex);
        //if none matched, return suggestion channels
        List<NnChannel> suggestion = new ArrayList<NnChannel>();
        if (channels.size() == 0 && users.size() == 0) {
            suggestion = NNF.getChannelMngr().findBillboard(Tag.TRENDING, LangTable.LANG_EN);
        }
        int numOfChannelReturned = channels.size(); 
        int numOfCuratorReturned = users.size();
        int numOfSuggestReturned = suggestion.size();
        //int numOfChannelTotal = (int) NnChannelManager.searchSize(text, false);
        int numOfCuratorTotal = users.size();
        int numOfSuggestTotal = suggestion.size();
        
        if (format == PlayerApiService.FORMAT_PLAIN) {
            String[] result = {"", "", "", ""}; //count, curator, curator's channels, channels, suggestion channels
            //matched curators && their channels [important, two sections]
            result[1] = (String) NNF.getUserMngr().composeCuratorInfo(users, true, false, req, version);
            //matched channels
            result[2] = (String) NNF.getChannelMngr().composeChannelLineup(channels, version, this.format);
            System.out.println("result 3:" + result[3]);
            //suggested channels
            if (channels.size() == 0 && users.size() == 0) {
                result[3] = (String) NNF.getChannelMngr().composeChannelLineup(suggestion, version, this.format);
            }
            //statistics           
            result[0] = assembleKeyValue("curator", String.valueOf(numOfCuratorReturned) + "\t" + String.valueOf(numOfCuratorTotal));
            result[0] += assembleKeyValue("channel", String.valueOf(numOfChannelReturned) + "\t" + String.valueOf(numOfChannelTotal));
            result[0] += assembleKeyValue("suggestion", String.valueOf(numOfSuggestReturned) + "\t" + String.valueOf(numOfSuggestTotal));
            
            //statistics           
            result[0] = assembleKeyValue("curator", String.valueOf(numOfCuratorReturned) + "\t" + String.valueOf(numOfCuratorTotal));
            result[0] += assembleKeyValue("channel", String.valueOf(numOfChannelReturned) + "\t" + String.valueOf(numOfChannelTotal));
            result[0] += assembleKeyValue("suggestion", String.valueOf(numOfSuggestReturned) + "\t" + String.valueOf(numOfSuggestTotal));
            System.out.println("result 0:" + result[0]);
            
            return this.assembleMsgs(NnStatusCode.SUCCESS, result);
        } else {
            Search search = new Search();
            search.setChannelLineups((List<ChannelLineup>) NNF.getChannelMngr().composeChannelLineup(channels,  version,  this.format));
            search.setNumOfChannelReturned(numOfChannelReturned);
            search.setNumOfCuratorReturned(numOfCuratorReturned);
            search.setNumOfSuggestReturned(numOfSuggestReturned);
            search.setNumOfChannelTotal((int)numOfChannelTotal);
            search.setNumOfCuratorTotal(numOfCuratorTotal);
            search.setNumOfSuggestTotal(numOfSuggestTotal);
            return this.assembleMsgs(NnStatusCode.SUCCESS, search);
        }
        
    }
    
    public Object programRemove(String programId, String ytVideoId, String userToken, String secret, String status) {
        if (userToken == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        if (programId == null && ytVideoId == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        if (ytVideoId != null && ytVideoId.length() < 11) {
            log.info("invalid youtube id:" + ytVideoId);
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        }
        
        try {
            NnProgramManager programMngr = NNF.getProgramMngr();
            if (programId != null) {
                NnProgram program = programMngr.findById(Long.parseLong(programId));
                if (program != null) {
                    if (secret != null && secret.equals("chicken")) {
                        program.setStatus(NnProgram.STATUS_ERROR);
                    } else {
                        program.setStatus(NnProgram.STATUS_NEEDS_REVIEWED);
                    }            
                    programMngr.save(program);
                    programMngr.resetCache(program.getChannelId());
                }
            }
            if (ytVideoId != null) {
                List<NnProgram> programs = programMngr.findByYtVideoId(ytVideoId);
                if (programs.size() == 0) {
                    return this.assembleMsgs(NnStatusCode.PROGRAM_INVALID, null);
                }
                for (NnProgram p : programs) {
                    log.info("mark program:" + p.getId() + "(" + ytVideoId + ") status:" + status + " by " + userToken);
                    p.setStatus(Short.parseShort(status));
                    programMngr.save(p);
                }
            }
        } catch (NumberFormatException e) {
            log.info("pass invalid program id:" + programId);
        } catch (NullPointerException e) {
            log.info("program does not exist: " + programId);
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);        
    }
    
    public Object channelCreate(String token, String name, String intro, String imageUrl, boolean isTemp) {                                 
        //verify input
        if (token == null || token.length() == 0 || name == null || name.length() == 0 ||  imageUrl == null || imageUrl.length() == 0) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        if (this.format == PlayerApiService.FORMAT_JSON) {
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        }
        NnUser user = NNF.getUserMngr().findByToken(token, mso.getId());
        if (user == null)
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);        
        NnChannel channel = new NnChannel(name, intro, imageUrl);
        channel.setPublic(false);
        channel.setStatus(NnChannel.STATUS_WAIT_FOR_APPROVAL);
        channel.setContentType(NnChannel.CONTENTTYPE_MIXED); // a channel type in podcast does not allow user to add program in it, so change to mixed type
        channel.setTemp(isTemp);
        NNF.getChannelMngr().save(channel);
        String[] result = {(String) NNF.getChannelMngr().composeEachChannelLineup(channel, version, PlayerApiService.FORMAT_PLAIN)};        
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);        
    }
    
    public Object programCreate(String channel, String name, String description, String image, String audio, String video, boolean temp) {
        if (channel == null || channel.length() == 0 || name == null || name.length() == 0 || 
            (audio == null && video == null)) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        long cid = Long.parseLong(channel);        
        NnChannel ch = NNF.getChannelMngr().findById(cid);
        if (ch == null)
            return this.assembleMsgs(NnStatusCode.CHANNEL_INVALID, null);
        NnProgram program = new NnProgram(ch.getId(), name, description, image);
        program.setChannelId(cid);
        program.setAudioFileUrl(audio);
        program.setFileUrl(video);
        program.setContentType(NNF.getProgramMngr().getContentType(program));
        program.setStatus(NnProgram.STATUS_OK);
        program.setPublic(true);
        NNF.getProgramMngr().save(program);
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }

    private short getStatus(String msg) {
        String[] status = msg.split("\t");
        if (status.length > 0)
            return Short.valueOf(status[0]);
        return NnStatusCode.ERROR;
    }

    public Object sphereData(String token, String email, String password, HttpServletRequest req, HttpServletResponse resp) {
        List<String> data = new ArrayList<String>();
        return this.assembleSections(data);
    }
    
    public Object auxLogin(String token, String email, String password, HttpServletRequest req, HttpServletResponse resp) {
        //1. user info
        List<String> data = new ArrayList<String>();
        log.info ("[quickLogin] verify user: " + token);
        String userInfo = "";
        if (token != null) {
            userInfo = (String) this.userTokenVerify(token, req, resp);
        } else if (email != null || password != null) {        
            userInfo = (String) this.login(email, password, req, resp);            
        } else {
            userInfo = (String) this.guestRegister(req, resp);
        }
        if (this.getStatus(userInfo) != NnStatusCode.SUCCESS) {
            return userInfo;
        }        
        String sphere = "en";
        Pattern pattern = Pattern.compile(".*sphere\t((en|zh)).*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(userInfo);
        if (matcher.matches()) {
            sphere = matcher.group(1);
        }        
        data.add(userInfo);
        //2. trending
        log.info ("[quickLogin] trending channels");
        String trending = (String) this.channelStack(Tag.TRENDING, sphere, token, null, false);
        data.add(trending);
        if (this.getStatus(trending) != NnStatusCode.SUCCESS) {
            return this.assembleSections(data);
        }
        //3. hottest
        log.info ("[quickLogin] hot channels");
        String hot = (String) this.channelStack(Tag.HOT, sphere, token, null, false);
        data.add(hot);
        if (this.getStatus(hot) != NnStatusCode.SUCCESS) {
            return this.assembleSections(data);
        }
        return this.assembleSections(data);
    }
    
    /**
     * 1. user info
     * 2. channel lineup (grid)
     * 3. featured curators
     * 4. trending channels
     */

    public Object quickLogin(String token, String email, String password, HttpServletRequest req, HttpServletResponse resp) {
        //1. user info
        List<String> data = new ArrayList<String>();
        log.info ("[quickLogin] verify user: " + token);
        String userInfo = "";
        if (token != null) {
            userInfo = (String) this.userTokenVerify(token, req, resp);
        } else if (email != null || password != null) {        
            userInfo = (String) this.login(email, password, req, resp);            
        } else {
            userInfo = (String) this.guestRegister(req, resp);
        }
        if (this.getStatus(userInfo) != NnStatusCode.SUCCESS) {
            return userInfo;
        }        
        String sphere = "en";
        Pattern pattern = Pattern.compile(".*sphere\t((en|zh)).*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(userInfo);
        if (matcher.matches()) {
            sphere = matcher.group(1);
        }        
        data.add(userInfo);
        //2. channel lineup
        log.info ("[quickLogin] channel lineup: " + token);
        String lineup = (String) this.channelLineup(token, null, null, false, null, true, false, false, false, null, req);
        data.add(lineup);
        if (this.getStatus(lineup) != NnStatusCode.SUCCESS) {
            return this.assembleSections(data);
        }
        //3. featured curators
        log.info ("[quickLogin] featured curators");
        String curatorInfo = (String) this.curator(null, null, "featured", req);
        data.add(curatorInfo);
        //4. trending
        log.info ("[quickLogin] trending channels");
        String trending = (String) this.channelStack(Tag.TRENDING, sphere, token, null, false);
        data.add(trending);
        if (this.getStatus(trending) != NnStatusCode.SUCCESS) {
            return this.assembleSections(data);
        }
        //5. recommended
        log.info ("[quickLogin] recommended channels");
        String recommended = (String) this.channelStack(Tag.RECOMMEND, sphere, token, null, false);        
        data.add(recommended);
        if (this.getStatus(recommended) != NnStatusCode.SUCCESS) {
            return this.assembleSections(data);
        }
        //6. featured
        log.info ("[quickLogin] featured channels");
        String featured = (String) this.channelStack(Tag.FEATURED, sphere, token, null, false);
        data.add(featured);
        if (this.getStatus(featured) != NnStatusCode.SUCCESS) {
            return this.assembleSections(data);
        }
        //7. hottest
        log.info ("[quickLogin] hot channels");
        String hot = (String) this.channelStack(Tag.HOT, sphere, token, null, false);
        data.add(hot);
        if (this.getStatus(hot) != NnStatusCode.SUCCESS) {
            return this.assembleSections(data);
        }
        //8. category top level
        // log.info ("[quickLogin] top level categories: " + ((sphere == null) ? "default" : sphere));
        // hardcoding to English for now, and keeping translations on the player side
        log.info ("[quickLogin] top level categories: en");
        String categoryTop = (String) this.category (null, "en", false);
        data.add(categoryTop);
        return this.assembleSections(data);
    }

    private String assembleSections(List<String> data) {
        String output = "";
        for (String d : data) {
            if (d != null) {
               d = d.replaceAll("null", "");
            }            
            output += d + "----\n";
        }
        return output;                
    }

    public Object favorite(String userToken, String channel, String program, String fileUrl, String name, String imageUrl, String duration, boolean delete) {
        if (userToken == null || (program == null && fileUrl == null) || channel == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        String[] result = {""};                

        NnUser u = NNF.getUserMngr().findByToken(userToken, mso.getId());
        if (u == null)
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);        
        long pid = 0;
        NnChannel c = NNF.getChannelMngr().findById(Long.parseLong(channel));
        if (c == null)
            return this.assembleMsgs(NnStatusCode.CHANNEL_INVALID, null);
        NnProgram p = null;
        NnEpisode e = null;
        if (program != null) {
            if (program.contains("e")) {
                program = program.replace("e", "");
                e = NNF.getEpisodeMngr().findById(Long.parseLong(program));
                if (e == null)
                    return this.assembleMsgs(NnStatusCode.PROGRAM_INVALID, null);
            } else {
                pid = Long.parseLong(program);
                p = NNF.getProgramMngr().findById(Long.parseLong(program));
                if (p == null)
                    return this.assembleMsgs(NnStatusCode.PROGRAM_INVALID, null);
            }
        }
        //delete pid should not contain "e"
        if (delete) {
            NNF.getChannelMngr().deleteFavorite(u, pid);
        } else {
            NNF.getChannelMngr().saveFavorite(u, c, e, p, fileUrl, name, imageUrl, duration);
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);

    }
        
    public NnChannel getChannel(String channel) {
        NnChannel c = NNF.getChannelMngr().findById(Long.parseLong(channel));
        return c;
    }
    
    public Object graphSearch(String email, String name) {
        if (email == null && name == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        List<NnUser> users = NNF.getUserMngr().search(email, name, null, mso.getId());
        String[] result = {""};
        for (NnUser u : users) {
            result[0] += u.getUserEmail() + "\t" + u.getProfile().getName() + "\n";
        }        
        return this.assembleMsgs(NnStatusCode.SUCCESS, result); 
    }
    
    public Object userInvite(String token, String toEmail, String toName, String channel, HttpServletRequest req) {
        if (token == null || toEmail == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        NnUser user = NNF.getUserMngr().findByToken(token, mso.getId());
        if (user == null) {
            return this.assembleMsgs(NnStatusCode.ACCOUNT_INVALID, null);
        }
        NnChannel c = NNF.getChannelMngr().findById(Long.parseLong(channel));
        if (c == null) {
            return this.assembleMsgs(NnStatusCode.CHANNEL_INVALID, null);
        }
        EmailService service = new EmailService();
        UserInviteDao dao = new UserInviteDao();
        UserInvite invite = dao.findInitiate(user.getId(), user.getShard(), toEmail, Long.parseLong(channel));
        if (invite != null) {
            log.info("old invite:" + invite.getId());
            return this.assembleMsgs(NnStatusCode.SUCCESS, new String[] {invite.getInviteToken()});
        }
        String inviteToken = UserInvite.generateToken();        
        invite = new UserInvite(user.getShard(), user.getId(), 
                                inviteToken, c.getId(), toEmail, toName);
        
        invite = new UserInviteDao().save(invite);
        NnUserProfile profile = user.getProfile();
        String content = UserInvite.getInviteContent(user, invite.getInviteToken(), toName, profile.getName(), req); 
        NnEmail mail = new NnEmail(toEmail, toName, NnEmail.SEND_EMAIL_SHARE,                                   
                                   profile.getName(), user.getUserEmail(), UserInvite.getInviteSubject(), 
                                   content);
        log.info("email content:" + UserInvite.getInviteContent(user, invite.getInviteToken(), toName, profile.getName(), req));
        service.sendEmail(mail, null, null);
        String[] result = {invite.getInviteToken()};
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);        
    }

    public Object inviteStatus(String inviteToken) {
        UserInvite invite = new UserInviteDao().findByToken(inviteToken);
        if (invite == null)
            return this.assembleMsgs(NnStatusCode.INVITE_INVALID, null);
        String[] result = {String.valueOf(invite.getStatus())};        
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);        
    }
    
    public Object disconnect(String userToken, String email, String channel, HttpServletRequest req) {
        if (userToken == null || email == null || channel == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        NnUser user = NNF.getUserMngr().findByToken(userToken, mso.getId());
        if (user == null) {
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
        }
        NnUser invitee = NNF.getUserMngr().findByEmail(email, mso.getId(), req);
        if (invitee == null) {
            log.info("invitee does not exist:" + email);
            return this.assembleMsgs(NnStatusCode.SUCCESS, null);
        }
        UserInviteDao dao = new UserInviteDao();
        List<UserInvite> invites = new UserInviteDao().findSubscribers(user.getId(), invitee.getId(), Long.parseLong(channel));
        if (invites.size() == 0) {
            log.info("invite not exist: user id:" + user.getId() + ";invitee id:" + invitee.getId());
            return this.assembleMsgs(NnStatusCode.SUCCESS, null);
        }
        for (UserInvite u : invites) {
            u.setInviteeId(0);
            dao.save(u);
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }

    @SuppressWarnings({ "rawtypes" })
    public Object notifySubscriber(String userToken, String channel, HttpServletRequest req) {
        NnUser user = NNF.getUserMngr().findByToken(userToken, mso.getId());
        if (user == null) {
            
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
        }
        Map<String, NnUser> umap = new HashMap<String, NnUser>();        
        Map<String, String> cmap = new HashMap<String, String>();
        String[] cid = channel.split(",");
        
        for (String id : cid) {
            NnChannel c = NNF.getChannelMngr().findById(Long.parseLong(id));
            if (c == null) {
                return this.assembleMsgs(NnStatusCode.CHANNEL_INVALID, null);
            }
            List<UserInvite> invites = new UserInviteDao().findSubscribers(user.getId(), user.getShard(), Long.parseLong(id));
            log.info("invite size with cid " + id + ":" + invites.size());
            for (UserInvite invite : invites) {    
                NnUser u = NNF.getUserMngr().findById(invite.getInviteeId(), invite.getShard());
                if (u != null) {
                    String content = "";
                    if (umap.containsKey(u.getUserEmail())) {
                        content = cmap.get(u.getUserEmail()) + ";" + c.getName();
                        cmap.put(u.getUserEmail(), content);
                    } else {
                        umap.put(u.getUserEmail(), u);
                        cmap.put(u.getUserEmail(), c.getName());
                    }                    
                }
            }
        }
        Set set = umap.entrySet(); 
        Iterator i = set.iterator(); 
        while(i.hasNext()) { 
            Map.Entry me = (Map.Entry)i.next();
            NnUser u = (NnUser) me.getValue();
            String ch = cmap.get(u.getUserEmail());
            String subject = UserInvite.getNotifySubject(ch);
            String content = UserInvite.getNotifyContent(ch);
            log.info("send to " + u.getUserEmail());
            log.info("subject:" + subject);
            log.info("content:" + content);
            NnUserProfile profile = user.getProfile();
            NnEmail mail = new NnEmail(u.getUserEmail(), profile.getName(), NnEmail.SEND_EMAIL_SHARE, profile.getName(), user.getUserEmail(), subject, content);        
            new EmailService().sendEmail(mail, null, null);                        
        }         
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }

    public Object curator(String profile, String userToken, String stack, HttpServletRequest req) {
        if (stack == null && profile == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        NnUserManager userMngr = NNF.getUserMngr();
        List<NnUser> users = new ArrayList<NnUser>();
        if (profile != null) {
            NnUser user = userMngr.findByProfileUrl(profile, mso.getId());
            if (user == null)
                return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
            users.add(user);
        } else {
            users = userMngr.findFeatured(mso.getId());
        }
        NnUser user = null;
        if (userToken != null) {
            user = userMngr.findByToken(userToken, mso.getId());            
        }
        
        String[] result = {"", ""};
        boolean isAllChannel = false;
        if (profile != null && user != null && users.size() > 0 && users.get(0).getToken().equals(user.getToken())) {
            log.info("find curator channels");
            isAllChannel = true;
        } else {
            log.info("find channels curator created for public");
        }
         
        if (stack != null)
            result[0] = userMngr.composeCuratorInfo(users, true, isAllChannel, req, version);
        else
            result[0] = userMngr.composeCuratorInfo(users, false, isAllChannel, req, version);
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
        
    public Object virtualChannel(String stack, String lang, String userToken, String channel, boolean isPrograms) {
        //check input
        if (stack == null && userToken == null && channel == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        //get channels
        List<NnChannel> channels = new ArrayList<NnChannel>();
        NnUser user = null;
        boolean chPos = false;
        if (userToken != null) {
            user = NNF.getUserMngr().findByToken(userToken, mso.getId());
            if (user == null) {
                NnGuest guest = new NnGuestManager().findByToken(userToken);
                if (guest == null)
                    return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
                else
                    return this.assembleMsgs(NnStatusCode.SUCCESS, null);
            }
            NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
            channels.addAll(subMngr.findSubscribedChannels(user));
            chPos = true;
            log.info("virtual channel find by subscriptions:" + user.getId());
        } else if (stack != null) {
            lang = this.checkLang(lang);
            log.info("virtual channel find by stack:" + stack + ";lang=" + lang);
            String[] cond = stack.split(",");
            for (String s : cond) {
                if (s.equals(Tag.RECOMMEND)) {
                    channels.addAll(new RecommendService().findRecommend(userToken, mso.getId(), lang));
                } else if (s.equals("mayLike")) {                
                    return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
                } else {
                    long displayId = 0;
                    long systagId = 0;
                    if (stack.contains("-")) {
                        String ids[] = stack.split("-");
                        log.info("ids.length:" + ids.length);
                        if (ids.length == 2) {
                            displayId = Long.parseLong(ids[0]);
                            systagId = Long.parseLong(ids[1]);
                            log.info("display id:" + displayId + ";"+ "systag id:" + systagId);
                        }
                    } else if (!Pattern.matches("^\\d*$", stack)) {
                        log.info("channelStack findbyname:" + stack);                        
                        SysTagDisplay display = NNF.getDisplayMngr().findByName(stack, mso.getId());
                        if (display != null)
                            systagId = display.getSystagId();
                    } else {
                        log.info("channelStack findbyid:" + stack);
                        SysTagDisplay display = NNF.getDisplayMngr().findById(Long.parseLong(stack));
                        if (display != null)
                            systagId = display.getSystagId();
                    }                    
                    channels.addAll(NNF.getSysTagMngr().findPlayerChannelsById(systagId, lang, true, 0));
                }
            }
        } else if (channel != null) {
            log.info("virtual channel find by channel ids:" + channel);
            String[] chArr = channel.split(",");
            if (chArr.length > 0) {
                List<Long> list = new ArrayList<Long>();
                for (int i=0; i<chArr.length; i++) { list.add(Long.valueOf(chArr[i]));}                
                channels.addAll(NNF.getChannelMngr().findByIds(list));
            }
        }
        if (!isPrograms) {
            String channelInfo = (String)NNF.getChannelMngr().composeReducedChannelLineup(channels, PlayerApiService.FORMAT_PLAIN);
            if (chPos)
                channelInfo = (String)NNF.getChannelMngr().chAdjust(channels, channelInfo, new ArrayList<ChannelLineup>(), this.format);
            log.info("virtual channel, return channel only");
            return this.assembleMsgs(NnStatusCode.SUCCESS, new String[] {channelInfo});
        }    
        //get programs
        List<String> result = new ArrayList<String>();        
        List<YtProgram> ytprograms = new YtProgramDao().findByChannels(channels);
        String programInfo = "";
        for (YtProgram p : ytprograms) {
            String[] ori = {
                    String.valueOf(p.getChannelId()),
                    p.getYtVideoId(),
                    p.getPlayerName(),
                    String.valueOf(p.getUpdateDate().getTime()),
                    p.getDuration(),
                    p.getImageUrl(),
                    p.getPlayerIntro(),
            };
            String output = NnStringUtil.getDelimitedStr(ori);
            output = output.replaceAll("null", "");
            output += "\n";
            programInfo += output;
        }
        String channelInfo = (String) NNF.getChannelMngr().composeReducedChannelLineup(channels, PlayerApiService.FORMAT_PLAIN);
        if (chPos) {
            log.info("adjust sequence of channellineup for user:" + user.getId());
            channelInfo = (String) NNF.getChannelMngr().chAdjust(channels, channelInfo, new ArrayList<ChannelLineup>(), PlayerApiService.FORMAT_PLAIN);
        }
        result.add(channelInfo);                
        result.add(programInfo);
        String size[] = new String[result.size()];
        return this.assembleMsgs(NnStatusCode.SUCCESS, result.toArray(size));        
    }
    
    public Object portal(String lang, String time, boolean minimal, String type) {
        lang = this.checkLang(lang);    
        if (lang == null)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        short baseTime = Short.valueOf(time);
        if (baseTime > 23 || baseTime < 0)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        
        if (type == null)
           type = "portal";
        if (type != null && type.equals("whatson"))
            return this.assembleMsgs(NnStatusCode.SUCCESS, NNF.getDisplayMngr().getPlayerWhatson(lang, baseTime, this.format, mso, minimal, version));
        
        return this.assembleMsgs(NnStatusCode.SUCCESS, NNF.getDisplayMngr().getPlayerPortal(lang, minimal, version, format, mso));            
    }

    public Object whatson(String lang, String time, boolean minimal) {
        lang = this.checkLang(lang);    
        if (lang == null)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        short baseTime = Short.valueOf(time);
        if (baseTime > 23 || baseTime < 0)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        
        return this.assembleMsgs(NnStatusCode.SUCCESS, NNF.getDisplayMngr().getPlayerWhatson(lang, baseTime, this.format, mso, minimal, version));                    
    }
  
    public Object frontpage(String time, String stack, String user) {
        short baseTime = Short.valueOf(time);
        String lang = LangTable.LANG_EN;
        lang = mso.getLang();
        //section 1: items
        List<String> data = new ArrayList<String>();
        String[] itemOutput = {""};

        SysTagDisplayManager displayMngr = NNF.getDisplayMngr();
        List<SysTagDisplay> displays = new ArrayList<SysTagDisplay>();
        //1. dayparting
        SysTagDisplay dayparting = displayMngr.findDayparting(baseTime, lang, mso.getId());
        if (dayparting != null)
            displays.add(dayparting);
        //2. on previosly 
        SysTagDisplay previously = displayMngr.findPrevious(mso.getId(), lang, dayparting);
        if (previously != null)
            displays.add(previously);
        //2.5. newly added        
        displays.addAll(displayMngr.findRecommendedSets(lang, mso.getId()));
        //3. following
        SysTagDisplay follow = displayMngr.findByType(mso.getId(), SysTag.TYPE_SUBSCRIPTION, lang);
        if (follow != null)
            displays.add(follow);
        //4 account
        SysTagDisplay account = displayMngr.findByType(mso.getId(), SysTag.TYPE_ACCOUNT, lang);        
        displays.add(account);   
        for (int i=0; i<displays.size(); i++) {            
            SysTagDisplay d = displays.get(i);
            int opened = 0;
            if (i == 0) opened = 1; 
            String stackname = d.getId() + "-" + d.getSystagId();
            if (i > 1) {
                stackname = "0";
            }
            String[] ori = {
                    d.getName(), //name
                    String.valueOf(NNF.getSysTagMngr().convertDashboardType(d.getSystagId())), //type
                    String.valueOf(stackname), //stackName
                    String.valueOf(opened), //opened, only daypartying is open
                    String.valueOf("0"), //icon, always zero
                 };    
            itemOutput[0] += NnStringUtil.getDelimitedStr(ori) + "\n";           
        }
        itemOutput[0] = itemOutput[0].replaceAll("null", "");
        itemOutput[0] = (String) this.assembleMsgs(NnStatusCode.SUCCESS, itemOutput);
        data.add(itemOutput[0]);
        String sphere = mso.getLang();
        try {
            //section 2: virtual channels
            String virtualOutput = "";
            String stackName = String.valueOf(dayparting.getId());
            virtualOutput = (String) this.virtualChannel(stackName, sphere, user, null, false);
            data.add(virtualOutput);
            return this.assembleSections(data);
        } catch (Exception e) {
            NnLogUtil.logException(e);
            return this.assembleSections(data);
        }
    }

    public Object virtualChannelAdd(String user, String channel, String payload, 
                                    boolean isQueued, HttpServletRequest req) {        
        if (user == null || payload == null || channel == null) {
            log.info("data is missing");
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }

        if (isQueued) {        
            log.info("virtual channel from user:" + user + " throwing to queue" );
            String url = "/playerAPI/virtualChannelAdd?queued=false";
            String data = "channel=" + channel + "&payload=" + payload + ";&user=" + user;             
            QueueFactory.add(url, QueueFactory.METHOD_POST, QueueFactory.CONTENTTYPE_TEXT, data);
            return this.assembleMsgs(NnStatusCode.SUCCESS, null);
        }
        log.info("--- process virtual channels ---");
        YtProgramDao dao = new YtProgramDao();        
        String[] lines = payload.split("\n");
        log.info("lines:" + lines.length);
        List<YtProgram> ytprograms = new ArrayList<YtProgram>();        
        for (String line : lines) {
            String[] tabs = line.split("\t");
            log.info("columns:" + tabs.length);
            if (tabs.length >= 9) {
                try {
                    String chstr = tabs[0];
                    String ytUserName = tabs[1];
                    String crawlD = tabs[2];
                    String ytVideoId = tabs[3];
                    String name = tabs[4];
                    String updateD = tabs[5];
                    String duration = tabs[6];
                    String imageUrl = tabs[7];
                    String intro = tabs[8];
                    log.info("updateD:" + updateD + ";crawD:" + crawlD);                    
                    YtProgram program = dao.findByVideo(ytVideoId);
                    if (program == null) {
                       long nowepoch = System.currentTimeMillis()/1000;
                       long chId = Long.parseLong(chstr);
                       Date updateDate = null;
                       long epoch = 0;
                       if (updateD != null) {
                           if (updateD.length() > String.valueOf(nowepoch).length()) {
                               updateD = updateD.substring(0, updateD.length() - 3);
                               log.info("updateD assuming is in milliseconds, change to:" + updateD);
                           }                           
                          epoch = Long.parseLong(updateD);
                          updateDate= new Date (epoch*1000);
                       }
                       Date crawlDate = null;
                       if (crawlD != null) {
                           if (crawlD.length() > String.valueOf(nowepoch).length()) {
                               crawlD = crawlD.substring(0, crawlD.length() - 3);
                               log.info("updateD assuming is in milliseconds, change to:" + crawlD);
                           }
                          epoch = Long.parseLong(crawlD);
                          crawlDate = new Date (epoch*1000);
                       }
                       YtProgram ytprogram = new YtProgram(chId, ytUserName, ytVideoId, 
                                                           name, duration, imageUrl, 
                                                           intro, crawlDate, updateDate);
                       log.info("ytprogram:" + chId + ";" + ytUserName + ";" + ytVideoId + ";" +
                                ";" + name + ";" + duration + ";" + imageUrl + ";" + intro + ";" + 
                                crawlDate + ";" + updateDate);
                       ytprograms.add(ytprogram);
                       dao.save(ytprogram);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }        
        //dao.saveAll(ytprograms);
        log.info("new ytprograms size:" + ytprograms.size());
        int existedSize = lines.length - ytprograms.size();
        log.info("existed ytprograms size:" + existedSize);
        NnChannel c = NNF.getChannelMngr().findById(Long.parseLong(channel));
        if (c != null) {
            if (c.getStatus() == NnChannel.STATUS_PROCESSING) {
                log.info("change channel status from processing to success:" + c.getId());
                c.setStatus(NnChannel.STATUS_SUCCESS);
                NNF.getChannelMngr().save(c);
            }
        }
        
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }
    
    public Object bulkIdentifier(String ytUsers) {
        //input
        if (ytUsers == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }   
        //channels
        List<NnChannel> channels = new ArrayList<NnChannel>();
        String[] ytUser = ytUsers.split(",");        
        for (String yt : ytUser) {
            if (yt.trim().length() > 0) {
                yt = "http://www.youtube.com/user/" + yt;
                NnChannel existed = NNF.getChannelMngr().findBySourceUrl(yt);
                if (existed == null) {
                    NnChannel c = NNF.getChannelMngr().createYoutubeChannel(yt);
                    if (c != null) channels.add(c);
                } else {
                    channels.add(existed);
                }
            }
        }
        String channelInfo = (String) NNF.getChannelMngr().composeReducedChannelLineup(channels, PlayerApiService.FORMAT_PLAIN);        
        return this.assembleMsgs(NnStatusCode.SUCCESS, new String[] {channelInfo});
    }
    
    public Object bulkSubscribe(String userToken, String ytUsers) {
        //input
        if (userToken == null || ytUsers == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        //user
        NnUser user = NNF.getUserMngr().findByToken(userToken, mso.getId());
        if (user == null) 
            return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
        //channels
        List<NnChannel> channels = new ArrayList<NnChannel>();
        String[] ytUser = ytUsers.split(",");        
        for (String yt : ytUser) {
            if (yt.trim().length() > 0) {
                yt = "http://www.youtube.com/user/" + yt;
                NnChannel existed = NNF.getChannelMngr().findBySourceUrl(yt);
                if (existed == null) {
                    NnChannel c = NNF.getChannelMngr().createYoutubeChannel(yt);
                    if (c != null) channels.add(c);
                } else {
                    channels.add(existed);
                }
            }
        }
        String channelInfo = (String) NNF.getChannelMngr().composeReducedChannelLineup(channels, PlayerApiService.FORMAT_PLAIN);        
        //subscribe
        NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
        List<NnUserSubscribe> list = subMngr.findAllByUser(user);
        Map<Long, NnUserSubscribe> map = new HashMap<Long, NnUserSubscribe>();        
        for (NnUserSubscribe s : list) {
            map.put(s.getChannelId(), s);
        }
        for (NnChannel c : channels) {
            if (!map.containsKey(c.getId())) {
                log.info("user automate subscribe:" + user.getToken() + ";" + c.getId());
                subMngr.subscribeChannel(user, c.getId(), (short)0, MsoIpg.TYPE_GENERAL);
            }
            map.remove(c.getId());
        }
        //unsubscribe
        Iterator<Entry<Long, NnUserSubscribe>> it = map.entrySet().iterator();
        channels.clear();
        while (it.hasNext()) {
            Map.Entry<Long, NnUserSubscribe> pairs = it.next();
            NnUserSubscribe s = pairs.getValue(); 
            NnChannel c = NNF.getChannelMngr().findById(s.getChannelId());
            if (c != null && (c.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL || 
                              c.getContentType() == NnChannel.CONTENTTYPE_YOUTUBE_PLAYLIST)) {
                log.info("auto unsubscribe channel: " + c.getId());
                subMngr.unsubscribeChannel(user, s);                
            }
        }        
        
        return this.assembleMsgs(NnStatusCode.SUCCESS, new String[] {channelInfo});        
    }    
    
    public Object obtainAccount(String email, String password, String name, 
            HttpServletRequest req, HttpServletResponse resp) {        
       if (email == null || password == null) {
           return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
       }
       //find existed account
       NnUser user = NNF.getUserMngr().findByEmail(email, mso.getId(), req);
       if (user != null) {
           log.info("returning youtube connect account"); 
           return this.login(email, password, req, resp);           
       }        
       //signing up a new one
       //verify inputs
       int status = NnUserValidator.validatePassword(password);
       if (status != NnStatusCode.SUCCESS)
           return this.assembleMsgs(status, null);
       boolean success = BasicValidator.validateEmail(email);
       if (!success)
           return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
       //create new account
       log.info("signing up a new youtube connect account");
       short type = NnUser.TYPE_USER;
       if ((email.contains("-AT-") || email.contains("-at-"))
               && email.contains("@9x9.tv")) {
           type = NnUser.TYPE_YOUTUBE_CONNECT;
       }
       if (name == null)
           name = email;
       NnUser newUser = new NnUser(email, password, type, mso.getId());
       NnUserProfile profile = new NnUserProfile(mso.getId(), name, LangTable.LANG_EN, LangTable.LANG_EN, null);
       newUser.setProfile(profile);
       newUser.setTemp(false);
       status = NNF.getUserMngr().create(newUser, req, (short)0);
       if (status != NnStatusCode.SUCCESS)
           return this.assembleMsgs(status, null);
       String result[] = {(String) this.prepareUserInfo(newUser, null, req, false)};
       this.setUserCookie(resp, CookieHelper.USER, newUser.getToken());       
       return this.assembleMsgs(NnStatusCode.SUCCESS, result);       
    }

    public Object channelUpdate(String user, String payload, 
                             boolean isQueued, HttpServletRequest req) {        
        if (user == null || payload == null) {
           log.info("data is missing");
           return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }

        if (isQueued) {        
           log.info("channel update from user:" + user + " throwing to queue" );
           String url = "/playerAPI/channelUpdate?queued=false";
           String data = "payload=" + payload + ";&user=" + user;             
           QueueFactory.add(url, QueueFactory.METHOD_POST, QueueFactory.CONTENTTYPE_TEXT, data);
           return this.assembleMsgs(NnStatusCode.SUCCESS, null);
        }
        log.info("--- process channels ---");        
        String[] lines = payload.split("\n");
        log.info("lines:" + lines.length);
        List<NnChannel> channels = new ArrayList<NnChannel>();        
        for (String line : lines) {
            String[] tabs = line.split("\t");
            log.info("columns:" + tabs.length);
            if (tabs.length >= 4) {
                try {
                    String id = tabs[0];
                    String name = tabs[2];
                    String imageUrl = tabs[3];
                    log.info("id:" + id + ";name:" + name + ";imageurl:" + imageUrl);
                    NnChannel channel = NNF.getChannelMngr().findById(Long.parseLong(id));
                    if (channel != null) {
                        if (name != null) {
                            int len = (name.length() > 255 ? 255 : name.length()); 
                            name = name.replaceAll("\\s", " ");                
                            name = name.substring(0, len);           
                        }
                        channel.setName(name);
                        channel.setImageUrl(imageUrl);
                        channels.add(channel);
                        NNF.getChannelMngr().save(channel);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("channel updated:" + channels.size());
        //chMngr.saveAll(channels);
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
     }
    
    public Object latestEpisode(String channel) {
        //check input
        if (channel == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        String[] result = {""};        
        List<YtProgram> ytprograms = new YtProgramDao().findOneLatestByChannelStr(channel);
        for (YtProgram p : ytprograms) {
            String[] ori = {
                    String.valueOf(p.getChannelId()),
                    p.getYtVideoId(),
                    p.getImageUrl(),
            };
            String output = NnStringUtil.getDelimitedStr(ori);
            output = output.replaceAll("null", "");
            output += "\n";
            result[0] += output;
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);        
    }

    public Object setInfo(String id, String name, String time, boolean isProgramInfo) {
        if (id == null && name == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        if (mso == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        }        
        if (id != null && id.startsWith("s")) id = id.replace("s", "");
        
        SysTagDisplay display = null;
        long displayId = 0;
        long systagId = 0;
        if (id != null) {
            if (id.contains("-")) {
                String ids[] = id.split("-");
                log.info("ids.length:" + ids.length);
                if (ids.length == 2) {
                    displayId = Long.parseLong(ids[0]);
                    systagId = Long.parseLong(ids[1]);
                    log.info("display id:" + displayId + ";"+ "systag id:" + systagId);
                }
            } else {
                displayId = Long.parseLong(id);
                log.info("regular form:" + displayId);
            }
            display = NNF.getDisplayMngr().findById(displayId);
        } else {
            display = NNF.getDisplayMngr().findByName(name, mso.getId());
        }
        if (display == null)
            return this.assembleMsgs(NnStatusCode.SET_INVALID, null);
        if (systagId == 0)
            systagId = display.getSystagId();         
        SysTag systag = NNF.getSysTagMngr().findById(systagId);
        List<NnChannel> channels = new ArrayList<NnChannel>();
        if (systag.getType() == SysTag.TYPE_DAYPARTING) {
            channels.addAll(NNF.getSysTagMngr().findDaypartingChannelsById(systagId, display.getLang(), mso.getId(), Short.parseShort(time)));
        } else if (systag.getType() == SysTag.TYPE_WHATSON) {
            channels.addAll(NNF.getSysTagMngr().findPlayerAllChannelsById(systagId, display.getLang(), SysTag.SORT_SEQ, mso.getId()));            
        } else {
            channels.addAll(NNF.getSysTagMngr().findPlayerChannelsById(systagId, null, systag.getSorting(), 0));
        }
        List<NnProgram> programs = new ArrayList<NnProgram>();
        Short shortTime = 24;
        if (time != null)
            shortTime = Short.valueOf(time);        
        return this.assembleMsgs(NnStatusCode.SUCCESS, NNF.getDisplayMngr().getPlayerSetInfo(mso, systag, display, channels, programs, version, this.format, shortTime, isProgramInfo));
    }

    public Object endpointRegister(String userToken, String token, String vendor, String action) {
        //input verification
        if (userToken == null || token == null || vendor == null || action == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        @SuppressWarnings("rawtypes")
        HashMap map = this.checkUser(userToken, false);
        if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
            return this.assembleMsgs((Integer)map.get("s"), null);
        }
        NnUser user = (NnUser) map.get("u");
        if (!action.equals("register") && !action.equals("unregister"))
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        boolean register = true;
        if (action.equals("unregister"))
            register = false;        
        user = (NnUser) map.get("u");
        //endpoint verify
        EndPointManager endpointMngr = new EndPointManager();
        short srtVendor = endpointMngr.getVendorType(vendor);
        if (srtVendor == EndPoint.VENDOR_UNDEFINED)
            return this.assembleMsgs(NnStatusCode.INPUT_BAD, null);
        EndPoint endpoint = endpointMngr.findByEndPoint(user.getId(), mso.getId(), srtVendor);
        if (register) {
            if (endpoint == null) {
                endpoint = new EndPoint(user.getId(), mso.getId(), token, srtVendor);                
            } else {
                endpoint.setToken(token);
            }
            endpointMngr.save(endpoint);            
            return this.assembleMsgs(NnStatusCode.SUCCESS, null);
        }
        if (!register) {
            if (endpoint == null) { 
                return this.assembleMsgs(NnStatusCode.DEVICE_INVALID, null);
            } else {
                endpointMngr.delete(endpoint);
            }
        }
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }

    public Object poiAction(String userToken, String deviceToken, String vendor, String poiId, String select) {
        //input verification
        if (userToken == null || poiId == null || select == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        @SuppressWarnings("rawtypes")
        HashMap map = this.checkUser(userToken, false);
        if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
            return this.assembleMsgs((Integer)map.get("s"), null);
        }
        NnUser user = (NnUser) map.get("u");
        long lPoiId = Long.parseLong(poiId);
        Poi poi = NNF.getPoiPointMngr().findPoiById(lPoiId);
        if (poi == null)
            return this.assembleMsgs(NnStatusCode.POI_INVALID, null); //poi invalid
        PoiEvent event = NNF.getPoiEventMngr().findByPoi(lPoiId);
        if (event == null) {
            log.info("event invalid");
            return this.assembleMsgs(NnStatusCode.POI_INVALID, null); //poi invalid
        }
        //?! requirement question: or it's user based
        if (event.getType() == PoiEvent.TYPE_INSTANTNOTIFICATION || event.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            if (deviceToken == null || vendor == null)
                return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
            EndPointManager endpointMngr = new EndPointManager();
            EndPoint endpoint = endpointMngr.findByEndPoint(user.getId(), user.getMsoId(), endpointMngr.getVendorType(vendor));
            if (endpoint == null)
                return this.assembleMsgs(NnStatusCode.DEVICE_INVALID, null);
        }
        //record action
        PdrManager pdrMngr = new PdrManager();
        if (event.getType() == PoiEvent.TYPE_POPUP) {
            //TYPE_POPUP, actually won't happen here, but in pdr api, data will be stored in pdr table        
            pdrMngr.processPoi(user, poi, event, select);
        } else if (event.getType() == PoiEvent.TYPE_HYPERLINK) {
            //TYPE_HYPERLINK, actually won't happen here, but in pdr api, data will be stored in pdr table
            pdrMngr.processPoi(user, poi, event, select);
        } else if (event.getType() == PoiEvent.TYPE_INSTANTNOTIFICATION) {
            //instantNotificationPush (push to apns)
            //put into queue            
            String msg = NnStringUtil.urlencode(event.getMessage());
            String url = "/notify/send?device=" + deviceToken + "&msg=" + msg + "&vendor="+ vendor;
            log.info("url:" + url);
            QueueFactory.add(url, null);
        } else if (event.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            PoiPdr pdr = pdrMngr.findPoiPdr(user, lPoiId);
            if (pdr != null) {
                return this.assembleMsgs(NnStatusCode.POI_DUPLICATED, null);
            } else {
                pdrMngr.processPoiScheduler(user, poi, event, select);
                return this.assembleMsgs(NnStatusCode.SUCCESS, null);
            }
        } else if (event.getType() == PoiEvent.TYPE_POLL) {
            //TYPE_POLL, data will be saved in poi_pdr;
            PoiPdr pdr = pdrMngr.findPoiPdr(user, lPoiId);
            if (pdr != null) {
                return this.assembleMsgs(NnStatusCode.POI_DUPLICATED, null);
            } else {
                pdrMngr.processPoiPdr(user, poi, event, select);
                return this.assembleMsgs(NnStatusCode.SUCCESS, null);
            }
        }
        
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);
    }
    
    public Object shareInChannelList(Long channelId, HttpServletRequest req) {
        
        if (channelId == null)
            return this.assembleMsgs(NnStatusCode.CHANNEL_INVALID, null);
        
        NnChannel channel = NNF.getChannelMngr().findById(channelId);
        if (channel == null)
            return this.assembleMsgs(NnStatusCode.CHANNEL_INVALID, null);
        
        List<NnChannel> channels = new ArrayList<NnChannel>();
        NnUser curator = null;
        String result[] = {"", "", "", ""};
        
        //mso info
        result[0] += PlayerApiService.assembleKeyValue("name", mso.getName());
        result[0] += PlayerApiService.assembleKeyValue("imageUrl", mso.getLogoUrl()); 
        result[0] += PlayerApiService.assembleKeyValue("intro", mso.getIntro());            
        
        if (channel.getContentType() != NnChannel.CONTENTTYPE_MIXED) {
            
            channels.add(channel);
            
            //set info
            result[1] += PlayerApiService.assembleKeyValue("id", String.valueOf(channel.getId()));
            result[1] += PlayerApiService.assembleKeyValue("name", channel.getName());
            result[1] += PlayerApiService.assembleKeyValue("imageUrl", channel.getImageUrl());
            
        } else {
            
            curator = NNF.getUserMngr().findByIdStr(channel.getUserIdStr(), MsoManager.getSystemMsoId()); // use 9x9 profile
            if (curator == null)
                return this.assembleMsgs(NnStatusCode.USER_INVALID, null);
            
            List<NnChannel> curatorChannels = NNF.getChannelMngr().findByUser(curator, 0, false);
            log.info("curatorChannels = " + curatorChannels.size());
            
            for (NnChannel ch : curatorChannels) {
                if ((ch.getStatus() == NnChannel.STATUS_SUCCESS || ch.getStatus() == NnChannel.STATUS_WAIT_FOR_APPROVAL) &&
                     ch.isPublic() && ch.getContentType() == NnChannel.CONTENTTYPE_MIXED)
                    
                    channels.add(ch);
            }
            
            Collections.sort(channels, NnChannelManager.getComparator("seq"));
            
            //set info
            result[1] += PlayerApiService.assembleKeyValue("id", String.valueOf(curator.getId()));
            result[1] += PlayerApiService.assembleKeyValue("name", curator.getProfile().getName());
            result[1] += PlayerApiService.assembleKeyValue("imageUrl", curator.getProfile().getImageUrl());
        }
        log.info("channels = " + channels.size());
        result[2] = (String) NNF.getChannelMngr().composeChannelLineup(channels, version, this.format);
        //program info
        String programStr = (String) NNF.getProgramMngr().findLatestProgramInfoByChannels(channels, PlayerApiService.FORMAT_PLAIN);
        result[3] = programStr;        
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);        
    }
    
    public String getAppDomain() {
        
        if (context == null)
            return null;
        
        return context.getAppDomain();
    }
    
    public Boolean isProductionSite() {
        
        if (context == null)
            return null;
        
        return context.isProductionSite();
    }
    
    /*
    public Object solr(String text) {
        //check input
        if (text == null) {
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        }
        SolrLib lib = new SolrLib();
        lib.searchBySolrJ(text);
        return this.assembleMsgs(NnStatusCode.SUCCESS, null);        
    }
    */
    
    //url = "http://vimeo.com/" + videoId;
    public Object getVimeoDirectUrl(String url) {
        if (url == null)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        String videoUrl = VimeoLib.getDirectVideoUrl(url);
        if (videoUrl == null)
           this.assembleMsgs(NnStatusCode.PROGRAM_ERROR, null);
        log.info("vimeo url:" + videoUrl);
        String data = PlayerApiService.assembleKeyValue("url", videoUrl);
        String[] result = {data};
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object generateSignedUrls(String url) {
        if (url == null || url.length() == 0)
            return this.assembleMsgs(NnStatusCode.INPUT_MISSING, null);
        String[] urls = url.split(",");
        MsoConfigManager configMngr = NNF.getConfigMngr();
        // NOTE: maybe the mso can be determined by bucket name
        MsoConfig cfSubomainConfig = configMngr.findByMsoAndItem(mso, MsoConfig.CF_SUBDOMAIN);
        if (cfSubomainConfig == null)
        	return this.assembleMsgs(NnStatusCode.DATA_ERROR, null);
        MsoConfig cfKeypairConfig = configMngr.findByMsoAndItem(mso, MsoConfig.CF_KEY_PAIR_ID);
        if (cfKeypairConfig == null)
        	return this.assembleMsgs(NnStatusCode.DATA_ERROR, null);
        String keypair = cfKeypairConfig.getValue();	
        String cfDomainStr = cfSubomainConfig.getValue() + ".cloudfront.net";
        String privateKeyPath = MsoConfigManager.getCfPrivateKeyPath(mso);
        log.info("private key path:" + privateKeyPath);
        String signedUrls = "";
        for (String u : urls) {
            
            Matcher matcher = Pattern.compile(AmazonLib.REGEX_S3_URL).matcher(u);
            if (matcher.find()) {
                signedUrls += AmazonLib.cfUrlSignature(cfDomainStr, privateKeyPath, keypair, matcher.group(2)) + "\n";            
            } else {
                signedUrls += u + "\n";
            }
        }
        String[] result = {signedUrls};
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object notificationList(String token, HttpServletRequest req) {
        
        if (token == null)
            return this.assembleMsgs(NnStatusCode.SUCCESS, null);
        
        NnDeviceNotificationManager notificationMngr = NNF.getDeviceNotiMngr();
        
        List<NnDevice> devices = NNF.getDeviceMngr().findByToken(token);
        if (devices.isEmpty())
            return this.assembleMsgs(NnStatusCode.DEVICE_INVALID, null);
        
        NnDevice device = devices.get(0);
        String badge = PlayerApiService.assembleKeyValue("badge", String.valueOf(device.getBadge()));
        
        if (req.getParameter("minimal") != null) {
            String[] result = { badge };
            return this.assembleMsgs(NnStatusCode.SUCCESS, result);
        }
        
        List<NnDeviceNotification> notifications = notificationMngr.findByDeviceId(device.getId());
        
        if (req.getParameter("clean") != null) {
            
            List<NnDeviceNotification> unreadNotifications = notificationMngr.findUnreadByDeviceId(device.getId());
            
            for (NnDeviceNotification unread : unreadNotifications) {
                unread.setRead(true);
            }
            device.setBadge(0);
            NNF.getDeviceMngr().save(device);
            notificationMngr.save(unreadNotifications);
        }
        
        Object output = notificationMngr.composeNotificationList(notifications);
        String[] result = { badge, (String) output };
        
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
    }
    
    @Override
    protected void finalize() throws Throwable {
        
        log.info(this.getClass().getName() + " is recycled");
    }
}
