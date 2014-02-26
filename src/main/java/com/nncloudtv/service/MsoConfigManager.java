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
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.SysTag;

@Service
public class MsoConfigManager {
    
    static MsoConfigDao configDao = new MsoConfigDao();
    protected static final Logger log = Logger.getLogger(MsoConfigManager.class.getName());
    
    protected static String serverDomain = null; 
    
    protected static String getProperty(String propertyFile, String propertyName) {
        
        Properties properties = new Properties();
        String result = null;
        log.info("to get property " + propertyName + " from " + propertyFile);
        try {
            properties.load(MsoConfigManager.class.getClassLoader().getResourceAsStream(propertyFile));
            result = properties.getProperty(propertyName);
        } catch (IOException e) {
            NnLogUtil.logException(e);
        }
        return result;
    }
    
    static public String getSearchServer() {
        
    	return getProperty("services.properties", "search");
    }
    
    static public String getP12FilePath(Mso mso) {
        
        return "/var/opt/p12files/" + mso.getName() + "_apns.p12";
    }
    
    static public String getS3UploadBucket() {
        
        return getProperty("aws.properties", "s3_upload_bucket");
    }
    
    static public String getPiwikDomain() {
        
        return getProperty("piwik.properties", "piwik_server");
    }
    
    static public String getServerDomain() {
        
        if (serverDomain == null) {
            serverDomain = getProperty("facebook.properties", "server_domain");
        }
        return serverDomain;
    }
        
    static public String getFacebookAppToken() {        
        return getProperty("facebook.properties", "facebook_apptoken");
    }
    
    static public String getCrawlerDomain() {
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
        Date now = new Date();
        config.setCreateDate(now);
        config.setUpdateDate(now);
        return configDao.save(config);
    }
    
    public MsoConfig save(Mso mso, MsoConfig config) {
        config.setUpdateDate(new Date());
        if (mso.getType() == Mso.TYPE_NN) {
            this.processCache(config);
        }
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
            if (os.equals(PlayerService.OS_IOS)) 
                return "J6GPGNMBR7GRDJVSCCN8";
            if (os.equals(PlayerService.OS_ANDROID))
                return "CJGQT59JKHN4MWBQFXZN";
        }
        if (function.contains("google")) {
            if (os.equals(PlayerService.OS_IOS)) 
                return "UA-47454448-3";
            if (os.equals(PlayerService.OS_ANDROID))
                return "UA-47454448-2";
            if (os.equals(PlayerService.OS_WEB)) {
                return "UA-47454448-1";
            }
        }
        if (function.contains("youtube")) {
            if (os.equals(PlayerService.OS_ANDROID))
                return "AI39si5HrNx2gxiCnGFlICK4Bz0YPYzGDBdJHfZQnf-fClL2i7H_A6Fxz6arDBriAMmnUayBoxs963QLxfo-5dLCO9PCX-DTrA";
        }
        return null;        
    }
    
    //used for device dependant key name. currently flurry and ga and youtube
    public String getKeyNameByOs(String os, String function) {
        if (os == null || function == null)
            return null;
        if (function.contains("flurry")) {
            if (os.equals(PlayerService.OS_IOS)) 
                return MsoConfig.FLURRY_ANALYTICS_IOS;
            if (os.equals(PlayerService.OS_ANDROID))
                return MsoConfig.FLURRY_ANALYTICS_ANDROID;
        }
        if (function.contains("google")) {
            if (os.equals(PlayerService.OS_IOS)) 
                return MsoConfig.GOOGLE_ANALYTICS_IOS;
            if (os.equals(PlayerService.OS_ANDROID))
                return MsoConfig.GOOGLE_ANALYTICS_ANDROID;
            if (os.equals(PlayerService.OS_WEB)) {
                return MsoConfig.GOOGLE_ANALYTICS_WEB;
            }
        }
        if (function.contains("youtube")) {
            if (os.equals(PlayerService.OS_ANDROID))
                return MsoConfig.YOUTUBE_ID_ANDROID;
        }
        return null;
    }
    
    public String getCacheKeyByMsoAndKey(long msoId, String key) {
        String cacheKey = "msoconfig(" + msoId + ")(" + key + ")";
        return cacheKey;
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
            
    public MsoConfig findByMsoAndItem(Mso mso, String item) {
        return configDao.findByMsoAndItem(mso.getId(), item);
    }
    
    public MsoConfig findByItem(String item) {
        return configDao.findByItem(item);
    }
    
    /** parse supportedRegion to list of sphere that mso can supported */
    public static List<String> parseSupportedRegion(String supportedRegion) {
        
        if (supportedRegion == null) {
            return new ArrayList<String>();
        }
        
        List<String> spheres = new ArrayList<String>();
        String[] pairs = supportedRegion.split(";");
        for (String pair : pairs) {
            String[] values = pair.split(" ");
            if (values[0].equals(LangTable.LANG_EN)) {
                spheres.add(LangTable.LANG_EN);
            }
            if (values[0].equals(LangTable.LANG_ZH)) {
                spheres.add(LangTable.LANG_ZH);
            }
            if (values[0].equals(LangTable.OTHER)) {
                spheres.add(LangTable.OTHER);
            }
        }
        
        return spheres;
    }
    
    public static List<String> parseSystemCategoryMask(String systemCategoryMask) {
        
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
    
    public static String composeSystemCategoryMask(List<String> systemCategoryLocks) {
        
        if (systemCategoryLocks == null || systemCategoryLocks.size() < 1) {
            return "";
        }
        
        return StringUtils.join(systemCategoryLocks, ",");
    }
    
    public static List<String> verifySystemCategoryLocks(List<String> systemCategoryLocks) {
        
        if (systemCategoryLocks == null || systemCategoryLocks.size() < 1) {
            return new ArrayList<String>();
        }
        
        // populate System's CategoryIds
        MsoManager msoMngr = new MsoManager();
        SysTagManager sysTagMngr = new SysTagManager();
        List<SysTag> systemCategories = sysTagMngr.findByMsoIdAndType(msoMngr.findNNMso().getId(), SysTag.TYPE_CATEGORY);
        Map<Long, Long> systemCategoryIds = new TreeMap<Long, Long>();
        for (SysTag systemCategory : systemCategories) {
            systemCategoryIds.put(systemCategory.getId(), systemCategory.getId());
        }
        
        List<String> verifiedLocks = new ArrayList<String>();
        for (String lock : systemCategoryLocks) {
            
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
}

