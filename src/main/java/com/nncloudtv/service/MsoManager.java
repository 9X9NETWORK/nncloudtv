package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.MsoDao;
import com.nncloudtv.dao.ShardedCounter;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.AdPlacement;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnItem;
import com.nncloudtv.model.NnPurchase;
import com.nncloudtv.web.api.ApiContext;
import com.nncloudtv.web.json.player.BrandInfo;

@Service
public class MsoManager {

    protected static final Logger log = Logger.getLogger(MsoManager.class.getName());
    
    private MsoDao dao = NNF.getMsoDao();
    
    public Mso findOneByName(String name) {
        
        if (name != null) {
            
            Mso mso = findByName(name);
            if (mso != null) {
                
                return mso;
            }
        }
        
        return getSystemMso();
    }
    
    public long addMsoVisitCounter(boolean readOnly) {        
        String counterName = "9x9" + "BrandInfo";
        CounterFactory factory = new CounterFactory();
        ShardedCounter counter = factory.getOrCreateCounter(counterName);
        if (!readOnly) {
            counter.increment();
        }
        return counter.getCount();
    }
    
    // only 9x9 mso will be stored in cache
    public Mso save(Mso mso) {
        
        // avoid save mso which name is duplicate with other, except itself
        Mso origin = this.findByName(mso.getName());
        if (origin != null && origin.getId() != mso.getId()) { 
            return null;
        }
        
        Date now = new Date();
        if (mso.getCreateDate() == null) {
            
            mso.setCreateDate(now);
        }
        mso.setUpdateDate(now);
        dao.save(mso);
        resetCache(mso);
        
        return mso;
    }
    
    public void resetCache(Mso mso) {
        
        if (mso == null) { return; }
        
        String keyAdInfoPlain  = CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_PLAIN);
        String keyAdInfoJson   = CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_JSON);
        String keyAndroidJson  = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_ANDROID, PlayerApiService.FORMAT_JSON);
        String keyAndroidPlain = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_ANDROID, PlayerApiService.FORMAT_PLAIN);
        String keyIosJson      = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_IOS, PlayerApiService.FORMAT_JSON);
        String keyIosPlain     = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_IOS, PlayerApiService.FORMAT_PLAIN);
        String keyWebJson      = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_WEB, PlayerApiService.FORMAT_JSON);
        String keyWebPlain     = CacheFactory.getBrandInfoKey(mso, PlayerService.OS_WEB, PlayerApiService.FORMAT_PLAIN);
        MsoConfigManager configMngr = NNF.getConfigMngr();
        String appConfig= configMngr.getCacheKeyByMsoAndKey(mso.getId(), MsoConfig.APP_EXPIRE);
        String appVersionConfig= configMngr.getCacheKeyByMsoAndKey(mso.getId(), MsoConfig.APP_VERSION_EXPIRE);
        CacheFactory.delete(keyAdInfoPlain);
        CacheFactory.delete(keyAdInfoJson);
        CacheFactory.delete(keyAndroidJson);
        CacheFactory.delete(keyAndroidPlain);
        CacheFactory.delete(keyIosJson);
        CacheFactory.delete(keyIosPlain);
        CacheFactory.delete(keyWebJson);
        CacheFactory.delete(keyWebPlain);
        CacheFactory.delete(appConfig);        
        CacheFactory.delete(appVersionConfig);        
    }
    
    public static Mso getSystemMso() {
        
        List<Mso> list = NNF.getMsoMngr().findByType(Mso.TYPE_NN);
        
        return list.get(0);
    }
    
    public static long getSystemMsoId() {
        
        return getSystemMso().getId();
    }
    
    public static boolean isNNMso(Mso mso) {
        if (mso == null)
            return false;
        if (mso.getId() == 1)
            return true;
        return false;
    }
    
    private String composeBrandInfoStr(Mso mso, String os) {
        
        MsoConfigManager configMngr = NNF.getConfigMngr();
        
        //general setting
        String result = PlayerApiService.assembleKeyValue("key", String.valueOf(mso.getId()));
        result += PlayerApiService.assembleKeyValue("name", mso.getName());
        result += PlayerApiService.assembleKeyValue("title", mso.getTitle());        
        result += PlayerApiService.assembleKeyValue("logoUrl", mso.getLogoUrl());
        result += PlayerApiService.assembleKeyValue("jingleUrl", mso.getJingleUrl());
        result += PlayerApiService.assembleKeyValue("preferredLangCode", mso.getLang());
        result += PlayerApiService.assembleKeyValue("jingleUrl", mso.getJingleUrl());
        List<MsoConfig> list = configMngr.findByMso(mso);
        //config
        boolean regionSet = false;
        boolean chromecastId = false;
        boolean facebookId = false;
        boolean searchSet = false;
        boolean audioSet = false;
        String adIosType = null;
        for (MsoConfig c : list) {
            if (c.getItem().equals(MsoConfig.DEBUG))
                result += PlayerApiService.assembleKeyValue(MsoConfig.DEBUG, c.getValue());
            if (c.getItem().equals(MsoConfig.FBTOKEN))
                result += PlayerApiService.assembleKeyValue(MsoConfig.FBTOKEN, c.getValue());
            if (c.getItem().equals(MsoConfig.RO)) {
                result += PlayerApiService.assembleKeyValue(MsoConfig.RO, c.getValue());
            }            
            if (c.getItem().equals(MsoConfig.SUPPORTED_REGION)) {
            	result += PlayerApiService.assembleKeyValue(MsoConfig.SUPPORTED_REGION, c.getValue());
            	regionSet = true;
            }
            if (c.getItem().equals(MsoConfig.FORCE_UPGRADE)) {
                result += PlayerApiService.assembleKeyValue(MsoConfig.FORCE_UPGRADE, c.getValue());
            }            
            if (c.getItem().equals(MsoConfig.UPGRADE_MSG)) {
                result += PlayerApiService.assembleKeyValue(MsoConfig.UPGRADE_MSG, c.getValue());
            }    
            if (c.getItem().equals(MsoConfig.VIDEO)) {
                result += PlayerApiService.assembleKeyValue(MsoConfig.VIDEO, c.getValue());
            }
            if (c.getItem().equals(MsoConfig.FACEBOOK_CLIENTID)) {
                facebookId = true;
                result += PlayerApiService.assembleKeyValue(MsoConfig.FACEBOOK_CLIENTID, c.getValue());
            }
            if (c.getItem().equals(MsoConfig.CHROMECAST_ID)) {
                chromecastId = true;
                result += PlayerApiService.assembleKeyValue(MsoConfig.CHROMECAST_ID, c.getValue());
            }
            if (c.getItem().equals(MsoConfig.GCM_SENDER_ID) && os.equals(PlayerService.OS_ANDROID)) {
                result += PlayerApiService.assembleKeyValue(MsoConfig.GCM_SENDER_ID, c.getValue());
            }
            if (c.getItem().equals(MsoConfig.SHAKE_DISCOVER) && (!os.equals(PlayerService.OS_WEB)) && c.getValue() != null && c.getValue().equals("on")) {
            	result += PlayerApiService.assembleKeyValue(MsoConfig.SHAKE_DISCOVER, c.getValue());
            }
            if (c.getItem().equals(MsoConfig.ABOUT_US)) {
                String aboutus = c.getValue().replaceAll("\t", "").replaceAll("\n", "{BR}");
                result += PlayerApiService.assembleKeyValue(MsoConfig.ABOUT_US, aboutus);
            }
            if (c.getItem().equals(MsoConfig.SOCIAL_FEEDS)) {
                result += PlayerApiService.assembleKeyValue(MsoConfig.SOCIAL_FEEDS, c.getValue());
            }
            if (c.getItem().equals(MsoConfig.SOCIAL_FEEDS_SERVER)) {
            	result += PlayerApiService.assembleKeyValue(MsoConfig.SOCIAL_FEEDS_SERVER, c.getValue());
            }
            if (c.getItem().equals(MsoConfig.AD_IOS_TYPE)) {
            	adIosType = c.getValue();
            }
            if (c.getItem().equals(MsoConfig.SEARCH)) {
            	searchSet = true;
                result += PlayerApiService.assembleKeyValue(MsoConfig.SEARCH, c.getValue());            	
            }
            if (c.getItem().equals(MsoConfig.AUDIO_BACKGROUND) && !os.equals(PlayerService.OS_WEB)) {
            	audioSet = true;
            	result += PlayerApiService.assembleKeyValue(MsoConfig.AUDIO_BACKGROUND, c.getValue());
            }
        }
        /*
        if (videoSet == false) {
        	result[0] += PlayerApiService.assembleKeyValue(MsoConfig.VIDEO, "en w-YkGyubqcA;zh w-YkGyubqcA");
        }
        */
        if (regionSet == false)
        	result += PlayerApiService.assembleKeyValue(MsoConfig.SUPPORTED_REGION, "en US;zh 台灣");
        if (chromecastId == false)
            result += PlayerApiService.assembleKeyValue(MsoConfig.CHROMECAST_ID, "DBB1992C");
        if (facebookId == false)
            result += PlayerApiService.assembleKeyValue(MsoConfig.FACEBOOK_CLIENTID, "361253423962738");
        if (searchSet == false)
        	result += PlayerApiService.assembleKeyValue(MsoConfig.SEARCH, "all");
        if (audioSet == false && !os.equals(PlayerService.OS_WEB))
        	result += PlayerApiService.assembleKeyValue(MsoConfig.AUDIO_BACKGROUND, "off");
        //add ga based on device
        String gaKeyName = configMngr.getKeyNameByOs(os, "google");
        if (gaKeyName != null) {
            MsoConfig gaKeyConfig = configMngr.findByMsoAndItem(mso, gaKeyName);
            String ga = configMngr.getDefaultValueByOs(os, "google");
            if (gaKeyConfig != null) 
                ga = gaKeyConfig.getValue();
            if (ga != null)
                result += PlayerApiService.assembleKeyValue("ga", ga);
        }
        //add flurry based on device
        String flurryKeyName = configMngr.getKeyNameByOs(os, "flurry");
        if (flurryKeyName != null) {
            MsoConfig flurryConfig = configMngr.findByMsoAndItem(mso, flurryKeyName);
            String flurry = configMngr.getDefaultValueByOs(os, "flurry");
            if (flurryConfig != null) 
                flurry = flurryConfig.getValue();
            if (flurry != null) 
                result += PlayerApiService.assembleKeyValue("flurry", flurry);
        }
        
        String ad = configMngr.getAdConfig(mso, os);
        if (ad != null) {
            result += PlayerApiService.assembleKeyValue("ad", ad);
            if (os.equals(PlayerService.OS_IOS) && adIosType != null)
            	result += PlayerApiService.assembleKeyValue("ad-type", adIosType);          	
        }
                
        String admobkeyKeyName = configMngr.getKeyNameByOs(os, "admobkey");
        if (admobkeyKeyName != null) {
            MsoConfig admobKeyConfig = configMngr.findByMsoAndItem(mso, admobkeyKeyName);
            if (admobKeyConfig != null) { 
                result += PlayerApiService.assembleKeyValue("admob-key", admobKeyConfig.getValue());
            }
        }
                
        String youtubeKeyName = configMngr.getKeyNameByOs(os, "youtube");        
        if (youtubeKeyName != null) {
            MsoConfig youtubeConfig = configMngr.findByMsoAndItem(mso, youtubeKeyName);
            String youtube = configMngr.getDefaultValueByOs(os, "youtube");
            if (youtubeConfig != null) 
                youtube = youtubeConfig.getValue();            
            if (youtube != null) {
                result += PlayerApiService.assembleKeyValue("youtube", youtube);            
            }
        }
        
        if (!os.equals(PlayerService.OS_WEB)) {
            MsoConfig homepage = configMngr.findByMsoAndItem(mso, MsoConfig.HOMEPAGE);
            if (homepage != null)
                result += PlayerApiService.assembleKeyValue("homepage", homepage.getValue());
            else
                result += PlayerApiService.assembleKeyValue("homepage", "portal");            	
            MsoConfig sound = configMngr.findByMsoAndItem(mso, MsoConfig.NOTIFICATION_SOUND_VIBRATION);
            String value = "sound off;vibration off";
            if (sound != null)
            	value = sound.getValue();
            result += PlayerApiService.assembleKeyValue(MsoConfig.NOTIFICATION_SOUND_VIBRATION, value);
            MsoConfig signup = configMngr.findByMsoAndItem(mso, MsoConfig.SIGNUP_ENFORCE);
            value = "never";
            if (signup != null)
            	value = signup.getValue();
            result += PlayerApiService.assembleKeyValue(MsoConfig.SIGNUP_ENFORCE, value);            
        }        

        CacheFactory.set(CacheFactory.getBrandInfoKey(mso, os, PlayerApiService.FORMAT_PLAIN), result);
        return result;
    }
    
    //missing the latest stuff
    private Object composeBrandInfoJson(Mso mso, String os) {
        BrandInfo info = new BrandInfo();        
        //general setting
        info.setKey(mso.getId());
        info.setName(mso.getName());
        info.setTitle(mso.getTitle());        
        info.setLogoUrl(mso.getLogoUrl());
        info.setJingleUrl(mso.getJingleUrl());
        info.setPreferredLangCode(mso.getLang());
        List<MsoConfig> list = NNF.getConfigMngr().findByMso(mso);
        //config
        for (MsoConfig c : list) {
            System.out.println(c.getItem() + ";" + c.getValue());
            if (c.getItem().equals(MsoConfig.DEBUG))
                info.setDebug(c.getValue());
            if (c.getItem().equals(MsoConfig.FBTOKEN))
                info.setFbToken(c.getValue());
            if (c.getItem().equals(MsoConfig.RO))
                info.setReadOnly(c.getValue());
            if (c.getItem().equals(MsoConfig.SUPPORTED_REGION))
            	info.setSupportedRegion(c.getValue());
            if (c.getItem().equals(MsoConfig.FORCE_UPGRADE))
                info.setForceUpgrade(c.getValue());
            if (c.getItem().equals(MsoConfig.UPGRADE_MSG))
            	info.setUpgradeMessage(c.getValue());
            if (c.getItem().equals(MsoConfig.VIDEO)) {
                info.setTutorialVideo(c.getValue());
            }
            if (c.getItem().equals(MsoConfig.GCM_SENDER_ID) && os.equals(PlayerService.OS_ANDROID)) {
                info.setGcmSenderId(c.getValue());
            }
            if (c.getItem().equals(MsoConfig.ABOUT_US)) {
                info.setAboutus(c.getValue());
            }
        }
        CacheFactory.set(CacheFactory.getBrandInfoKey(mso, os, PlayerApiService.FORMAT_JSON), info);
        return info;    	
    }    

    private String checkOs(String os, HttpServletRequest req) {
        if (os != null) {
            if (!os.equals(PlayerService.OS_ANDROID) && !os.equals(PlayerService.OS_IOS)) {
                return PlayerService.OS_WEB;
            }
            return os;
        }
        ApiContext service = new ApiContext(req);
        os = PlayerService.OS_WEB;
        if (service.isIos()) {
            os = PlayerService.OS_IOS;
        } else if (service.isAndroid()) { 
            os = PlayerService.OS_ANDROID;
        }
        return os;
    }
    
    @SuppressWarnings("unchecked")
    public Object getBrandInfo(HttpServletRequest req, Mso mso, String os, short format, String locale, long counter, String piwik, String acceptLang) {
        if (mso == null) {return null; }
        os = checkOs(os, req);        
        String cacheKey = CacheFactory.getBrandInfoKey(mso, os, format);
        Object cached = null;
        try {
            cached = CacheFactory.get(cacheKey);
        } catch (Exception e) {
            log.info("memcache error");
        }
        if (format == PlayerApiService.FORMAT_JSON) {
            
            BrandInfo json = (BrandInfo) cached;
            if (cached == null) {
                log.info("plain text is not cached");
                json = (BrandInfo) this.composeBrandInfoJson(mso, os);
            }
            json.setLocale(locale);
            json.setBrandInfoCounter(counter);
            json.setPiwik(piwik);
            json.setAcceptLang(acceptLang);
            
            String ad = NNF.getConfigMngr().getAdConfig(mso, os);
            if (ad != null && ad.equals(MsoConfig.AD_DIRECT_VIDEO)) {
                
                String adKey = CacheFactory.getAdInfoKey(mso, format);
                List<AdPlacement> adPlacements = null;
                try {
                    adPlacements = (List<AdPlacement>) CacheFactory.get(adKey);
                } catch (Exception e) {
                    log.info("memcache error");
                }
                
                if (adPlacements == null) {
                    
                    log.info("json is not cached (adInfo)");
                    adPlacements = (List<AdPlacement>) composeAdInfo(mso, format);
                }
                
                json.setAdPlacements(adPlacements);
            }
            
            return json;
            
        } else {
            
            String brandInfo = (String) cached;
            if (cached == null) {
                log.info("plain text is not cached");
                brandInfo = this.composeBrandInfoStr(mso, os);
            }
            brandInfo += PlayerApiService.assembleKeyValue("locale", locale);
            brandInfo += PlayerApiService.assembleKeyValue("brandInfoCounter", String.valueOf(counter));
            brandInfo += PlayerApiService.assembleKeyValue("piwik", piwik);
            brandInfo += PlayerApiService.assembleKeyValue("acceptLang", acceptLang);
            
            String ad = NNF.getConfigMngr().getAdConfig(mso, os);
            if (ad != null && ad.equals(MsoConfig.AD_DIRECT_VIDEO)) {
                
                String adKey = CacheFactory.getAdInfoKey(mso, format);
                String adInfo = null;
                try {
                    adInfo = (String) CacheFactory.get(adKey);
                } catch (Exception e) {
                    log.info("memcache error");
                }
                
                if (adInfo == null) {
                    
                    log.info("plain text is not cached (adInfo)");
                    adInfo = (String) composeAdInfo(mso, format);
                }
                
                String[] plain = { brandInfo, adInfo };
                return plain;
                
            } else {
                
                String[] plain = { brandInfo };
                return plain;
            }
        }
    }
    
    private Object composeAdInfo(Mso mso, short format) {
        
        String adInfo = "";
        List<AdPlacement> ads = NNF.getAdMngr().findByMso(mso.getId());
        
        if (format == PlayerApiService.FORMAT_JSON) {
            
            CacheFactory.set(CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_JSON), ads);
            
            return ads;
        }
        
        for (AdPlacement ad : ads) {
            
            String[] ori = {
                  String.valueOf(ad.getId()),
                  String.valueOf(ad.getType()),
                  NnStringUtil.htmlSafeChars(ad.getName()),
                  ad.getUrl()
            };
            adInfo += NnStringUtil.getDelimitedStr(ori) + "\n";
        }
        
        CacheFactory.set(CacheFactory.getAdInfoKey(mso, PlayerApiService.FORMAT_PLAIN), adInfo);
        
        return adInfo;
    }
    
    public List<Mso> findByType(short type) {
        return dao.findByType(type);
    }
    
    public Mso findByName(String name, boolean extend) {
        
        if (name == null) return null;
        Mso mso = dao.findByName(name);
        if (mso == null) return null;
        
        if (extend) {
            
            return populateMso(mso);
        }
        
        return mso;
        
    }
    
    private Mso populateMso(Mso mso) {
        
        MsoConfig config = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.SUPPORTED_REGION);
        if (config == null) {
            mso.setSupportedRegion(null);
        } else {
            mso.setSupportedRegion(config.getValue());
        }
        
        config = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.FAVICON_URL);
        if (config != null) {
            mso.setJingleUrl(config.getValue());
        }
        
        return mso;
    }
    
    public Mso findByIdOrName(String idStr) {
        
        if (idStr == null) { return null; }
        
        return NnStringUtil.isDigits(idStr) ? dao.findById(idStr) : dao.findByName(idStr);
    }
    
    public Mso findByName(String name) {
        if (name == null) {return null;}
        Mso mso = dao.findByName(name);
        return mso;
    }
    
    public Mso getByNameFromCache(String name) {
        if (name == null || name.isEmpty()) {return null;}
        String cacheKey = "mso(" + name + ")";
        try {
            Mso cached = (Mso) CacheFactory.get(cacheKey);
            if (cached != null) {
                log.info("get mso object from cache:" + cached.getId());
                return cached;
            }
        } catch (Exception e) {
            log.info("memcache error");
        }        
        log.info("NOT get mso object from cache:" + name);
        Mso mso = findByIdOrName(name);
        CacheFactory.set(cacheKey, mso);
        return mso;
    }
    
    public Mso findById(long id) {
        return dao.findById(id);
    }
    
    /** rewrite method findById, populate supportedRegion information 
     * @param extend TODO*/
    public Mso findById(long id, boolean extend) {
        
        Mso mso = dao.findById(id);
        if (mso == null) {return null;}
        
        if (extend) {
            mso = populateMso(mso);
        }
        
        return mso;
    }
    
    public List<Mso> findAll() {
        return dao.findAll();
    }
    
    public List<Mso> list(int page, int limit, String sidx, String sord) {
        return dao.list(page, limit, sidx, sord);
    }
    
    public List<Mso> list(int page, int limit, String sidx, String sord, String filter) {
        return dao.list(page, limit, sidx, sord, filter);
    }
    
    public int total() {
        return dao.total();
    }
    
    public int total(String filter) {
        return dao.total(filter);
    }
    
    //TODO: to be removed
    /** indicate which brands that channel can play on, means channel is in the brand's store */
    public List<Mso> findValidMso(NnChannel channel) {
        
        if (channel == null) {
            return new ArrayList<Mso>();
        }
        
        List<Mso> valids = new ArrayList<Mso>();
        valids.add(getSystemMso()); // channel is always valid for brand 9x9
        
        if (channel.getStatus() == NnChannel.STATUS_SUCCESS &&
                channel.getContentType() != NnChannel.CONTENTTYPE_FAVORITE &&
                channel.isPublic() == true) {
            // the channel is in the official store
        } else {
            return valids;
        }
        
        MsoConfig supportedRegion = null;
        List<String> spheres = null;
        List<Mso> msos = findByType(Mso.TYPE_MSO);
        for (Mso mso : msos) {
            
            supportedRegion = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.SUPPORTED_REGION); // TODO : sql in the for loop
            if (supportedRegion == null) {
                valids.add(mso); // mso support all region
            } else {
                spheres = MsoConfigManager.parseSupportedRegion(supportedRegion.getValue());
                spheres.add(LangTable.OTHER);
                for (String sphere : spheres) {
                    if (sphere.equals(channel.getSphere())) { // this channel's sphere that MSO supported
                        valids.add(mso);
                        break;
                    }
                    // if not hit any of sphere, channel is not playable on this MSO, is not valid brand.
                }
            }
        }
        
        return valids;
    }
    
    /** indicate channel can or can't set brand for target MSO,
     *  9x9 is always a valid brand for auto-sharing even channel is not playable */
    public boolean isValidBrand(NnChannel channel, Mso mso) {
        
        if (channel == null || mso == null) {
            return false;
        }
        
        // 9x9 always valid
        if (mso.getType() == Mso.TYPE_NN) {
            return true;
        }
        
        // official store check
        if (channel.getStatus() == NnChannel.STATUS_SUCCESS &&
                channel.getContentType() != NnChannel.CONTENTTYPE_FAVORITE &&
                channel.isPublic() == true) {
            // the channel is in official store
        } else {
            return false; // the channel is not in official store
        }
        
        // support region check
        MsoConfig supportedRegion = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.SUPPORTED_REGION);
        if (supportedRegion == null) {
            return true; // Mso's region support all sphere
        } else {
            List<String> spheres = MsoConfigManager.parseSupportedRegion(supportedRegion.getValue());
            spheres.add(LangTable.OTHER);
            for (String sphere : spheres) {
                if (sphere.equals(channel.getSphere())) { // Mso's region support channel's sphere
                    log.info(mso.getName() + " is the valid brand of " + channel.getName());
                    return true;
                }
            }
            return false; // Mso's region not support channel's sphere
        }
    }
    
    /** Get playable channels on target MSO.
     *  The basic definition of playable should same as NnChannelDao.getStoreChannels. */
    public List<Long> getPlayableChannels(List<NnChannel> channels, Long msoId) {
        
        if (channels == null || channels.size() < 1 || msoId == null) {
            return new ArrayList<Long>();
        }
        
        Mso mso = findById(msoId, true);
        if (mso == null) {
            return new ArrayList<Long>();
        }
        
        List<String> supportSpheres;
        if (mso.getSupportedRegion() == null) {
            supportSpheres = null; // means support all sphere
        } else {
            supportSpheres = MsoConfigManager.parseSupportedRegion(mso.getSupportedRegion());
            supportSpheres.add(LangTable.OTHER);
        }
        
        List<Long> results = new ArrayList<Long>();
        for (NnChannel channel : channels) {
            
            // official store check
            if (channel.getStatus() == NnChannel.STATUS_SUCCESS &&
                    channel.getContentType() != NnChannel.CONTENTTYPE_FAVORITE &&
                    channel.isPublic() == true) {
                // the channel is in official store
            } else {
                continue; // the channel is not in official store
            }
            
            // MSO support region check
            if (supportSpheres == null) { // Mso's region support all sphere
                results.add(channel.getId());
            } else {
                for (String sphere : supportSpheres) {
                    if (sphere.equals(channel.getSphere())) { // Mso's region support channel's sphere
                        results.add(channel.getId());
                        break;
                    }
                }
            }
        }
        
        return results;
    }
    
    public boolean isPlayableChannel(NnChannel channel, Long msoId) {
        
        if (channel == null || msoId==null) {
            return false;
        }
        
        List<NnChannel> unverifiedChannels = new ArrayList<NnChannel>();
        unverifiedChannels.add(channel);
        
        List<Long> verifiedChannels = getPlayableChannels(unverifiedChannels, msoId);
        if (verifiedChannels != null && verifiedChannels.isEmpty() == false) {
            return true;
        }
        return false;
    }
    
    public static void normalize(Mso mso) {
        
        mso.setTitle(NnStringUtil.revertHtml(mso.getTitle()));
        mso.setIntro(NnStringUtil.revertHtml(mso.getIntro()));
        mso.setSupportedRegion(formatSupportedRegion(mso.getSupportedRegion()));
    }
    
    /** format supportedRegion of Mso to response format, ex : "en,zh,other" */
    private static String formatSupportedRegion(String input) {
        
        if (input == null) {
            return null;
        }
        
        List<String> spheres = MsoConfigManager.parseSupportedRegion(input);
        String supportedRegion = "";
        for (String sphere : spheres) {
            supportedRegion = supportedRegion + "," + sphere;
        }
        supportedRegion = supportedRegion.replaceFirst(",", "");
        
        String output = supportedRegion;
        if (output.equals("")) {
            return null;
        }
        return output;
    }
    
    public Mso findByPurchase(NnPurchase purchase) {
        
        NnItem item = NNF.getItemMngr().findById(purchase.getItemId());
        if (item != null) {
            
            return NNF.getMsoMngr().findById(item.getMsoId());
        }
        
        return null;
    }
}
