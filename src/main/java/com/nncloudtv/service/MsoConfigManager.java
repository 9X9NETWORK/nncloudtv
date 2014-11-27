package com.nncloudtv.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.nncloudtv.dao.MsoConfigDao;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.web.api.ApiContext;

@Service
public class MsoConfigManager {
    
    static MsoConfigDao configDao = new MsoConfigDao();
    protected static final Logger log = Logger.getLogger(MsoConfigManager.class.getName());
    
    protected static final String PROPERTIES_CLEARCOMMRCE = "clearcommerce.properties";
    protected static final String PROPERTIES_AWS          =           "aws.properties";
    protected static final String PROPERTIES_GOOGLEPLAY   =    "googleplay.properties";
    protected static final String PROPERTIES_APPSTORE     =      "appstore.properties";
    
    protected static String serverDomain = null;
    
    protected static String getProperty(String file, String name) {
        
        Properties properties = new Properties();
        String result = null;
        log.info("get property " + name + " from " + file);
        try {
            properties.load(MsoConfigManager.class.getClassLoader().getResourceAsStream(file));
            result = properties.getProperty(name);
        } catch (IOException e) {
            NnLogUtil.logException(e);
        }
        return result;
    }
    
    public static String getGooglePlayAccountEmail() {
        return getProperty(PROPERTIES_GOOGLEPLAY, "account_email");
    }
    
    public static String getGooglePlayPemFilePath() {
        return getProperty(PROPERTIES_GOOGLEPLAY, "pem_file_path");
    }
    
    public static String getAppStoreSharedSecret(Mso mso) {
        
        if (mso != null) {
            MsoConfig config = NNF.getConfigMngr().getByMsoAndItem(mso, MsoConfig.APPSTORE_SHARED_SECRET);
            if (config != null) {
                return config.getValue();
            }
        }
        
        return getProperty(PROPERTIES_APPSTORE, "shared_secret");
    }
    
    public static String getAppStoreBundleId(Mso mso) {
        
        if (mso != null) {
            MsoConfig config = NNF.getConfigMngr().getByMsoAndItem(mso, MsoConfig.APPSTORE_BUNDLE_ID);
            if (config != null) {
                return config.getValue();
            }
        }
        
        return getProperty(PROPERTIES_APPSTORE, "bundle_id");
    }
    
    public static String getGooglePlayPackageName(Mso mso) {
        
        if (mso != null) {
            MsoConfig config = NNF.getConfigMngr().getByMsoAndItem(mso, MsoConfig.GOOGLEPLAY_PACKAGE_NAME);
            if (config != null) {
                return config.getValue();
            }
        }
        
        return getProperty(PROPERTIES_GOOGLEPLAY, "package_name");
    }
    
    public static String getGooglePlayAppName(Mso mso) {
        
        if (mso != null) {
            MsoConfig config = NNF.getConfigMngr().getByMsoAndItem(mso, MsoConfig.GOOGLEPLAY_APP_NAME);
            if (config != null) {
                return config.getValue();
            }
        }
        
        return getProperty(PROPERTIES_GOOGLEPLAY, "app_name");
    }
    
    public static String getSearchNnChannelServer() {
        return getProperty("services.properties", "search_nncloudtv");
    }
    
    public static String getSearchPoolServer() {
        return getProperty("services.properties", "search_pool");
    }
   
    public static String getValue(String key) {
        return getProperty("services.properties", key);
    }
    
    public static String getP12FilePath(Mso mso, boolean isProduction) {
        
        String path = "/var/opt/p12files/";
        
        if (isProduction) {
            return path + mso.getName() + "_apns.p12";
        } else {
            return path + mso.getName() + "_apns_dev.p12";
        }
    }
    
    static public String getCFPrivateKeyPath(Mso mso) {
        
        String msoName = (mso == null) ? NNF.getMsoMngr().getSystemMsoName() : mso.getName();
        String path = "/var/opt/p12files/";
        return path + "rsa-key-" + msoName + ".der";
    }
    
    public static String getServerDomain() {
        
        if (serverDomain == null) {
            serverDomain = getProperty("facebook.properties", "server_domain");
        }
        return serverDomain;
    }
    
    static public String getFacebookAppToken() {
        return getProperty("facebook.properties", "facebook_apptoken");
    }
    
    public static String getCrawlerDomain() {
        return getProperty("crawler.properties", "server");
    }
    
    public String getFacebookInfo(String type, Mso mso) {
        if (mso == null || type == null) {
            return null;
        }
        MsoConfig config = this.findByMsoAndItem(mso, type);
        if (config != null) {
            return config.getValue();
        }
        if (type == MsoConfig.FACEBOOK_CLIENTID)
            return getProperty("facebook.properties", "facebook_clientid");
        if (type == MsoConfig.FACEBOOK_APPTOKEN)
            return getProperty("facebook.properties", "facebook_apptoken");
        if (type == MsoConfig.FACEBOOK_CLIENTSECRET)
            return getProperty("facebook.properties", "facebook_client_secret");
        return null;
    }
    
    static public String getFacebookClientId() {
        return getProperty("facebook.properties", "facebook_clientid");
    }
    
    static public String getFacebookClientSecret() {
        
        return getProperty("facebook.properties", "facebook_client_secret");
    }
    
    static public String getExternalRootPath() {
        
        return getProperty("aws.properties", "static_file_root_path");
    }
    
    public MsoConfig create(MsoConfig config) {
        Date now = NnDateUtil.now();
        config.setCreateDate(now);
        config.setUpdateDate(now);
        return configDao.save(config);
    }
    
    public MsoConfig save(Mso mso, MsoConfig config) {
        config.setUpdateDate(NnDateUtil.now());
        if (mso.getType() == Mso.TYPE_NN)
            processCache(config);
        return configDao.save(config);
    }
    
    public void processCache(MsoConfig config) {
        isInReadonlyMode(true);
        isQueueEnabled(true);
    }
    
    public String getDefaultValueByOs(String os, String function) {
        if (os == null || function == null)
            return null;
        if (function.contains("flurry")) {
            if (os.equals(ApiContext.OS_IOS)) 
                return "J6GPGNMBR7GRDJVSCCN8";
            if (os.equals(ApiContext.OS_ANDROID))
                return "CJGQT59JKHN4MWBQFXZN";
        }
        if (function.contains("google")) {
            if (os.equals(ApiContext.OS_IOS)) 
                return "UA-47454448-3";
            if (os.equals(ApiContext.OS_ANDROID))
                return "UA-47454448-2";
            if (os.equals(ApiContext.OS_WEB)) {
                return "UA-47454448-1";
            }
        }
        if (function.contains("youtube")) {
            if (os.equals(ApiContext.OS_ANDROID))
                return "AI39si5HrNx2gxiCnGFlICK4Bz0YPYzGDBdJHfZQnf-fClL2i7H_A6Fxz6arDBriAMmnUayBoxs963QLxfo-5dLCO9PCX-DTrA";
        }
        if (function.contains("notify")) {
            if (os.equals(ApiContext.OS_ANDROID))
                return "758834427689";
        }
        return null;
    }
    
    //used for device dependant key name. currently flurry and ga and youtube
    public String getKeyNameByOs(String os, String function) {
        if (os == null || function == null)
            return null;
        if (function.contains("flurry")) {
            if (os.equals(ApiContext.OS_IOS))
                return MsoConfig.FLURRY_ANALYTICS_IOS;
            if (os.equals(ApiContext.OS_ANDROID))
                return MsoConfig.FLURRY_ANALYTICS_ANDROID;
        }
        if (function.contains("google")) {
            if (os.equals(ApiContext.OS_IOS))
                return MsoConfig.GOOGLE_ANALYTICS_IOS;
            if (os.equals(ApiContext.OS_ANDROID))
                return MsoConfig.GOOGLE_ANALYTICS_ANDROID;
            if (os.equals(ApiContext.OS_WEB)) {
                return MsoConfig.GOOGLE_ANALYTICS_WEB;
            }
        }
        if (function.contains("youtube")) {
            if (os.equals(ApiContext.OS_ANDROID))
                return MsoConfig.YOUTUBE_ID_ANDROID;
        }
        if (function.equals("ad")) {
            if (os.equals(ApiContext.OS_ANDROID))
                return MsoConfig.AD_ANDROID;
            if (os.equals(ApiContext.OS_IOS))
                return MsoConfig.AD_IOS;
        }
        if (function.contains("admobkey")) {
            if (os.equals(ApiContext.OS_ANDROID))
                return MsoConfig.ADMOBKEY_ANDROID;
            if (os.equals(ApiContext.OS_IOS))
                return MsoConfig.ADMOBKEY_IOS;
        }
        return null;
    }
    
    public boolean getBooleanValueFromCache(String key, boolean cacheReset) {
        String cacheKey = "msoconfig(" + key + ")";
        try {
            String result = (String)CacheFactory.get(cacheKey);
            if (result != null){
                log.info("value from cache: key=" + cacheKey + "value=" + result);
                return NnStringUtil.stringToBool(result);
            }
        } catch (Exception e) {
            log.info("memcache error");
        }
        boolean value = false;
        MsoConfig config = configDao.findByItem(key);
        if (config != null) {
            CacheFactory.set(cacheKey, config.getValue());
            value = NnStringUtil.stringToBool(config.getValue());
        }
        return value;
    }
    
    public boolean isInReadonlyMode(boolean cacheReset) {
        return this.getBooleanValueFromCache(MsoConfig.RO, cacheReset);
    }
    
    public boolean isQueueEnabled(boolean cacheReset) {
        boolean status = this.getBooleanValueFromCache(MsoConfig.QUEUED, cacheReset);     
        return status;
    }
    
    public List<MsoConfig> findByMso(Mso mso) {
        return configDao.findByMso(mso);
    }
    
    //find: access db directly; get: through cache
    public MsoConfig getByMsoAndItem(Mso mso, String item) {
        
        String cacheKey = CacheFactory.getMsoConfigKey(mso.getId(), item);
        try {
            MsoConfig result = (MsoConfig) CacheFactory.get(cacheKey);
            if (result != null){
                if (result.getValue() == null || result.getValue().isEmpty()) {
                    log.fine("empty config from cache: key=" + cacheKey);
                    return null;
                } else {
                    log.fine("value from cache: key=" + cacheKey + ", value=" + result.getValue());
                    return result;
                }
            }    
        } catch (Exception e) {
            log.info("memcache error");
        }
        MsoConfig config = findByMsoAndItem(mso, item);
        if (config != null) {
            log.info("set value to cache: key=" + cacheKey + ", value=" + config.getValue());
            CacheFactory.set(cacheKey, config);
        } else {
            log.info("set empty config to cache: key=" + cacheKey);
            CacheFactory.set(cacheKey, new MsoConfig());
        }
        return config;
        
    }
    
    public MsoConfig findByMsoAndItem(Mso mso, String item) {
        return configDao.findByMsoAndItem(mso.getId(), item);
    }
    
    public MsoConfig findByItem(String item) {
        return configDao.findByItem(item);
    }
    
    public static List<String> parseCategoryMask(String systemCategoryMask) {
        
        if (systemCategoryMask == null) {
            return new ArrayList<String>();
        }
        
        String[] systemCategoryLocks = systemCategoryMask.split(",");
        if (systemCategoryLocks.length == 1 && systemCategoryLocks[0].equals("")) {
            return new ArrayList<String>();
        }
        
        List<String> results = new ArrayList<String>();
        for (String systemCategoryLock : systemCategoryLocks) {
            results.add(systemCategoryLock);
        }
        
        return results;
    }
    
    private static List<String> validateMasks(List<String> masks) {
        
        if (masks == null || masks.size() < 1) {
            return new ArrayList<String>();
        }
        
        // populate System's CategoryIds
        List<SysTag> systemCategories = NNF.getSysTagMngr().findByMsoIdAndType(MsoManager.getSystemMsoId(), SysTag.TYPE_CATEGORY);
        Map<Long, Long> systemCategoryIds = new TreeMap<Long, Long>();
        for (SysTag systemCategory : systemCategories) {
            systemCategoryIds.put(systemCategory.getId(), systemCategory.getId());
        }
        
        List<String> verifiedLocks = new ArrayList<String>();
        for (String lock : masks) {
            
            Long categoryId = null;
            try {
                categoryId = Long.valueOf(lock);
            } catch (NumberFormatException e) {
                // special lock for lock all System Category
                if (lock.equals(MsoConfig.DISABLE_ALL_SYSTEM_CATEGORY)) {
                    verifiedLocks.add(MsoConfig.DISABLE_ALL_SYSTEM_CATEGORY);
                }
            }
            
            if (categoryId != null) {
                if (systemCategoryIds.containsKey(categoryId)) {
                    verifiedLocks.add(lock);
                }
            }
        }
        
        return verifiedLocks;
    }
    
    // DB first, property file as fallback
    public static String getMemcacheServer() {
        
        String result = null;
        MsoConfig config = configDao.findByItem(MsoConfig.MEMCACHE_SERVER);
        if (config != null) {
            result = config.getValue();
        }
        if (result == null || result.isEmpty()) {
            try {
                Properties properties = new Properties();
                properties.load(MsoConfigManager.class.getClassLoader().getResourceAsStream("memcache.properties"));
                result = properties.getProperty("server");
            } catch (IOException e) {
                log.warning(e.getMessage());
            }
        }
        return result;
    }
    
    static public String getCFKeyPairId() {
        
        return getProperty(PROPERTIES_AWS, "cf_key_pair_id");
    }
    
    static public String getS3UploadBucket() {
        
        return getProperty(PROPERTIES_AWS, "s3_upload_bucket");
    }
    
    public String getS3UploadBucket(Mso mso) {
        
        MsoConfig config = findByMsoAndItem(mso, MsoConfig.S3_UPLOAD_BUCKET);
        if (config != null) {
            return config.getValue();
        }
        return getS3UploadBucket();
    }
    
    public String getS3VideoBucket(Mso mso) {
        
        MsoConfig config = findByMsoAndItem(mso, MsoConfig.S3_VIDEO_BUCKET);
        if (config != null) {
            return config.getValue();
        }
        return getS3UploadBucket();
    }
    
    public static String getS3DepotBucket() {
        
        return getProperty(PROPERTIES_AWS, "s3_depot_bucket");
    }
    
    public static String getAWSId() {
        
        return getProperty(PROPERTIES_AWS, "aws_id");
    }
    
    public static String getAWSId(Mso mso) {
        
        if (mso != null) {
            MsoConfig config = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.AWS_ID);
            if (config != null) {
                return config.getValue();
            }
        }
        
        return getAWSId();
    }
    
    public static String getAWSKey() {
        
        return getProperty(PROPERTIES_AWS, "aws_key");
    }
    
    public static String getAWSKey(Mso mso) {
        
        if (mso != null) {
            MsoConfig config = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.AWS_KEY);
            if (config != null) {
                return config.getValue();
            }
        }
        
        return getAWSKey();
    }
    
    public static String getCCClientId() {
        
        return getProperty(PROPERTIES_CLEARCOMMRCE, "client_id");
    }
    
    public static String getCCBillingGayeway() {
        
        return getProperty(PROPERTIES_CLEARCOMMRCE, "billing_gateway");
    }
    
    public static String getCCUserName() {
        
        return getProperty(PROPERTIES_CLEARCOMMRCE, "user_name");
    }
    
    public static String getCCPassword() {
        
        return getProperty(PROPERTIES_CLEARCOMMRCE, "password");
    }
    
    public static String getCCPort() {
        
        return getProperty(PROPERTIES_CLEARCOMMRCE, "port");
    }
    
    /**
     * Get system Category locks setting from MSO store.
     * @param msoId required, MSO ID
     * @return system Category locks
     */
    public List<String> getCategoryMasks(long msoId) {
        
        List<String> empty = new ArrayList<String>();
        
        Mso mso = NNF.getMsoMngr().findById(msoId);
        
        if (mso != null) {
            
            MsoConfig mask = findByMsoAndItem(mso, MsoConfig.SYSTEM_CATEGORY_MASK);
            if (mask != null) {
                
                return parseCategoryMask(mask.getValue());
            }
        }
        
        return empty;
    }
    
    public List<String> setCategoryMasks(long msoId, List<String> masks) {
        
        List<String> empty = new ArrayList<String>();
        
        Mso mso = NNF.getMsoMngr().findById(msoId);
        
        if (mso != null) {
            
            MsoConfig config = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.SYSTEM_CATEGORY_MASK);
            if (config == null) {
                config = new MsoConfig();
                config.setMsoId(mso.getId());
                config.setItem(MsoConfig.SYSTEM_CATEGORY_MASK);
                config.setValue("");
                config = NNF.getConfigMngr().create(config);
            }
            
            if (masks == null || masks.isEmpty()) {
                config.setValue("");
            } else {
                config.setValue(StringUtils.join(validateMasks(masks), ","));
            }
            
            config = NNF.getConfigMngr().save(mso, config);
            
            return MsoConfigManager.parseCategoryMask(config.getValue());
        }
        
        return empty;
    }
    
    public String getAdConfig(Mso mso, String os) {
        
        String adKeyName = getKeyNameByOs(os, "ad");
        if (adKeyName != null) {
            MsoConfig adConfig = findByMsoAndItem(mso, adKeyName);
            String ad = "off";
            if (adConfig != null)
                ad = adConfig.getValue();
            return ad;
        }
        return null;
    }
    
    public Mso findMsoByVideoBucket(String bucket) {
        
        MsoConfig config = configDao.findByItemAndValue(MsoConfig.S3_VIDEO_BUCKET, bucket);
        if (config != null) {
            
            return NNF.getMsoMngr().findById(config.getMsoId());
        }
        return null;
    }
    
    public static List<String> getSuppoertedRegion(Mso mso, boolean appendOther) {
        List<String> empty = new ArrayList<String>();
        if (mso == null) return empty;
        MsoConfig config = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.SUPPORTED_REGION);
        if (config != null) {
            List<String> regions = NnStringUtil.parseRegion(config.getValue(), appendOther);
            if (regions != null && !regions.isEmpty())
                return regions;
        }
        return empty;
    }
}

