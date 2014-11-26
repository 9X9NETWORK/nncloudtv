package com.nncloudtv.model;

import java.util.Arrays;
import java.util.Locale;

import com.nncloudtv.exception.NnNotSupportedRegionException;

/**
 * Extend some day when needed. Id is always a good idea, country code etc  
 */
public class LocaleTable {
    
    public static final String LANG_ZH = "zh";
    public static final String LANG_EN = "en";
    public static final String LANG_OTHER = "other";
    
    public static final String REGION_US = "us";
    public static final String REGION_TW = "tw";
    public static final String REGION_GLOBAL = "global";
    
    public static final String EN_PROPERTIE_FILE = "messages_en_US.properties";
    public static final String ZH_PROPERTIE_FILE = "messages_zh_TW.properties";
    
    public static final String[] ALL_SUPPORTED_LANGUAGES = { LANG_ZH, LANG_EN, LANG_OTHER };
    public static final String[] ALL_SUPPORTED_REGIONS = { REGION_TW, REGION_US, REGION_GLOBAL};
    
    public static boolean isRegionSupported(String region) {
        
        if (region == null) return false;
        
        return Arrays.asList(ALL_SUPPORTED_REGIONS).contains(region);
    }
    
    public static boolean isLanguageSupported(String lang) {
        
        if (lang == null) return false;
        
        return Arrays.asList(ALL_SUPPORTED_LANGUAGES).contains(lang);
    }
    
    public static Locale getLocaleFromRegion(String region) {
        
        if (region != null && region.equalsIgnoreCase(REGION_TW))
            return Locale.TAIWAN;
        
        return Locale.US;
    }
    
    public static Locale getLocaleFromLang(String lang) {
        
        if (lang != null && lang.equalsIgnoreCase(LANG_ZH))
            return Locale.TRADITIONAL_CHINESE;
        
        return Locale.ENGLISH;
    }
    
    public static String sphere2region(String sphere) throws NnNotSupportedRegionException {
        
        if (sphere == null) return null;
        
        if (sphere.equalsIgnoreCase(LANG_EN)) {
            
            return REGION_US;
            
        } else if (sphere.equalsIgnoreCase(LANG_ZH)) {
            
            return REGION_TW;
            
        } else if (sphere.equalsIgnoreCase(LANG_OTHER)) {
            
            return REGION_GLOBAL;
            
        } else {
            
            throw new NnNotSupportedRegionException(sphere);
        }
    }
}
