package com.nncloudtv.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

import com.nncloudtv.dao.UserInviteDao;
import com.nncloudtv.dao.YtProgramDao;
import com.nncloudtv.exception.NotPurchasedException;
import com.nncloudtv.lib.AmazonLib;
import com.nncloudtv.lib.AuthLib;
import com.nncloudtv.lib.CookieHelper;
import com.nncloudtv.lib.FacebookLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.QueueFactory;
import com.nncloudtv.lib.SearchLib;
import com.nncloudtv.lib.stream.StreamFactory;
import com.nncloudtv.lib.stream.StreamLib;
import com.nncloudtv.lib.stream.YouTubeLib;
import com.nncloudtv.model.App;
import com.nncloudtv.model.Captcha;
import com.nncloudtv.model.EndPoint;
import com.nncloudtv.model.LocaleTable;
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
import com.nncloudtv.model.NnItem;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.NnPurchase;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserChannelSorting;
import com.nncloudtv.model.NnUserPref;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.model.NnUserReport;
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
    
    public final static int PAGING_ROWS  = 50;
    public final static int MAX_EPISODES = 200;
    
    @Override
    protected void finalize() throws Throwable {
        
        NnLogUtil.logFinalize(getClass().getName());
    }
    
    public int prepService(ApiContext ctx) {
        
        HttpServletRequest req = ctx.getReq();
        
        req.getSession().setMaxInactiveInterval(60);
        
        NnLogUtil.logUrl(req);
        
        MsoConfig brandExpireConfig = NNF.getConfigMngr().getByMsoAndItem(ctx.getMso(), MsoConfig.APP_EXPIRE);
        if (brandExpireConfig != null) {
            try {
                //"January 2, 2019";
                Date date = new SimpleDateFormat("MMMM d, yyyy").parse(brandExpireConfig.getValue());
                if (NnDateUtil.now().after(date)) {
                    log.info(String.format("mso %s expires!", ctx.getMsoName()));
                    return NnStatusCode.APP_EXPIRE;
                }
            } catch (ParseException e) {
                NnLogUtil.logException(e);
            }
        }
        MsoConfig appExpireConfig = NNF.getConfigMngr().getByMsoAndItem(ctx.getMso(), MsoConfig.APP_VERSION_EXPIRE);
        if (appExpireConfig != null) {
            String[] str = appExpireConfig.getValue().split(";");
            String appVersion = ctx.getAppVersion();
            for (String s : str) {
               if (appVersion != null && appVersion.equals(s)) {
                   log.info("expire version:" + appVersion);
                   return NnStatusCode.APP_VERSION_EXPIRE;
               }
            }
        }
        if (ctx.getVer() < checkApiMinimal())
            return NnStatusCode.API_FORCE_UPGRADE;
        else
            return checkRO();
    }
    
    //assemble key and value string
    public static String assembleKeyValue(String key, String value) {
        return key + "\t" + value + "\n";
    }
    
    //Prepare user info, it is used by login, guest register, userTokenVerify
    //object, UserInfo
    public Object prepareUserInfo(ApiContext ctx, NnUser user, NnGuest guest, boolean login) {
        return NNF.getUserMngr().getPlayerUserInfo(user, guest, ctx.getReq(), login, ctx.getFmt());
    }
    
    public void setUserCookie(HttpServletResponse resp, String cookieName, String userId) {
        CookieHelper.setCookie(resp, cookieName, userId);
    }
    
    public Object relatedApps(ApiContext ctx, String stack, String sphere) {
        
        short type = App.TYPE_IOS;
        if (ApiContext.OS_ANDROID.equalsIgnoreCase(ctx.getOs()))
            type = App.TYPE_ANDROID;
        
        List<App> featuredApps = NNF.getAppDao().findFeaturedBySphere(sphere, ctx.getMsoId());
        List<App> apps = new ArrayList<App>();
        apps.addAll(featuredApps);
        apps.addAll(NNF.getAppDao().findAllBySphere(sphere, ctx.getMsoId()));
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
        if (ctx.isJsonFmt())
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, new RelatedApp());
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
     }
    
    public Object listrecommended(ApiContext ctx) {
        
        List<SysTagDisplay> sets = new ArrayList<SysTagDisplay>();
        if (ctx.getVer() < 40) {
            sets.addAll(NNF.getDisplayMngr().find33RecommendedSets(ctx.getLang(), ctx.getMsoId()));
        } else {
            sets.addAll(NNF.getDisplayMngr().findRecommendedSets(ctx.getLang(), ctx.getMsoId()));
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
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object fbDeviceSignup(ApiContext ctx, FacebookMe me, String expire, String msoString, HttpServletResponse resp) {
        if (me.getAccessToken() == null || me.getId() == null || me.getEmail() == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        return this.fbSignup(ctx, me, expire, msoString, resp);
    }
    
    private Object fbSignup(ApiContext ctx, FacebookMe me, String expires, String msoName, HttpServletResponse resp) {
        long expire = 0;
        if (expires != null && expires.length() > 0)
            expire = Long.parseLong(expires); //TODO pass to FacebookMe
        
        Mso mso = NNF.getMsoMngr().findByName(msoName);
        if (mso == null) {
           mso = MsoManager.getSystemMso();
        }
        NnUserManager userMngr = NNF.getUserMngr();
        NnUser user = userMngr.findByEmail(me.getId(), mso.getId(), ctx.getReq());
        log.info("find user in db from fbId:" + me.getId());
        if (user == null) {
            log.info("FACEBOOK: signup with fb account:" + me.getEmail() + "(" + me.getId() + ")");
            user = new NnUser(me.getEmail(), me.getId(), me.getAccessToken());
            NnUserProfile profile = new NnUserProfile(mso.getId(), me.getName(), null, null, null);
            user.setProfile(profile);
            user.setExpires(NnDateUtil.timestamp() + expire);
            user.setTemp(false);
            user.setMsoId(mso.getId());
            user = userMngr.setFbProfile(user, me);
            int status = userMngr.create(user, ctx.getReq(), (short)0);
            if (status != NnStatusCode.SUCCESS)
                return ctx.assemblePlayerMsgs(status);
            //userMngr.subscibeDefaultChannels(user);
        } else {
            user = userMngr.setFbProfile(user, me);
            log.info("FACEBOOK: original FB user login with fbId - " + user.getEmail() + ";email:" + user.getFbId());
            userMngr.save(user, true);
        }
        
        Object result = prepareUserInfo(ctx, user, null, true);
        this.setUserCookie(resp, CookieHelper.USER, user.getToken());
        if (ctx.isPlainFmt()) {
            String[] value = {(String) result};
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, value);
        }
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    //TODO move to usermanager
    public Object fbWebSignup(ApiContext ctx, String accessToken, String expires, String msoString, HttpServletResponse resp) {
        log.info("msoString:" + msoString);
        FacebookMe me = new FacebookLib().getFbMe(accessToken);
        return fbSignup(ctx, me, expires, msoString, resp);
    }
    
    public Object signup(ApiContext ctx,
                        String email, String password, String name,
                         String token, String captchaFilename,
                         String captchaText, String sphere,
                         String uiLang, String year,
                         String gender,
                         boolean isTemp, HttpServletResponse resp) {
        //validate basic inputs
        int status = NnUserValidator.validate(email, password, name, ctx.getReq());
        if (status != NnStatusCode.SUCCESS)
            return ctx.assemblePlayerMsgs(status);
        if (uiLang == null || sphere == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        
        //convert from guest
        NnGuestManager guestMngr = new NnGuestManager();
        NnGuest guest = guestMngr.findByToken(token);
        if (guest == null && captchaFilename != null) {
            log.info("such guest does not exist, where does this token from");
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        }
        if (guest != null) {
            if (captchaFilename != null || captchaText != null) {
                status = this.checkCaptcha(guest, captchaFilename, captchaText);
                if (status != NnStatusCode.SUCCESS)
                    return ctx.assemblePlayerMsgs(status);
            }
        }
        //Convert email "tdell9x9@gmail.com" to "tdell9x9-AT-gmail.com@9x9.tv",
        //This logic is weak, make something else in the future
        short type = NnUser.TYPE_USER;
        if ((email.contains("-AT-") || email.contains("-at-"))
                && email.contains("@9x9.tv")) {
            type = NnUser.TYPE_YOUTUBE_CONNECT;
        }
        NnUser user = new NnUser(email, password, type, ctx.getMsoId());
        user.setTemp(isTemp);
        NnUserProfile profile = new NnUserProfile(ctx.getMsoId(), name, sphere, uiLang, year);
        if (gender != null)
            profile.setGender(Short.parseShort(gender));
        user.setProfile(profile);
        status = NNF.getUserMngr().create(user, ctx.getReq(), (short)0);
        if (status != NnStatusCode.SUCCESS)
            return ctx.assemblePlayerMsgs(status);
        
        //userMngr.subscibeDefaultChannels(user);
        /*
        String[] result = {(String) prepareUserInfo(user, null, req, false)};
        this.setUserCookie(resp, CookieHelper.USER, user.getToken());
        return this.assembleMsgs(NnStatusCode.SUCCESS, result);
        */
        
        Object result = prepareUserInfo(ctx, user, guest, true);
        this.setUserCookie(resp, CookieHelper.USER, user.getToken());
        if (ctx.isPlainFmt()) {
            String[] value = {(String) result};
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, value);
        }
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
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
    
    public Object guestRegister(ApiContext ctx, HttpServletResponse resp) {
        //verify input
        NnGuestManager mngr = new NnGuestManager();
        NnGuest guest = new NnGuest(NnGuest.TYPE_GUEST);
        mngr.save(guest, ctx.getReq());
        
        Object result = mngr.getPlayerGuestRegister(guest, ctx.getFmt(), ctx.getReq());
        this.setUserCookie(resp, CookieHelper.USER, guest.getToken());
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object userTokenVerify(ApiContext ctx, String token, HttpServletResponse resp) {
        if (token == null) {return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);}
        
        NnUserManager userMngr = NNF.getUserMngr();
        NnGuestManager guestMngr = new NnGuestManager();
        NnUser user = userMngr.findByToken(token, ctx.getMsoId());
        NnGuest guest = guestMngr.findByToken(token);
        if (user == null && guest == null) {
            CookieHelper.deleteCookie(resp, CookieHelper.USER);
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        }
        if (user != null) {
            if (user.getEmail().equals(NnUser.GUEST_EMAIL))
                return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
            if (user.isFbUser()) {
                FacebookLib lib = new FacebookLib();
                FacebookMe me = lib.getFbMe(user.getToken());
                if (me.getStatus() == FacebookMe.STATUS_ERROR) {
                    log.info("FACEBOOK: access token invalid");
                    CookieHelper.deleteCookie(resp, CookieHelper.USER);
                    return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
                } else {
                    user = userMngr.setFbProfile(user, me);
                    log.info("reset fb info:" + user.getId());
                }
            }
            userMngr.save(user, false); //FIXME: change last login time (ie updateTime)
        }
        Object result = prepareUserInfo(ctx, user, guest, true);
        if (ctx.isPlainFmt()) {
            String[] value = {(String) result};
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, value);
        }
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object category(ApiContext ctx, String categoryIdStr, boolean flatten) {
        if (categoryIdStr == null)
            categoryIdStr = "0";
        String[] result = {"", "", ""};
        result[0] = "id" + "\t" + categoryIdStr + "\n";
        
        if (!categoryIdStr.equals("0")) { //v30 compatibilities
            long displayId = Long.parseLong(categoryIdStr);
            SysTagDisplay display = NNF.getDisplayMngr().findById(displayId);
            List<NnChannel> channels = NNF.getSysTagMngr().findPlayerChannelsById(display.getSystagId(), display.getLang(), 0, 200, SysTag.SORT_DATE, ctx.getMsoId());
            result[2] = (String) NNF.getChannelMngr().composeChannelLineup(channels, ctx);
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
        }
        
        MsoConfig mask = NNF.getConfigMngr().findByMsoAndItem(ctx.getMso(), MsoConfig.SYSTEM_CATEGORY_MASK);
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
        categories.addAll(NNF.getDisplayMngr().findPlayerCategories(ctx.getLang(), ctx.getMsoId()));
        
        if (!disableAll && !MsoManager.isSystemMso(ctx.getMso())) {
            List<SysTagDisplay> systemCategories = NNF.getDisplayMngr().findPlayerCategories(ctx.getLang(), MsoManager.getSystemMsoId());
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
            if (ctx.isPlainFmt()) {
                String[] str = {cId,
                                name,
                                String.valueOf(cntChannel),
                                subItemHint};
                result[1] += NnStringUtil.getDelimitedStr(str) + "\n";
            } else {
                Category playerCategory = new Category();
                playerCategory.setId(categoryIdStr);
                playerCategory.setName(name);
                playerCategory.setNextLevel(subItemHint);
                playerCategories.add(playerCategory);
            }
        }
        
        //flatten result process
        if (categoryIdStr.equals("0") && flatten && ctx.isPlainFmt()) {
            log.info("return flatten data");
            List<String> flattenResult = new ArrayList<String>();
            flattenResult.add(result[0]);
            flattenResult.add(result[1]);
            String size[] = new String[flattenResult.size()];
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, flattenResult.toArray(size));
        } else {
            if (ctx.isPlainFmt())
                return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
            else
                return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, playerCategories);
        }
    }
    
    public Object brandInfo(ApiContext ctx) {
        String locale = findLocaleByHttpRequest(ctx.getReq());
        String acceptLang = ctx.getReq().getHeader("Accept-Language");
        Object result = NNF.getMsoMngr().getBrandInfo(ctx, locale, 0, acceptLang);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public String findLocaleByHttpRequest(HttpServletRequest req) {
        
        return NNF.getUserMngr().findLocaleByHttpRequest(req);
    }
    
    public Object pdr(ApiContext ctx, String userToken,
                      String deviceToken, String session,
                      String pdr) {
        if (userToken == null && deviceToken == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        if (pdr == null || pdr.length() == 0)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_ERROR);
        
        //pdr process
        PdrManager pdrMngr = new PdrManager();
        NnUser user = null;
        if (userToken != null) {
            //verify input
            @SuppressWarnings("rawtypes")
            HashMap map = checkUser(ctx.getMsoId(), userToken, false);
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
            return ctx.assemblePlayerMsgs(NnStatusCode.ACCOUNT_INVALID);
        
        String ip = NnNetUtil.getIp(ctx.getReq());
        pdrMngr.processPdr(user, device, session, pdr, ip);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object unsubscribe(ApiContext ctx, String userToken, String channelId, String grid, String pos) {
        //verify input
        if (userToken == null || userToken.equals("undefined"))
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        if (channelId == null && pos == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        //verify user
        NnUser user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
        if (user == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
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
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object subscribe(ApiContext ctx, String userToken, String channelId, String gridId) {
        //verify input
        @SuppressWarnings("rawtypes")
        HashMap map = checkUser(ctx.getMsoId(), userToken, false);
        if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
            return ctx.assemblePlayerMsgs((Integer)map.get("s"));
        }
        if (channelId == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        NnUser user = (NnUser) map.get("u");
        NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
        //verify channel and grid
        if (channelId.contains("f-")) {
            String profileUrl = channelId.replaceFirst("f-", "");
            NnUser curator = NNF.getUserMngr().findByProfileUrl(profileUrl, ctx.getMsoId());
            if (curator == null)
                return ctx.assemblePlayerMsgs(NnStatusCode.CHANNEL_ERROR);
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
            return ctx.assemblePlayerMsgs(NnStatusCode.CHANNEL_ERROR);
        
        short seq = 0;
        if (gridId == null) {
            seq = subMngr.findFirstAvailableSpot(user);
            if (seq == 0)
                return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        } else {
            seq = Short.parseShort(gridId);
        }
        if (seq > 72) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        }
        log.info("input token:" + userToken + ";user token:" + user.getToken() +
                ";userId:" + user.getId() + ";user type:" + user.getType() + ";seq:" + seq);
        //if (user.getType() != NnUser.TYPE_YOUTUBE_CONNECT) {
        if (seq != 0) {
            NnUserSubscribe s = subMngr.findByUserAndSeq(user, seq);
            if (s != null)
                return ctx.assemblePlayerMsgs(NnStatusCode.SUBSCRIPTION_POS_OCCUPIED);
        }
        NnUserSubscribe s = subMngr.findByUserAndChannel(user, String.valueOf(channel.getId()));
        if (s != null)
            return ctx.assemblePlayerMsgs(NnStatusCode.SUBSCRIPTION_DUPLICATE_CHANNEL);
        s = subMngr.subscribeChannel(user, channel.getId(), seq, MsoIpg.TYPE_GENERAL);
        String result[] = {""};
        result[0] = s.getSeq() + "\t" + s.getChannelId();
        
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object categoryInfo(ApiContext ctx, String displayIdStr, String tagStr, String start, String count, String sort, boolean programInfo) {
        if (displayIdStr == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        long displyId = Long.parseLong(displayIdStr);
        SysTagDisplay display = NNF.getDisplayMngr().findById(displyId);
        if (display == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.CATEGORY_INVALID);
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
                    Integer.parseInt(String.valueOf(limit)), SysTag.SORT_DATE, ctx.getMsoId());
        }
        long longTotal = NNF.getSysTagMngr().findPlayerChannelsCountById(display.getSystagId(), display.getLang(), ctx.getMsoId());
        Object result = NNF.getDisplayMngr().getPlayerCategoryInfo(display, programInfo, channels, Long.valueOf(start), limit, longTotal, ctx);
        
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object channelStack(ApiContext ctx, String stack, String sphere, String userToken, String channel, boolean isReduced) {
        if (stack == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        List<String> result = new ArrayList<String>();
        String[] cond = stack.split(",");
        for (String s : cond) {
            List<NnChannel> chs = new ArrayList<NnChannel>();
            if (s.equals(Tag.RECOMMEND)) {
                Object obj = new YtChannelManager().findRecommend(userToken, sphere, ctx.getVer(), ctx.getFmt());
                return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, obj);
            } else if (s.equals("mayLike")) {
                chs = new RecommendService().findMayLike(userToken, ctx.getMsoId(), channel, sphere);
            } else {
                SysTagDisplay set = null;
                if (!Pattern.matches("^\\d*$", stack)) {
                    log.info("channelStack findbyname:" + stack);
                    set = NNF.getDisplayMngr().findByName(stack, ctx.getMsoId());
                } else {
                    log.info("channelStack findbyid:" + stack);
                    set = NNF.getDisplayMngr().findById(Long.parseLong(stack));
                }
                if (set != null)
                    chs = NNF.getSysTagMngr().findPlayerAllChannelsById(set.getSystagId(), set.getLang(), SysTag.SORT_DATE, 0);
            }
            
            String output = "";
            if (isReduced) {
                output = (String) NNF.getChannelMngr().composeReducedChannelLineup(chs, ApiContext.FORMAT_PLAIN);
            } else {
                output = (String) NNF.getChannelMngr().composeChannelLineup(chs, ctx);
            }
            result.add(output);
        }
        String size[] = new String[result.size()];
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result.toArray(size));
    }
    
    public Object subscriberLineup(ApiContext ctx, String userToken, String curatorIdStr) {
        NnUser curator = NNF.getUserMngr().findByProfileUrl(curatorIdStr, ctx.getMsoId());
        if (curator == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
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
    public Object channelLineup(ApiContext ctx,
                                String userToken,
                                String curatorIdStr,
                                String subscriptions,
                                boolean userInfo,
                                String channelIds,
                                boolean setInfo,
                                boolean isRequired,
                                boolean isReduced,
                                boolean programInfo,
                                String sort) {
        //verify input
        if (((userToken == null && userInfo == true) ||
            (userToken == null && channelIds == null) ||
            (userToken == null && setInfo == true))) {
            if (curatorIdStr == null && subscriptions == null)
                return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        List<String> result = new ArrayList<String>();
        NnUser user = null;
        NnGuest guest = null;
        if (userToken != null || subscriptions != null) {
            if (subscriptions != null) {
                user = NNF.getUserMngr().findByProfileUrl(subscriptions, ctx.getMsoId());
            } else {
                user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
            }
            if (user == null) {
                guest = new NnGuestManager().findByToken(userToken);
                if (guest == null)
                    return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
            }
        }
        PlayerChannelLineup playerChannelLineup = new PlayerChannelLineup();
        //user info
        if (userInfo) {
            log.info("userInfo is added");
            if (ctx.isPlainFmt())
                result.add((String) prepareUserInfo(ctx, user, guest, true));
            else
                playerChannelLineup.setUserInfo(((UserInfo) prepareUserInfo(ctx, user, guest, true)));
        }
        NnUserSubscribeGroupManager groupMngr = new NnUserSubscribeGroupManager();
        if (curatorIdStr == null) {
            //set info
            if (setInfo) {
                String setOutput = "";
                List<NnUserSubscribeGroup> groups = new ArrayList<NnUserSubscribeGroup>();
                String[] list = new String[9];
                if (user != null) {
                    groups.addAll(groupMngr.findByUser(user));
                    if (ctx.isPlainFmt()) {
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
                List<MsoIpg> ipgs = new MsoIpgManager().findSetsByMso(ctx.getMsoId());
                for (MsoIpg i : ipgs) {
                    String[] obj = {
                            String.valueOf(i.getSeq()),
                            String.valueOf(0),
                            i.getGroupName(),
                            "",
                            String.valueOf(i.getType()),
                    };
                    list[i.getSeq() - 1] = NnStringUtil.getDelimitedStr(obj);
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
            List<NnChannel> defaultChannels = NNF.getChannelMngr().findMsoDefaultChannels(ctx.getMsoId(), false);
            channels.addAll(defaultChannels);
            log.info("user: " + userToken + " find subscribed size:" + channels.size());
        } else if (curatorIdStr != null) {
            channelPos = false;
            NnUser curator = NNF.getUserMngr().findByProfileUrl(curatorIdStr, ctx.getMsoId());
            if (curator == null)
                return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
            List<NnChannel> curatorChannels = new ArrayList<NnChannel>();
            if (curator.getToken().equals(userToken)) {
                log.info("find channels curator himself created");
                curatorChannels = NNF.getChannelMngr().findByUserAndHisFavorite(curator, 0, true);
            } else {
                log.info("find channels curator created for public");
                curatorChannels = NNF.getChannelMngr().findByUserAndHisFavorite(curator, 0, false);
            }
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
            return ctx.assemblePlayerMsgs(NnStatusCode.CHANNEL_INVALID);
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
        if (ctx.isJsonFmt()) {
            @SuppressWarnings("unchecked")
            List<ChannelLineup> lineup = (List<ChannelLineup>) NNF.getChannelMngr().getPlayerChannelLineup(channels, channelPos, programInfo, isReduced, ctx, null);
            playerChannelLineup.setChannelLineup(lineup);
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, playerChannelLineup);
        }
        Object channelLineup = NNF.getChannelMngr().getPlayerChannelLineup(channels, channelPos, programInfo, isReduced, ctx, result);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, channelLineup);
    }
    
    public Object channelSubmit(ApiContext ctx,
                                String categoryIds,
                                String userToken, String url,
                                String grid, String name,
                                String image, String tags) {
        //verify input
        if (url == null || url.length() == 0 ||  grid == null || grid.length() == 0 ||
             userToken== null || userToken.length() == 0) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        if (Integer.parseInt(grid) < 0 || Integer.parseInt(grid) > 81) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        }
        url = url.trim();
        //verify user
        NnUser user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
        if (user == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        if (user.getEmail().equals(NnUser.GUEST_EMAIL))
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_PERMISSION_ERROR);
        
        //verify url, also converge youtube url
        url = NNF.getChannelMngr().verifyUrl(url);
        if (url == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.CHANNEL_URL_INVALID);
        
        //verify channel status for existing channel
        NnChannel channel = NNF.getChannelMngr().findBySourceUrl(url);
        if (channel != null && (channel.getStatus() == NnChannel.STATUS_ERROR)) {
            log.info("channel id and status :" + channel.getId()+ ";" + channel.getStatus());
            ctx.assemblePlayerMsgs(NnStatusCode.CHANNEL_STATUS_ERROR);
        }
        
        //create a new channel
        if (channel == null) {
            if (name != null && image != null) {
                channel = NNF.getChannelMngr().createYouTubeWithMeta(url, name, null, ctx.getLang(), image, ctx.getReq());
            } else {
                channel = NNF.getChannelMngr().create(url, null, ctx.getLang(), ctx.getReq());
            }
            if (channel == null) {
                return ctx.assemblePlayerMsgs(NnStatusCode.CHANNEL_URL_INVALID);
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
                return ctx.assemblePlayerMsgs(NnStatusCode.SUBSCRIPTION_POS_OCCUPIED);
        }
        NnUserSubscribe s = subMngr.findByUserAndChannel(user, String.valueOf(channel.getId()));
        if (s != null)
            return ctx.assemblePlayerMsgs(NnStatusCode.SUBSCRIPTION_DUPLICATE_CHANNEL);
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
        
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    //TODO if use if fb user, redirect to facebook
    public Object login(ApiContext ctx, String email, String password, HttpServletResponse resp) {
        log.info("login player api service:" + ctx.getMsoId());
        log.info("login: email=" + email + ";password=" + password);
        if (!BasicValidator.validateRequired(new String[] {email, password}))
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);;
        
        NnUser user = NNF.getUserMngr().findAuthenticatedUser(email, password, ctx.getMsoId(), ctx.getReq());
        if (user != null) {
            Object result = prepareUserInfo(ctx, user, null, true);
            NNF.getUserMngr().save(user, false); //change last login time (ie updateTime)
            this.setUserCookie(resp, CookieHelper.USER, user.getToken());
            if (ctx.isPlainFmt()) {
                String[] raw = {(String) result};
                return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, raw);
            }
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
        } else {
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_LOGIN_FAILED);
        }
    }
    
    public Object setProgramProperty(ApiContext ctx, String program, String property, String value) {
        if (program == null || property == null || value == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        NnProgram p = NNF.getProgramMngr().findById(Long.parseLong(program));
        if (p == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.PROGRAM_INVALID);
        if (property.equals("duration")) {
            p.setDuration(value);
            NNF.getProgramMngr().save(p);
        } else {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        }
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object setChannelProperty(ApiContext ctx, String channel, String property, String value) {
        if (channel == null || property == null || value == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        NnChannel c = NNF.getChannelDao().findById(Long.parseLong(channel));
        if (c == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.CHANNEL_INVALID);
        if (property.equals("count")) {
            c.setCntEpisode(Integer.valueOf(value));
        } else if (property.equals("updateDate")) {
            long epoch = Long.parseLong(value);
            Date date = new Date (epoch*1000);
            log.info("Date:" + date);
            c.setUpdateDate(date);
        } else {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        }
        NNF.getChannelDao().save(c);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    //TODO real range search
    public Object tagInfo(ApiContext ctx, String name, String start, String count) {
        if (name == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        String[] result = {"", ""};
        TagManager tagMngr = new TagManager();
        Tag tag = tagMngr.findByName(name);
        if (tag == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.TAG_INVALID);
        
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
        result[1] += NNF.getChannelMngr().composeChannelLineup(channels, ctx);
        
        result[0] += assembleKeyValue("id", String.valueOf(tag.getId()));
        result[0] += assembleKeyValue("name", tag.getName());
        result[0] += assembleKeyValue("start", start);
        result[0] += assembleKeyValue("count", count);
        result[0] += assembleKeyValue("total", String.valueOf(total));
        
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    @SuppressWarnings("unchecked")
    public Object programInfo(ApiContext ctx, String channelIds,
                                  String episodeIdStr, String userToken,
                                  String ipgId, boolean userInfo, String sidx,
                                  String limit, String start,
                                  String count, String time) throws NotPurchasedException {
        
        if (channelIds == null || (channelIds.equals("*") && userToken == null && ipgId == null)) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        if (start != null && Integer.valueOf(start) > 200) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
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
            user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
            if (user == null) {
                NnGuest guest = new NnGuestManager().findByToken(userToken);
                if (guest == null)
                    return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
                else
                    return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
            }
        } else if (chArr.length > 1) {
            
            List<Long> list = new ArrayList<Long>();
            for (int i = 0; i < chArr.length; i++) { list.add(Long.valueOf(chArr[i])); }
            for (Long l : list) {
                if (ctx.getVer() < 32) {
                    programInfoStr = new IosService().findPlayerProgramInfoByChannel(l, startI, end);
                } else {
                    if (ctx.isPlainFmt()) {
                        programInfoStr += (String) NNF.getProgramMngr().findPlayerProgramInfoByChannel(l, startI, end, shortTime, userToken, ctx);
                        if (pagination) {
                            NnChannel c = NNF.getChannelMngr().findById(l);
                            if (c != null)
                                paginationStr += assembleKeyValue(c.getIdStr(), String.valueOf(countI) + "\t" + String.valueOf(c.getCntEpisode()));
                        }
                    } else {
                        programInfoJson = (List<ProgramInfo>) NNF.getProgramMngr().findPlayerProgramInfoByChannel(l, startI, end, shortTime, userToken, ctx);
                    }
                }
            }
        } else if (orphanEpisode != null) {
            
            long cId = Long.parseLong(channelIds);
            NnChannel channel = NNF.getChannelMngr().findById(cId);
            if (ctx.isPlainFmt()) {
                
                programInfoStr = (String) NNF.getProgramMngr().findPlayerProgramInfoByEpisode(orphanEpisode, channel, ctx.getFmt());
                if (pagination && channel != null) {
                    paginationStr += assembleKeyValue(channel.getIdStr(), "1\t1");
                }
            } else {
                
                programInfoJson = (List<ProgramInfo>) NNF.getProgramMngr().findPlayerProgramInfoByEpisode(orphanEpisode, channel, ctx.getFmt());
                playerProgramInfo.setProgramInfo(programInfoJson);
            }
        } else {
            if (ctx.getVer() < 32) {
                programInfoStr = new IosService().findPlayerProgramInfoByChannel(Long.parseLong(channelIds), startI, end);
                if (programInfoStr != null && ctx.isIos()) {
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
                if (ctx.isPlainFmt()) {
                    long cId = Long.parseLong(channelIds);
                    programInfoStr = (String) NNF.getProgramMngr().findPlayerProgramInfoByChannel(cId, startI, end, shortTime, userToken, ctx);
                    if (pagination) {
                        NnChannel c = NNF.getChannelMngr().findById(cId);
                        if (c != null)
                            paginationStr += assembleKeyValue(c.getIdStr(), String.valueOf(countI) + "\t" + String.valueOf(c.getCntEpisode()));
                    }
                } else {
                    programInfoJson = (List<ProgramInfo>) NNF.getProgramMngr().findPlayerProgramInfoByChannel(Long.parseLong(channelIds), startI, end, shortTime, userToken, ctx);
                    playerProgramInfo.setProgramInfo(programInfoJson);
                }
            }
        }
        
        String userInfoStr = "";
        List<String> result = new ArrayList<String>();
        if (userInfo) {
            if (user == null && userToken != null)  {
                user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
                if (ctx.isPlainFmt()) {
                    userInfoStr = (String) prepareUserInfo(ctx, user, null, true);
                    result.add(userInfoStr);
                } else {
                    playerProgramInfo.setUserInfo((UserInfo) prepareUserInfo(ctx, user, null, true));
                }
            }
        }
        if (pagination && ctx.isPlainFmt())
            result.add(paginationStr);
            
        if (ctx.isJsonFmt()) {
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, playerProgramInfo);
        } else {
            result.add(programInfoStr);
            String size[] = new String[result.size()];
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result.toArray(size));
        }
    }
    
    public Object moveChannel(ApiContext ctx, String userToken, String grid1, String grid2) {
        //verify input
        if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") || grid1 == null || grid2 == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        if (!Pattern.matches("^\\d*$", grid1) || !Pattern.matches("^\\d*$", grid2) ||
            Integer.parseInt(grid1) < 0 || Integer.parseInt(grid1) > 81 ||
            Integer.parseInt(grid2) < 0 || Integer.parseInt(grid2) > 81) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        }
        NnUser user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
        if (user == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        
        NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
        boolean success = subMngr.moveSeq(user, Short.parseShort(grid1), Short.parseShort(grid2));
        
        if (!success) { return ctx.assemblePlayerMsgs(NnStatusCode.SUBSCRIPTION_ERROR); }
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object setSetInfo(ApiContext ctx, String userToken, String name, String pos) {
        //verify input
        if (name == null || pos == null)  {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        if (!Pattern.matches("^\\d*$", pos) || Integer.parseInt(pos) < 0 || Integer.parseInt(pos) > 9) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_ERROR);
        }
        
        NnUser user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
        if (user == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        
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
        
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object staticContent(ApiContext ctx, String key) {
        NnContentManager contentMngr = new NnContentManager();
        NnContent content = contentMngr.findByItemAndLang(key, ctx.getLang(), ctx.getMsoId());
        if (content == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        String[] result = { content.getValue() };
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object deviceRegister(ApiContext ctx, String userToken, String type, HttpServletResponse resp) {
        
        NnDevice device = null;
        NnUser user = null;
        if (userToken != null) {
            @SuppressWarnings({ "rawtypes"})
            HashMap map = checkUser(ctx.getMsoId(), userToken, false);
            if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
                return ctx.assemblePlayerMsgs((Integer)map.get("s"));
            }
            user = (NnUser) map.get("u");
        }
        
        if (type == null) {
        } else if (type.equalsIgnoreCase(NnDevice.TYPE_APNS) || type.equalsIgnoreCase(NnDevice.TYPE_GCM)) {
            
            String token = ctx.getParam("token");
            if (token == null) {
                return ctx.assemblePlayerMsgs(NnStatusCode.ERROR, "missing param token");
            }
            
            device = NNF.getDeviceMngr().findDuplicated(token, ctx.getMsoId(), type);
            if (device == null) {
                device = new NnDevice(token, ctx.getMsoId(), type);
            }
            device.setBadge(0);
        }
        NnDeviceManager deviceMngr = new NnDeviceManager();
        deviceMngr.setReq(ctx.getReq()); // FIXME !!!
        device = deviceMngr.create(device, user, type);
        
        if (type == null || type.equalsIgnoreCase(NnDevice.TYPE_FLIPR)) {
            setUserCookie(resp, CookieHelper.DEVICE, device.getToken());
        }
        Object result = deviceMngr.getPlayerDeviceInfo(device, ctx.getFmt(), null);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object deviceTokenVerify(ApiContext ctx, String token) {
        if (token == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
        NnDeviceManager deviceMngr = new NnDeviceManager();
        deviceMngr.setReq(ctx.getReq()); // FIXME !!!
        List<NnDevice> devices = deviceMngr.findByToken(token);
        if (devices.size() == 0)
            return ctx.assemblePlayerMsgs(NnStatusCode.DEVICE_INVALID);
        List<NnUser> users = new ArrayList<NnUser>();
        log.info("device size" + devices.size());
        for (NnDevice d : devices) {
            if (d.getUserId() != 0) {
                NnUser user = NNF.getUserMngr().findById(d.getUserId(), (short)1);
                if (user != null && user.getMsoId() == ctx.getMsoId())
                    users.add(user);
                else
                    log.info("bad data in device:" + d.getToken() + ";userId:" + d.getUserId());
            }
        }
        String[] result = {""};
        for (NnUser u : users) {
            result[0] += u.getToken() + "\t" + u.getProfile().getName() + "\t" + u.getUserEmail() + "\n";
        }
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object deviceAddUser(ApiContext ctx, String deviceToken, String userToken) {
        NnUser user = null;
        if (userToken != null) {
            @SuppressWarnings("rawtypes")
            HashMap map = checkUser(ctx.getMsoId(), userToken, false);
            if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
                return ctx.assemblePlayerMsgs((Integer)map.get("s"));
            }
            user = (NnUser) map.get("u");
        }
        if (deviceToken == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        NnDevice device = NNF.getDeviceMngr().addUser(deviceToken, user);
        if (device == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.DEVICE_INVALID);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object deviceRemoveUser(ApiContext ctx, String deviceToken, String userToken) {
        NnUser user = null;
        if (userToken != null) {
            @SuppressWarnings("rawtypes")
            HashMap map = checkUser(ctx.getMsoId(), userToken, false);
            if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
                return ctx.assemblePlayerMsgs((Integer)map.get("s"));
            }
            user = (NnUser) map.get("u");
        }
        if (deviceToken == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        boolean success = NNF.getDeviceMngr().removeUser(deviceToken, user);
        if (!success)
            return ctx.assemblePlayerMsgs(NnStatusCode.DEVICE_INVALID);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private HashMap checkUser(long msoId, String userToken, boolean guestOK) {
        HashMap map = new HashMap();
        //verify input
        if (userToken == null || userToken.length() == 0 || userToken.equals("undefined"))
            map.put("s", NnStatusCode.INPUT_MISSING);
        if (guestOK) {
            map.put("s", NnStatusCode.SUCCESS);
            return map;
        }
        //verify user
        NnUser user = NNF.getUserMngr().findByToken(userToken, msoId);
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
        if (NnDateUtil.now().after(guest.getExpiredAt()))
            return NnStatusCode.CAPTCHA_FAILED;
        return NnStatusCode.SUCCESS;
    }
    
    public Object userReport(ApiContext ctx, String userToken,
            String deviceToken, String session, String type, String item,
            String comment) {
        if (comment == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        if (comment.length() > 500)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        
        NnUser user = null;
        if (userToken != null) {
            //verify input
            @SuppressWarnings("rawtypes")
            HashMap map = checkUser(ctx.getMsoId(), userToken, false);
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
            user = NNF.getUserMngr().findByEmail(NnUser.ANONYMOUS_EMAIL, 1, ctx.getReq());
        }
        String content = "";
        String from = "";
        if (item != null) {
            String[] key = item.split(",");
            String[] value = comment.split(",");
            String description = "";
            if (key.length != value.length)
                return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_ERROR);
            for (int i=0; i<key.length; i++) {
            	if (!key[i].equals("description"))
                    content += key[i] + ":" + value[i] + "\n";
            	else
                    description = value[i];
            	if (key[i].equals("email"))
            		from = key[i];
            }            
            //description needs to be put at the end
            if (description.length() > 0)
        		content += "description:" + description + "\n";
        } else {
            content = comment; //backward compatibility
        }
        if (from.length() == 0)
        	from = user.getEmail();
        NnUserReportManager reportMngr = new NnUserReportManager();
        String[] result = {""};
        NnUserReport report = reportMngr.save(user, device, session, type, item, content);
        if (report != null && user != null) {
            result[0] = PlayerApiService.assembleKeyValue("id", String.valueOf(report.getId()));
            EmailService service = new EmailService();
            String toEmail = "feedback@9x9.tv";
            String toName = "feedback";
            //String subject = "User send a report";
            NnUserProfile profile = user.getProfile();
            String body = "user ui-lang:" + profile.getLang() + "\n";
            String region = "TW";
            if (profile.getSphere() != null && profile.getSphere().equals("zh"))
            	region = "US";
            body += "user region:" + region + "\n";
            body += "user brand:" + ctx.getMsoName() + "\n\n";
            body += content;
            try {
                body = URLDecoder.decode(body, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            
            String subject = "[" + ctx.getMsoName() + "]";
            subject += (type != null) ? (" - [" + type + "]") : "" ;
            SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yyyy");
        	String date = sdf.format(NnDateUtil.now()); 
            subject += " (" + date + ")";
            log.info("subject:" + subject);
            log.info("content:" + body);
            NnEmail mail = new NnEmail(toEmail, toName,
                                       from, profile.getName(),
                                       from, subject, body);
            service.sendEmail(mail, "userfeedback@9x9.tv", "userfeedback");
        }
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object setUserProfile(ApiContext ctx, String userToken, String items, String values) {
        
        NnUserProfileManager profileMngr = NNF.getProfileMngr();
        
        //verify input
        if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") ||
            items == null || values == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        //verify user
        NnUser user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
        if (user == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        if (user.getUserEmail().equals(NnUser.GUEST_EMAIL))
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_PERMISSION_ERROR);
        NnUserProfile profile = profileMngr.findByUser(user);
        if (profile == null) profile = new NnUserProfile(user.getId(), ctx.getMsoId());
        String[] key = items.split(",");
        String[] value = values.split(",");
        String password = "";
        String oldPassword = "";
        if (key.length != value.length) {
            log.info("key and value length mismatches!");
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_ERROR);
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
                return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_ERROR);
            }
           if (user.isFbUser()) {
               if (!key[i].equals("sphere") && !key[i].equals("ui-lang")) {
                   log.info("fbuser: not permitted key:" + key);
                   return ctx.assemblePlayerMsgs(NnStatusCode.USER_PERMISSION_ERROR);
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
                    return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_ERROR);
                profile.setName(theValue);
            }
            if (key[i].equals("phone")) {
                if (!Pattern.matches("^\\d*$", theValue))
                    return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
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
            if (key[i].equals("sphere"))
                profile.setSphere(theValue);
            if (key[i].equals("gender"))
                profile.setGender(Short.parseShort(theValue));
            if (key[i].equals("ui-lang"))
                profile.setLang(theValue);
        }
        int status = NnUserValidator.validateProfile(user);
        if (status != NnStatusCode.SUCCESS) {
            log.info("profile fail");
            return ctx.assemblePlayerMsgs(status);
        }
        if (password.length() > 0 && oldPassword.length() > 0) {
            log.info("password:" + password + ";oldPassword:" + oldPassword);
            NnUser authenticated = NNF.getUserMngr().findAuthenticatedUser(user.getEmail(), oldPassword, ctx.getMsoId(), ctx.getReq());
            if (authenticated == null)
                return ctx.assemblePlayerMsgs(NnStatusCode.USER_LOGIN_FAILED);
            status = NnUserValidator.validatePassword(password);
            if (status != NnStatusCode.SUCCESS)
                return ctx.assemblePlayerMsgs(status);
            user.setPassword(password);
            user.setSalt(AuthLib.generateSalt());
            user.setCryptedPassword(AuthLib.encryptPassword(user.getPassword(), user.getSalt()));
            NNF.getUserMngr().save(user, true);
        }
        profileMngr.save(user, profile);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object getUserProfile(ApiContext ctx, String userToken) {
        //verify input
        if (userToken == null || userToken.length() == 0 || userToken.equals("undefined"))
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        //verify user
        NnUser user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
        if (user == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        Object result = NNF.getProfileMngr().getPlayerProfile(user, ctx.getFmt());
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object shareByEmail(ApiContext ctx,
            String userToken, String toEmail,
            String toName, String subject,
            String content, String captcha, String text) {
        @SuppressWarnings("rawtypes")
        HashMap map = checkUser(ctx.getMsoId(), userToken, false);
        if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
            return ctx.assemblePlayerMsgs((Integer)map.get("s"));
        }
        boolean isIos = ctx.isIos();
        if (!isIos) {
            if (captcha == null || text == null)
                return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        if (toEmail == null || content == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        NnUser user = (NnUser) map.get("u");
        if (captcha != null) {
            NnGuestManager guestMngr = new NnGuestManager();
            NnGuest guest = guestMngr.findByToken(userToken);
            int status = this.checkCaptcha(guest, captcha, text);
            if (status != NnStatusCode.SUCCESS)
                return ctx.assemblePlayerMsgs(status);
            guestMngr.delete(guest);
        }
        NnEmail mail = new NnEmail(toEmail, toName, NnEmail.SEND_EMAIL_SHARE, user.getProfile().getName(), user.getUserEmail(), subject, content);
        NNF.getEmailService().sendEmail(mail, null, null);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object setUserPref(ApiContext ctx, String token, String item, String value) {
        //verify input
        if (token == null || token.length() == 0 || token.equals("undefined") ||
            item == null || value == null || item.length() == 0 || value.length() == 0) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        //verify user
        NnUser user = NNF.getUserMngr().findByToken(token, ctx.getMsoId());
        if (user == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        //get preference
        NnUserPref pref = NNF.getPrefMngr().findByUserAndItem(user, item);
        if (pref != null) {
            pref.setValue(value);
            NNF.getPrefMngr().save(user, pref);
        } else {
            pref = new NnUserPref(user, item, value);
            NNF.getPrefMngr().save(user, pref);
        }
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object requestCaptcha(ApiContext ctx, String token, String action) {
        if (token == null || action == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        CaptchaManager mngr = new CaptchaManager();
        Captcha c = mngr.getRandom();
        if (c == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.CAPTCHA_ERROR);
        short a = Short.valueOf(action);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 5);
        NnGuestManager guestMngr = new NnGuestManager();
        NnGuest guest = guestMngr.findByToken(token);
        if (a == Captcha.ACTION_SIGNUP) {
            if (guest == null)
                return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        } else if (a == Captcha.ACTION_EMAIL) {
            if (guest == null) {
                guest = new NnGuest(NnGuest.TYPE_USER);
                guest.setToken(token);
            }
        }
        guest.setCaptchaId(c.getId());
        guest.setExpiredAt(cal.getTime());
        guest.setGuessTimes(0);
        guestMngr.save(guest, ctx.getReq());
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, new String[] {c.getFileName()});
    }
    
    public Object saveSorting(ApiContext ctx, String token, String channelId, String sort) {
        @SuppressWarnings("rawtypes")
        HashMap map = checkUser(ctx.getMsoId(), token, false);
        if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
            return ctx.assemblePlayerMsgs((Integer)map.get("s"));
        }
        NnUser user = (NnUser) map.get("u");
        NnUserChannelSorting sorting = new NnUserChannelSorting(user.getId(),
                                           Long.parseLong(channelId), Short.parseShort(sort));
        NnUserChannelSortingManager sortingMngr = new NnUserChannelSortingManager();
        sortingMngr.save(user, sorting);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object personalHistory(ApiContext ctx, String userToken) {
        @SuppressWarnings("rawtypes")
        HashMap map = checkUser(ctx.getMsoId(), userToken, false);
        if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
            return ctx.assemblePlayerMsgs((Integer)map.get("s"));
        }
        NnUser user = (NnUser) map.get("u");
        List<NnChannel> channels = NNF.getChannelMngr().findPersonalHistory(user.getId(), user.getMsoId());
        String result[] = {(String) NNF.getChannelMngr().composeChannelLineup(channels, ctx)};
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object userWatched(ApiContext ctx, String userToken, String count, boolean channelInfo, boolean episodeIndex, String channel) {
        /*
        @SuppressWarnings("rawtypes")
        HashMap map = checkUser(userToken, false);
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
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object copyChannel(ApiContext ctx, String userToken, String channelId, String grid) {
        //verify input
        if (userToken == null || userToken.length() == 0 || userToken.equals("undefined") || grid == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        if (!Pattern.matches("^\\d*$", grid) ||
            Integer.parseInt(grid) < 0 || Integer.parseInt(grid) > 81)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_ERROR);
        NnUser user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
        if (user == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        
        NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
        boolean success = false;
        success = subMngr.copyChannel(user, Long.parseLong(channelId), Short.parseShort(grid));
        if (!success)
            return ctx.assemblePlayerMsgs(NnStatusCode.SUBSCRIPTION_ERROR);
        else
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object forgotpwd(ApiContext ctx, String email) {
        NnUser user = NNF.getUserMngr().findByEmail(email, ctx.getMsoId(), ctx.getReq());
        if (user == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        }
        if (user.isFbUser())
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_PERMISSION_ERROR);
        
        //String link = NnNetUtil.getUrlRoot(req) + "/#!resetpwd!e=" + email + "!pass=" + userMngr.forgotPwdToken(user);
        //signin.html?ac=resetpwd&e=marshsu.9x9@gmail.com&pass=b38ea3c1e56135827a6e4343d6ac4ea3
        String link = NnNetUtil.getUrlRoot(ctx.getReq()).replaceAll("^http:\\/\\/", "https://");
        link += "/cms/signin.html?ac=resetpwd&e=" + email + "&pass=" + NNF.getUserMngr().forgotPwdToken(user);
        log.info("link:" + link);
        
        NnContentManager contentMngr = new NnContentManager();
        NnUserProfile profile = user.getProfile();
        String lang = profile.getLang();
        log.info("user language:" + lang);
        NnContent content = contentMngr.findByItemAndLang("resetpwd", lang, ctx.getMsoId());
        String subject = "Forgotten Password";
        if (lang.equalsIgnoreCase(LocaleTable.LANG_ZH))
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
                NnEmail.REPLY_EMAIL_NOREPLY, "noreply", NnEmail.REPLY_EMAIL_NOREPLY,
                subject, body);
        
        mail.setHtml(true);
        service.sendEmail(mail, null, null);
        
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object resetpwd(ApiContext ctx, String email, String token, String password) {
        if (email == null || token == null || password == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        NnUserManager userMngr = NNF.getUserMngr();
        NnUser user = userMngr.findByEmail(email, ctx.getMsoId(), ctx.getReq());
        if (user == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        }
        String forgotPwdToken = userMngr.forgotPwdToken(user);
        log.info("forgotPwdToken:" + forgotPwdToken + ";token user passes:" + token);
        if (forgotPwdToken.equals(token)) {
            int status = NnUserValidator.validatePassword(password);
            if (status != NnStatusCode.SUCCESS)
                return ctx.assemblePlayerMsgs(status);
            user.setPassword(password);
            userMngr.resetPassword(user);
            userMngr.save(user, true);
            log.info("reset password success:" + user.getEmail());
        } else {
            log.info("reset password token mismatch");
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_PERMISSION_ERROR);
        }
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    @SuppressWarnings("unchecked")
    public Object search(ApiContext ctx, String text, String stack, String type, String start, String count) {
        if (text == null || text.length() == 0)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        if (ctx.getVer() < 32) {
            return new IosService().search(ctx, text);
        }
        
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
        
        List<NnChannel> channels = new ArrayList<NnChannel>();
        long numOfChannelTotal = 0;
        if (text.startsWith("@")) {
            String cid = text.substring(1);
            NnChannel c = NNF.getChannelMngr().findById(Long.parseLong(cid));
            numOfChannelTotal = 1;
            channels.add(c);
        } else {
        @SuppressWarnings("rawtypes")
        Stack st = NnChannelManager.searchSolr(SearchLib.CORE_NNCLOUDTV, text, searchContent, null, false, startIndex, limit);
        channels = (List<NnChannel>) st.pop();
        System.out.println("solr search channel size:" + channels.size());
        numOfChannelTotal = (Long) st.pop();
        }
        
        List<NnUser> users = NNF.getUserMngr().search(null, null, text, ctx.getMsoId());
        int endIndex = (users.size() > 9) ? 9: users.size();
        users = users.subList(0, endIndex);
        //if none matched, return suggestion channels
        List<NnChannel> suggestion = new ArrayList<NnChannel>();
        if (channels.size() == 0 && users.size() == 0) {
            suggestion = NNF.getChannelMngr().findBillboard(Tag.TRENDING, LocaleTable.LANG_EN);
        }
        int numOfChannelReturned = channels.size();
        int numOfCuratorReturned = users.size();
        int numOfSuggestReturned = suggestion.size();
        int numOfCuratorTotal = users.size();
        int numOfSuggestTotal = suggestion.size();
        
        if (ctx.isPlainFmt()) {
            String[] result = {"", "", "", ""}; //count, curator, curator's channels, channels, suggestion channels
            //matched curators && their channels [important, two sections]
            result[1] = (String) NNF.getUserMngr().composeCuratorInfo(ctx, users, true, false);
            //matched channels
            result[2] = (String) NNF.getChannelMngr().composeChannelLineup(channels, ctx);
            System.out.println("result 3:" + result[3]);
            //suggested channels
            if (channels.size() == 0 && users.size() == 0) {
                result[3] = (String) NNF.getChannelMngr().composeChannelLineup(suggestion, ctx);
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
            
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
        } else {
            Search search = new Search();
            search.setChannelLineups((List<ChannelLineup>) NNF.getChannelMngr().composeChannelLineup(channels,  ctx));
            search.setNumOfChannelReturned(numOfChannelReturned);
            search.setNumOfCuratorReturned(numOfCuratorReturned);
            search.setNumOfSuggestReturned(numOfSuggestReturned);
            search.setNumOfChannelTotal((int)numOfChannelTotal);
            search.setNumOfCuratorTotal(numOfCuratorTotal);
            search.setNumOfSuggestTotal(numOfSuggestTotal);
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, search);
        }
        
    }
    
    public Object programRemove(ApiContext ctx, String programId, String ytVideoId, String userToken, String secret, String status) {
        if (userToken == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        if (programId == null && ytVideoId == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        if (ytVideoId != null && ytVideoId.length() < 11) {
            log.info("invalid youtube id:" + ytVideoId);
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
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
                    return ctx.assemblePlayerMsgs(NnStatusCode.PROGRAM_INVALID);
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
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object channelCreate(ApiContext ctx, String token, String name, String intro, String imageUrl, boolean isTemp) {
        //verify input
        if (token == null || token.length() == 0 || name == null || name.length() == 0 ||  imageUrl == null || imageUrl.length() == 0) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        if (ctx.isJsonFmt()) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        }
        NnUser user = NNF.getUserMngr().findByToken(token, ctx.getMsoId());
        if (user == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        NnChannel channel = new NnChannel(name, intro, imageUrl);
        channel.setPublic(false);
        channel.setStatus(NnChannel.STATUS_WAIT_FOR_APPROVAL);
        channel.setContentType(NnChannel.CONTENTTYPE_MIXED); // a channel type in podcast does not allow user to add program in it, so change to mixed type
        channel.setTemp(isTemp);
        NNF.getChannelMngr().save(channel);
        String[] result = {(String) NNF.getChannelMngr().composeEachChannelLineup(channel, ctx)};
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object programCreate(ApiContext ctx, String channel, String name, String description, String image, String audio, String video, boolean temp) {
        if (channel == null || channel.length() == 0 || name == null || name.length() == 0 ||
            (audio == null && video == null)) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        long cid = Long.parseLong(channel);
        NnChannel ch = NNF.getChannelMngr().findById(cid);
        if (ch == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.CHANNEL_INVALID);
        NnProgram program = new NnProgram(ch.getId(), name, description, image);
        program.setChannelId(cid);
        program.setAudioFileUrl(audio);
        program.setFileUrl(video);
        program.setContentType(NNF.getProgramMngr().getContentType(program));
        program.setStatus(NnProgram.STATUS_OK);
        program.setPublic(true);
        NNF.getProgramMngr().save(program);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    private short getStatus(String msg) {
        String[] status = msg.split("\t");
        if (status.length > 0)
            return Short.valueOf(status[0]);
        return NnStatusCode.ERROR;
    }
    
    public Object sphereData(ApiContext ctx, String token, String email, String password, HttpServletResponse resp) {
        List<String> data = new ArrayList<String>();
        return this.assembleSections(data);
    }
    
    public Object auxLogin(ApiContext ctx, String token, String email, String password, HttpServletResponse resp) {
        //1. user info
        List<String> data = new ArrayList<String>();
        log.info ("[quickLogin] verify user: " + token);
        String userInfo = "";
        if (token != null) {
            userInfo = (String) this.userTokenVerify(ctx, token, resp);
        } else if (email != null || password != null) {
            userInfo = (String) this.login(ctx, email, password, resp);
        } else {
            userInfo = (String) this.guestRegister(ctx, resp);
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
        String trending = (String) this.channelStack(ctx, Tag.TRENDING, sphere, token, null, false);
        data.add(trending);
        if (this.getStatus(trending) != NnStatusCode.SUCCESS) {
            return this.assembleSections(data);
        }
        //3. hottest
        log.info ("[quickLogin] hot channels");
        String hot = (String) this.channelStack(ctx, Tag.HOT, sphere, token, null, false);
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
    
    public Object quickLogin(ApiContext ctx, String token, String email, String password, HttpServletResponse resp) {
        //1. user info
        List<String> data = new ArrayList<String>();
        log.info ("[quickLogin] verify user: " + token);
        String userInfo = "";
        if (token != null) {
            userInfo = (String) this.userTokenVerify(ctx, token, resp);
        } else if (email != null || password != null) {
            userInfo = (String) this.login(ctx, email, password, resp);
        } else {
            userInfo = (String) this.guestRegister(ctx, resp);
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
        String lineup = (String) channelLineup(ctx, token, null, null, false, null, true, false, false, false, null);
        data.add(lineup);
        if (this.getStatus(lineup) != NnStatusCode.SUCCESS) {
            return this.assembleSections(data);
        }
        //3. featured curators
        log.info ("[quickLogin] featured curators");
        String curatorInfo = (String) this.curator(ctx, null, null, "featured");
        data.add(curatorInfo);
        //4. trending
        log.info ("[quickLogin] trending channels");
        String trending = (String) this.channelStack(ctx, Tag.TRENDING, sphere, token, null, false);
        data.add(trending);
        if (this.getStatus(trending) != NnStatusCode.SUCCESS) {
            return this.assembleSections(data);
        }
        //5. recommended
        log.info ("[quickLogin] recommended channels");
        String recommended = (String) this.channelStack(ctx, Tag.RECOMMEND, sphere, token, null, false);
        data.add(recommended);
        if (this.getStatus(recommended) != NnStatusCode.SUCCESS) {
            return this.assembleSections(data);
        }
        //6. featured
        log.info ("[quickLogin] featured channels");
        String featured = (String) this.channelStack(ctx, Tag.FEATURED, sphere, token, null, false);
        data.add(featured);
        if (this.getStatus(featured) != NnStatusCode.SUCCESS) {
            return this.assembleSections(data);
        }
        //7. hottest
        log.info ("[quickLogin] hot channels");
        String hot = (String) this.channelStack(ctx, Tag.HOT, sphere, token, null, false);
        data.add(hot);
        if (this.getStatus(hot) != NnStatusCode.SUCCESS) {
            return this.assembleSections(data);
        }
        //8. category top level
        // log.info ("[quickLogin] top level categories: " + ((sphere == null) ? "default" : sphere));
        // hardcoding to English for now, and keeping translations on the player side
        log.info ("[quickLogin] top level categories: en");
        String categoryTop = (String) this.category (ctx, null, false);
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
    
    public Object favorite(ApiContext ctx, String userToken, String channel, String program, String fileUrl, String name, String imageUrl, String duration, boolean delete) {
        if (userToken == null || (program == null && fileUrl == null) || channel == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        String[] result = {""};
        
        NnUser u = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
        if (u == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        long pid = 0;
        NnChannel c = NNF.getChannelMngr().findById(Long.parseLong(channel));
        if (c == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.CHANNEL_INVALID);
        NnProgram p = null;
        NnEpisode e = null;
        if (program != null) {
            if (program.contains("e")) {
                program = program.replace("e", "");
                e = NNF.getEpisodeMngr().findById(Long.parseLong(program));
                if (e == null)
                    return ctx.assemblePlayerMsgs(NnStatusCode.PROGRAM_INVALID);
            } else {
                pid = Long.parseLong(program);
                p = NNF.getProgramMngr().findById(Long.parseLong(program));
                if (p == null)
                    return ctx.assemblePlayerMsgs(NnStatusCode.PROGRAM_INVALID);
            }
        }
        //delete pid should not contain "e"
        if (delete) {
            NNF.getChannelMngr().deleteFavorite(u, pid);
        } else {
            NNF.getChannelMngr().saveFavorite(u, c, e, p, fileUrl, name, imageUrl, duration);
        }
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
        
    }
    
    public NnChannel getChannel(String channel) {
        NnChannel c = NNF.getChannelMngr().findById(Long.parseLong(channel));
        return c;
    }
    
    public Object graphSearch(ApiContext ctx, String email, String name) {
        if (email == null && name == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        List<NnUser> users = NNF.getUserMngr().search(email, name, null, ctx.getMsoId());
        String[] result = {""};
        for (NnUser u : users) {
            result[0] += u.getUserEmail() + "\t" + u.getProfile().getName() + "\n";
        }
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object userInvite(ApiContext ctx, String token, String toEmail, String toName, String channel) {
        if (token == null || toEmail == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        NnUser user = NNF.getUserMngr().findByToken(token, ctx.getMsoId());
        if (user == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.ACCOUNT_INVALID);
        }
        NnChannel c = NNF.getChannelMngr().findById(Long.parseLong(channel));
        if (c == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.CHANNEL_INVALID);
        }
        EmailService service = new EmailService();
        UserInviteDao dao = new UserInviteDao();
        UserInvite invite = dao.findInitiate(user.getId(), user.getShard(), toEmail, Long.parseLong(channel));
        if (invite != null) {
            log.info("old invite:" + invite.getId());
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, new String[] {invite.getInviteToken()});
        }
        String inviteToken = UserInvite.generateToken();
        invite = new UserInvite(user.getShard(), user.getId(),
                                inviteToken, c.getId(), toEmail, toName);
        
        invite = new UserInviteDao().save(invite);
        NnUserProfile profile = user.getProfile();
        String content = UserInvite.getInviteContent(user, invite.getInviteToken(), toName, profile.getName(), ctx.getReq());
        NnEmail mail = new NnEmail(toEmail, toName, NnEmail.SEND_EMAIL_SHARE,
                                   profile.getName(), user.getUserEmail(), UserInvite.getInviteSubject(),
                                   content);
        log.info("email content:" + UserInvite.getInviteContent(user, invite.getInviteToken(), toName, profile.getName(), ctx.getReq()));
        service.sendEmail(mail, null, null);
        String[] result = {invite.getInviteToken()};
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object inviteStatus(ApiContext ctx, String inviteToken) {
        UserInvite invite = new UserInviteDao().findByToken(inviteToken);
        if (invite == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INVITE_INVALID);
        String[] result = {String.valueOf(invite.getStatus())};
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object disconnect(ApiContext ctx, String userToken, String email, String channel) {
        if (userToken == null || email == null || channel == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        NnUser user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
        if (user == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        }
        NnUser invitee = NNF.getUserMngr().findByEmail(email, ctx.getMsoId(), ctx.getReq());
        if (invitee == null) {
            log.info("invitee does not exist:" + email);
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
        }
        UserInviteDao dao = new UserInviteDao();
        List<UserInvite> invites = new UserInviteDao().findSubscribers(user.getId(), invitee.getId(), Long.parseLong(channel));
        if (invites.size() == 0) {
            log.info("invite not exist: user id:" + user.getId() + ";invitee id:" + invitee.getId());
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
        }
        for (UserInvite u : invites) {
            u.setInviteeId(0);
            dao.save(u);
        }
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    @SuppressWarnings({ "rawtypes" })
    public Object notifySubscriber(ApiContext ctx, String userToken, String channel) {
        NnUser user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
        if (user == null) {
            
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        }
        Map<String, NnUser> umap = new HashMap<String, NnUser>();
        Map<String, String> cmap = new HashMap<String, String>();
        String[] cid = channel.split(",");
        
        for (String id : cid) {
            NnChannel c = NNF.getChannelMngr().findById(Long.parseLong(id));
            if (c == null) {
                return ctx.assemblePlayerMsgs(NnStatusCode.CHANNEL_INVALID);
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
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object curator(ApiContext ctx, String profile, String userToken, String stack) {
        if (stack == null && profile == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        NnUserManager userMngr = NNF.getUserMngr();
        List<NnUser> users = new ArrayList<NnUser>();
        if (profile != null) {
            NnUser user = userMngr.findByProfileUrl(profile, ctx.getMsoId());
            if (user == null)
                return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
            users.add(user);
        } else {
            users = userMngr.findFeatured(ctx.getMsoId());
        }
        NnUser user = null;
        if (userToken != null) {
            user = userMngr.findByToken(userToken, ctx.getMsoId());
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
            result[0] = userMngr.composeCuratorInfo(ctx, users, true, isAllChannel);
        else
            result[0] = userMngr.composeCuratorInfo(ctx, users, false, isAllChannel);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object virtualChannel(ApiContext ctx, String stack, String userToken, String channel, boolean isPrograms) {
        //check input
        if (stack == null && userToken == null && channel == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        //get channels
        List<NnChannel> channels = new ArrayList<NnChannel>();
        NnUser user = null;
        boolean chPos = false;
        if (userToken != null) {
            user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
            if (user == null) {
                NnGuest guest = new NnGuestManager().findByToken(userToken);
                if (guest == null)
                    return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
                else
                    return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
            }
            NnUserSubscribeManager subMngr = new NnUserSubscribeManager();
            channels.addAll(subMngr.findSubscribedChannels(user));
            chPos = true;
            log.info("virtual channel find by subscriptions:" + user.getId());
        } else if (stack != null) {
            log.info("virtual channel find by stack:" + stack + ";lang=" + ctx.getLang());
            String[] cond = stack.split(",");
            for (String s : cond) {
                if (s.equals(Tag.RECOMMEND)) {
                    channels.addAll(new RecommendService().findRecommend(userToken, ctx.getMsoId(), ctx.getLang()));
                } else if (s.equals("mayLike")) {
                    return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
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
                        SysTagDisplay display = NNF.getDisplayMngr().findByName(stack, ctx.getMsoId());
                        if (display != null)
                            systagId = display.getSystagId();
                    } else {
                        log.info("channelStack findbyid:" + stack);
                        SysTagDisplay display = NNF.getDisplayMngr().findById(Long.parseLong(stack));
                        if (display != null)
                            systagId = display.getSystagId();
                    }
                    channels.addAll(NNF.getSysTagMngr().findPlayerChannelsById(systagId, ctx.getLang(), true, 0));
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
            String channelInfo = (String)NNF.getChannelMngr().composeReducedChannelLineup(channels, ApiContext.FORMAT_PLAIN);
            if (chPos)
                channelInfo = (String)NNF.getChannelMngr().chAdjust(channels, channelInfo, new ArrayList<ChannelLineup>(), ctx.getFmt());
            log.info("virtual channel, return channel only");
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, new String[] {channelInfo});
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
        String channelInfo = (String) NNF.getChannelMngr().composeReducedChannelLineup(channels, ApiContext.FORMAT_PLAIN);
        if (chPos) {
            log.info("adjust sequence of channellineup for user:" + user.getId());
            channelInfo = (String) NNF.getChannelMngr().chAdjust(channels, channelInfo, new ArrayList<ChannelLineup>(), ApiContext.FORMAT_PLAIN);
        }
        result.add(channelInfo);
        result.add(programInfo);
        String size[] = new String[result.size()];
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result.toArray(size));
    }
    
    public Object portal(ApiContext ctx, String time, boolean minimal, String type) {
        short baseTime = Short.valueOf(time);
        if (baseTime > 23 || baseTime < 0)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        
        if (type == null)
           type = "portal";
        
        if (type != null && type.equals("whatson")) {
           String whatsOnResult[] = (String[])NNF.getDisplayMngr().getPlayerWhatson(baseTime, minimal, ctx);
           String regularResult[] = (String[])NNF.getDisplayMngr().getPlayerPortal(minimal, ctx);
           String set = "";
           String channel = "";
           String program = "";
           if (whatsOnResult.length == 3 && regularResult.length == 3) {
              set = whatsOnResult[0] + regularResult[0];
              channel = whatsOnResult[1] + regularResult[1];
              program = whatsOnResult[2] + regularResult[2];
              String result[] = {set, channel, program};
              return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
           }
        }
        
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, NNF.getDisplayMngr().getPlayerPortal(minimal, ctx));
    }
    
    public Object whatson(ApiContext ctx, String time, boolean minimal) {
        short baseTime = Short.valueOf(time);
        if (baseTime > 23 || baseTime < 0)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, NNF.getDisplayMngr().getPlayerWhatson(baseTime, minimal, ctx));
    }
    
    public Object frontpage(ApiContext ctx, String time, String stack, String user) {
        short baseTime = Short.valueOf(time);
        String lang = LocaleTable.LANG_EN;
        lang = ctx.getMso().getLang();
        //section 1: items
        List<String> data = new ArrayList<String>();
        String[] itemOutput = {""};
        
        SysTagDisplayManager displayMngr = NNF.getDisplayMngr();
        List<SysTagDisplay> displays = new ArrayList<SysTagDisplay>();
        //1. dayparting
        SysTagDisplay dayparting = displayMngr.findDayparting(baseTime, lang, ctx.getMsoId());
        if (dayparting != null)
            displays.add(dayparting);
        //2. on previosly
        SysTagDisplay previously = displayMngr.findPrevious(ctx.getMsoId(), lang, dayparting);
        if (previously != null)
            displays.add(previously);
        //2.5. newly added
        displays.addAll(displayMngr.findRecommendedSets(lang, ctx.getMsoId()));
        //3. following
        SysTagDisplay follow = displayMngr.findByType(ctx.getMsoId(), SysTag.TYPE_SUBSCRIPTION, lang);
        if (follow != null)
            displays.add(follow);
        //4 account
        SysTagDisplay account = displayMngr.findByType(ctx.getMsoId(), SysTag.TYPE_ACCOUNT, lang);
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
        itemOutput[0] = (String) ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, itemOutput);
        data.add(itemOutput[0]);
        try {
            //section 2: virtual channels
            String virtualOutput = "";
            String stackName = String.valueOf(dayparting.getId());
            virtualOutput = (String) this.virtualChannel(ctx, stackName, user, null, false);
            data.add(virtualOutput);
            return this.assembleSections(data);
        } catch (Exception e) {
            NnLogUtil.logException(e);
            return this.assembleSections(data);
        }
    }
    
    public Object virtualChannelAdd(ApiContext ctx, String user, String channel,
                                    String payload, boolean isQueued) {
        if (user == null || payload == null || channel == null) {
            log.info("data is missing");
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        
        if (isQueued) {
            log.info("virtual channel from user:" + user + " throwing to queue" );
            String url = "/playerAPI/virtualChannelAdd?queued=false";
            String data = "channel=" + channel + "&payload=" + payload + ";&user=" + user;
            QueueFactory.add(url, QueueFactory.METHOD_POST, QueueFactory.CONTENTTYPE_TEXT, data);
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
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
        
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object bulkIdentifier(ApiContext ctx, String ytUsers) {
        //input
        if (ytUsers == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
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
        String channelInfo = (String) NNF.getChannelMngr().composeReducedChannelLineup(channels, ApiContext.FORMAT_PLAIN);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, new String[] {channelInfo});
    }
    
    public Object bulkSubscribe(ApiContext ctx, String userToken, String ytUsers) {
        //input
        if (userToken == null || ytUsers == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        //user
        NnUser user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
        if (user == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
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
        String channelInfo = (String) NNF.getChannelMngr().composeReducedChannelLineup(channels, ApiContext.FORMAT_PLAIN);
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
        
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, new String[] {channelInfo});
    }
    
    public Object obtainAccount(ApiContext ctx, String email, String password,
            String name, HttpServletRequest req, HttpServletResponse resp) {
       if (email == null || password == null) {
           return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
       }
       //find existed account
       NnUser user = NNF.getUserMngr().findByEmail(email, ctx.getMsoId(), req);
       if (user != null) {
           log.info("returning youtube connect account");
           return this.login(ctx, email, password, resp);
       }
       //signing up a new one
       //verify inputs
       int status = NnUserValidator.validatePassword(password);
       if (status != NnStatusCode.SUCCESS)
           return ctx.assemblePlayerMsgs(status);
       boolean success = BasicValidator.validateEmail(email);
       if (!success)
           return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
       //create new account
       log.info("signing up a new youtube connect account");
       short type = NnUser.TYPE_USER;
       if ((email.contains("-AT-") || email.contains("-at-"))
               && email.contains("@9x9.tv")) {
           type = NnUser.TYPE_YOUTUBE_CONNECT;
       }
       if (name == null)
           name = email;
       NnUser newUser = new NnUser(email, password, type, ctx.getMsoId());
       NnUserProfile profile = new NnUserProfile(ctx.getMsoId(), name, LocaleTable.LANG_EN, LocaleTable.LANG_EN, null);
       newUser.setProfile(profile);
       newUser.setTemp(false);
       status = NNF.getUserMngr().create(newUser, req, (short)0);
       if (status != NnStatusCode.SUCCESS)
           return ctx.assemblePlayerMsgs(status);
       String result[] = {(String) prepareUserInfo(ctx, newUser, null, false)};
       this.setUserCookie(resp, CookieHelper.USER, newUser.getToken());
       return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object channelUpdate(ApiContext ctx, String user,
                             String payload, boolean isQueued) {
        if (user == null || payload == null) {
           log.info("data is missing");
           return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        
        if (isQueued) {
           log.info("channel update from user:" + user + " throwing to queue" );
           String url = "/playerAPI/channelUpdate?queued=false";
           String data = "payload=" + payload + ";&user=" + user;
           QueueFactory.add(url, QueueFactory.METHOD_POST, QueueFactory.CONTENTTYPE_TEXT, data);
           return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
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
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
     }
    
    public Object latestEpisode(ApiContext ctx, String channel) {
        //check input
        if (channel == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
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
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object setInfo(ApiContext ctx, String id, String name, String time, boolean isProgramInfo) {
        if (id == null && name == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
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
            display = NNF.getDisplayMngr().findByName(name, ctx.getMsoId());
        }
        if (display == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.SET_INVALID);
        if (systagId == 0)
            systagId = display.getSystagId();
        SysTag systag = NNF.getSysTagMngr().findById(systagId);
        List<NnChannel> channels = new ArrayList<NnChannel>();
        if (systag.getType() == SysTag.TYPE_DAYPARTING) {
            channels.addAll(NNF.getSysTagMngr().findDaypartingChannelsById(systagId, display.getLang(), ctx.getMsoId(), Short.parseShort(time)));
        } else if (systag.getType() == SysTag.TYPE_WHATSON) {
            channels.addAll(NNF.getSysTagMngr().findPlayerAllChannelsById(systagId, display.getLang(), SysTag.SORT_SEQ, ctx.getMsoId()));
        } else {
            channels.addAll(NNF.getSysTagMngr().findPlayerAllChannelsById(systagId, null, systag.getSorting(), 0));
        }
        List<NnProgram> programs = new ArrayList<NnProgram>();
        Short shortTime = 24;
        if (time != null)
            shortTime = Short.valueOf(time);
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, NNF.getDisplayMngr().getPlayerSetInfo(ctx.getMso(), systag, display, channels, programs, shortTime, isProgramInfo, ctx));
    }
    
    public Object endpointRegister(ApiContext ctx, String userToken, String token, String vendor, String action) {
        //input verification
        if (userToken == null || token == null || vendor == null || action == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        @SuppressWarnings("rawtypes")
        HashMap map = checkUser(ctx.getMsoId(), userToken, false);
        if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
            return ctx.assemblePlayerMsgs((Integer)map.get("s"));
        }
        NnUser user = (NnUser) map.get("u");
        if (!action.equals("register") && !action.equals("unregister"))
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        boolean register = true;
        if (action.equals("unregister"))
            register = false;
        user = (NnUser) map.get("u");
        //endpoint verify
        EndPointManager endpointMngr = new EndPointManager();
        short srtVendor = endpointMngr.getVendorType(vendor);
        if (srtVendor == EndPoint.VENDOR_UNDEFINED)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        EndPoint endpoint = endpointMngr.findByEndPoint(user.getId(), ctx.getMsoId(), srtVendor);
        if (register) {
            if (endpoint == null) {
                endpoint = new EndPoint(user.getId(), ctx.getMsoId(), token, srtVendor);
            } else {
                endpoint.setToken(token);
            }
            endpointMngr.save(endpoint);
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
        }
        if (!register) {
            if (endpoint == null) {
                return ctx.assemblePlayerMsgs(NnStatusCode.DEVICE_INVALID);
            } else {
                endpointMngr.delete(endpoint);
            }
        }
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object poiAction(ApiContext ctx, String userToken, String deviceToken, String vendor, String poiId, String select) {
        //input verification
        if (userToken == null || poiId == null || select == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        @SuppressWarnings("rawtypes")
        HashMap map = checkUser(ctx.getMsoId(), userToken, false);
        if ((Integer)map.get("s") != NnStatusCode.SUCCESS) {
            return ctx.assemblePlayerMsgs((Integer)map.get("s"));
        }
        NnUser user = (NnUser) map.get("u");
        long lPoiId = Long.parseLong(poiId);
        Poi poi = NNF.getPoiPointMngr().findPoiById(lPoiId);
        if (poi == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.POI_INVALID); //poi invalid
        PoiEvent event = NNF.getPoiEventMngr().findByPoiId(lPoiId);
        if (event == null) {
            log.info("event invalid");
            return ctx.assemblePlayerMsgs(NnStatusCode.POI_INVALID); //poi invalid
        }
        //?! requirement question: or it's user based
        if (event.getType() == PoiEvent.TYPE_INSTANTNOTIFICATION || event.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            if (deviceToken == null || vendor == null)
                return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
            EndPointManager endpointMngr = new EndPointManager();
            EndPoint endpoint = endpointMngr.findByEndPoint(user.getId(), user.getMsoId(), endpointMngr.getVendorType(vendor));
            if (endpoint == null)
                return ctx.assemblePlayerMsgs(NnStatusCode.DEVICE_INVALID);
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
                return ctx.assemblePlayerMsgs(NnStatusCode.POI_DUPLICATED);
            } else {
                pdrMngr.processPoiScheduler(user, poi, event, select);
                return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
            }
        } else if (event.getType() == PoiEvent.TYPE_POLL) {
            //TYPE_POLL, data will be saved in poi_pdr;
            PoiPdr pdr = pdrMngr.findPoiPdr(user, lPoiId);
            if (pdr != null) {
                return ctx.assemblePlayerMsgs(NnStatusCode.POI_DUPLICATED);
            } else {
                pdrMngr.processPoiPdr(user, poi, event, select);
                return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
            }
        }
        
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
    }
    
    public Object shareInChannelList(ApiContext ctx, Long channelId) {
        
        if (channelId == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.CHANNEL_INVALID);
        
        NnChannel channel = NNF.getChannelMngr().findById(channelId);
        if (channel == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.CHANNEL_INVALID);
        
        List<NnChannel> channels = new ArrayList<NnChannel>();
        NnUser curator = null;
        String result[] = {"", "", "", ""};
        
        //mso info
        Mso mso = ctx.getMso();
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
                return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
            
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
        result[2] = (String) NNF.getChannelMngr().composeChannelLineup(channels, ctx);
        //program info
        String programStr = (String) NNF.getProgramMngr().findLatestProgramInfoByChannels(channels, ApiContext.FORMAT_PLAIN);
        result[3] = programStr;
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object getUserNames(ApiContext ctx, String ids) {
        if (ids == null)
           return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        NnUserProfileManager profileMngr = NNF.getProfileMngr();
        String[] idstr = ids.split(",");
        String returnStr = "";
        for (String id : idstr) {
            NnUser user = NNF.getUserMngr().findByIdStr(id, ctx.getMsoId());
            if (user != null) {
                NnUserProfile profile = profileMngr.findByUser(user);
                if (profile != null)
                  returnStr += id + "\t" + profile.getName() + "\n";
                else
                  returnStr += id + "\t" + "anonymous" + "\n";
            } else {
                returnStr += id + "\t" + "anonymous" + "\n";
            }
        }
        String[] result = {returnStr};
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object getDirectUrl(ApiContext ctx, String url, String programIdStr) {
        if (url == null && programIdStr == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        log.info("url = " + url + ", programId = " + programIdStr);
        if (url == null) {
            NnProgram program = NNF.getProgramMngr().findById(programIdStr);
            if (program == null)
                return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
            url = program.getFileUrl();
        } else {
            url = url.trim();
        }
        StreamLib streamLib = StreamFactory.getStreamLib(url);
        if (streamLib == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_BAD);
        String videoUrl = streamLib.getHtml5DirectVideoUrl(url);
        if (videoUrl == null || videoUrl.isEmpty())
           return ctx.assemblePlayerMsgs(NnStatusCode.PROGRAM_ERROR);
        log.info("video url:" + videoUrl);
        String data = PlayerApiService.assembleKeyValue("url", videoUrl);
        String[] result = {data};
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object generateSignedUrls(ApiContext ctx, String url) {
        if (url == null || url.length() == 0)
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        String[] urls = url.split(",");
        MsoConfigManager configMngr = NNF.getConfigMngr();
        Mso mso = ctx.getMso();
        // NOTE: maybe the mso can be determined by bucket name
        MsoConfig cfSubomainConfig = configMngr.findByMsoAndItem(mso, MsoConfig.CF_SUBDOMAIN);
        if (cfSubomainConfig == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.DATA_ERROR);
        MsoConfig cfKeypairConfig = configMngr.findByMsoAndItem(mso, MsoConfig.CF_KEY_PAIR_ID);
        if (cfKeypairConfig == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.DATA_ERROR);
        
        String keypair = cfKeypairConfig.getValue();
        String cfDomainStr = cfSubomainConfig.getValue() + ".cloudfront.net";
        String privateKeyPath = MsoConfigManager.getCFPrivateKeyPath(mso);
        log.info("private key path:" + privateKeyPath);
        String videoStr = "";
        for (String u : urls) {
            try {
                u = URLDecoder.decode(u, "utf-8");
                Matcher matcher = Pattern.compile(AmazonLib.REGEX_S3_URL).matcher(u);
                String signedUrl = "";
                if (matcher.find()) {
                    signedUrl = AmazonLib.cfUrlSignature(cfDomainStr, privateKeyPath, keypair, matcher.group(2));
                } else {
                    signedUrl = u;
                }
                videoStr += u + "\t" + signedUrl + "\n";
            } catch (UnsupportedEncodingException e) {
                NnLogUtil.logException(e);
            }
        }
        String[] result = {videoStr};
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object notificationList(ApiContext ctx, String token) {
        
        if (token == null)
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS);
        
        NnDeviceNotificationManager notificationMngr = NNF.getDeviceNotiMngr();
        
        List<NnDevice> devices = NNF.getDeviceMngr().findByToken(token);
        if (devices.isEmpty())
            return ctx.assemblePlayerMsgs(NnStatusCode.DEVICE_INVALID);
        
        NnDevice device = devices.get(0);
        String badge = PlayerApiService.assembleKeyValue("badge", String.valueOf(device.getBadge()));
        
        if (ctx.getParam("minimal") != null) {
            String[] result = { badge };
            return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
        }
        
        List<NnDeviceNotification> notifications = notificationMngr.findByDeviceId(device.getId());
        
        if (ctx.getParam("clean") != null) {
            
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
        
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object addPurchase(ApiContext ctx, String userToken, String productIdRef, String purchaseToken) {
        
        if (purchaseToken == null || productIdRef == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_MISSING);
        }
        
        if (userToken == null) {
            userToken = ctx.getCookie(CookieHelper.USER);
            if (userToken == null) {
                return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
            }
        }
        
        NnUser user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
        if (user == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        }
        
        NnItem item = NNF.getItemMngr().findByProductIdRef(productIdRef);
        if (item == null) {
            log.warning("invalid productIdRef = " + productIdRef);
            return ctx.assemblePlayerMsgs(NnStatusCode.INPUT_ERROR);
        }
        
        NnPurchase purchase = NNF.getPurchaseMngr().findByUserAndItem(user, item);
        if (purchase != null) {
            // renew token existing purchase
            purchase.setStatus(NnPurchase.ACTIVE);
            purchase.setVerified(false);
            purchase.setPurchaseToken(purchaseToken);
        } else {
            purchase = new NnPurchase(item, user, purchaseToken);
        }
        purchase = NNF.getPurchaseMngr().save(purchase);
        
        NNF.getPurchaseMngr().verifyPurchase(purchase, ctx.isProductionSite());
        
        return getPurchases(ctx, userToken);
    }
    
    public Object getPurchases(ApiContext ctx, String userToken) {
        
        if (userToken == null) {
            userToken = ctx.getCookie(CookieHelper.USER);
            if (userToken == null) {
                return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
            }
        }
        
        NnUser user = NNF.getUserMngr().findByToken(userToken, ctx.getMsoId());
        if (user == null) {
            return ctx.assemblePlayerMsgs(NnStatusCode.USER_INVALID);
        }
        
        String purchasesStr = "";
        List<NnPurchase> purchases = NNF.getPurchaseMngr().findByUser(user);
        for (NnPurchase purchase : purchases) {
            
            NnItem item = NNF.getItemMngr().findById(purchase.getItemId());
            if (item == null) {
                log.warning("item not found, itemId = " + purchase.getItemId());
                continue;
            }
            if (item.getMsoId() == ctx.getMsoId() && purchase.isVerified()) {
                
                purchasesStr += (String) NNF.getItemMngr().composeEachItem(item) + "\n";
            }
        }
        
        String[] result = { purchasesStr };
        
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object getItems(ApiContext ctx) {
        
        List<NnItem> items = NNF.getItemMngr().findByMsoAndOs(ctx.getMso(), ctx.getOs());
        
        String purchasesStr = "";
        for (NnItem item : items) {
            
            purchasesStr += (String) NNF.getItemMngr().composeEachItem(item) + "\n";
        }
        
        String[] result = { purchasesStr };
        
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
    public Object chat(ApiContext ctx, String userToken) {
        String[] result = { "" };
        return ctx.assemblePlayerMsgs(NnStatusCode.SUCCESS, result);
    }
    
}
