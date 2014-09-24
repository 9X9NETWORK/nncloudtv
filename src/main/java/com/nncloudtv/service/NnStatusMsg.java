package com.nncloudtv.service;

import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.web.api.NnStatusCode;

/**
 * Transition implementation before we can switch to real locale instead of mso.
 * 
 * Shortcut to retrieve messages that are used often.  
 * 
 */
@Service
public class NnStatusMsg {
    //private static MessageSource messageSource = new ClassPathXmlApplicationContext("locale.xml";
    
    public static String assembleStrMsg(int statusCode, String msg) {
        return statusCode + "\t" + msg + "\n";
    }
        
    public static String getPlayerMsgText(int status) {
        try {
            switch (status) {
                case NnStatusCode.SUCCESS: return "SUCCESS";
                case NnStatusCode.INFO: return "INFO";
                case NnStatusCode.WARNING: return "WARNING";
                case NnStatusCode.FATAL: return "FATAL";
                case NnStatusCode.ERROR: return "ERROR";
                
                case NnStatusCode.API_DEPRECATED: return "THIS FEATURE IS TEMPORARILY DISABLED";
                case NnStatusCode.API_UNDER_CONSTRUCTION: return "API_UNDER_CONSTRUCTION";
                case NnStatusCode.API_FORCE_UPGRADE: return MsoConfig.FORCE_UPGRADE;
                case NnStatusCode.APP_EXPIRE: return "APP_EXPIRE";
                case NnStatusCode.APP_VERSION_EXPIRE: return "APP_VERSION_EXPIRE";
                case NnStatusCode.INPUT_ERROR: return "INPUT_ERROR";
                case NnStatusCode.INPUT_MISSING: return "INPUT_MISSING";
                case NnStatusCode.INPUT_BAD: return "INPUT_BAD";
                case NnStatusCode.CAPTCHA_FAILED: return "CAPTCHA_FAILED";
                case NnStatusCode.CAPTCHA_EXPIRED: return "CAPTCHA_EXPIPRED";
                case NnStatusCode.CAPTCHA_TOOMANY_TRIES: return "CAPTCHA_TOOMANY_TRIES";
                case NnStatusCode.CAPTCHA_INVALID: return "CAPTCHA_INVALID";
                case NnStatusCode.CAPTCHA_ERROR: return "CAPTCHA_ERROR";
                
                case NnStatusCode.PIWIK_INVALID: return "PIWIK_INVALID";
                case NnStatusCode.PIWIK_ERROR: return "PIWIK_ERROR";
                
                case NnStatusCode.POI_INVALID: return "POI_INVALID";
                case NnStatusCode.POI_DUPLICATED: return "POI_DUPLICATED";
                
                case NnStatusCode.DEVICE_INVALID: return "DEVICE_INVALID";
                
                case NnStatusCode.OUTPUT_NO_MSG_DEFINED: return "OUTPUT_NO_MSG_DEFINED";
                
                case NnStatusCode.DATA_ERROR: return "DATA_ERROR";
                
                case NnStatusCode.USER_ERROR: return "USER_ERROR";
                case NnStatusCode.USER_LOGIN_FAILED: return "USER_LOGIN_FAILED";
                case NnStatusCode.USER_EMAIL_TAKEN: return "USER_EMAIL_TAKEN";
                case NnStatusCode.USER_INVALID: return "USER_INVALID";
                case NnStatusCode.USER_TOKEN_TAKEN: return "USER_TOKEN_TAKEN"; 
                case NnStatusCode.USER_PERMISSION_ERROR: return "USER_PERMISSION_ERROR";
                case NnStatusCode.ACCOUNT_INVALID: return "ACCOUNT_INVALID";
                case NnStatusCode.INVITE_INVALID: return "INVITE_INVALID";
                
                case NnStatusCode.MSO_ERROR: return "MSO_ERROR";
                case NnStatusCode.MSO_INVALID: return "MSO_INVALID";
                
                case NnStatusCode.CHANNEL_ERROR: return "CHANNEL_ERROR";
                case NnStatusCode.CHANNEL_URL_INVALID: return "CHANNEL_URL_INVALID";
                case NnStatusCode.CHANNEL_INVALID: return "CHANNEL_INVALID";
                case NnStatusCode.CHANNEL_OR_USER_INVALID: return "CHANNEL_OR_USER_INVALID";
                case NnStatusCode.CHANNEL_STATUS_ERROR: return "CHANNEL_STATUS_ERROR";
                case NnStatusCode.CHANNEL_MAXSIZE_EXCEEDED: return "CHANNEL_MAXSIZE_EXCEEDED";
                case NnStatusCode.CHANNEL_YOUTUBE_NOT_AVAILABLE: return "CHANNEL_YOUTUBE_NOT_AVAILABLE";
                
                case NnStatusCode.PROGRAM_ERROR: return "PROGRAM_ERROR";
                case NnStatusCode.PROGRAM_INVALID: return "PROGRAM_INVALID";
                
                case NnStatusCode.SUBSCRIPTION_ERROR: return "SUBSCRIPTION_ERROR";
                case NnStatusCode.SUBSCRIPTION_DUPLICATE_CHANNEL: return "SUBSCRIPTION_DUPLICATE_CHANNEL";
                case NnStatusCode.SUBSCRIPTION_SET_OCCUPIED: return "SUBSCRIPTION_SET_OCCUPIED";
                case NnStatusCode.SUBSCRIPTION_DUPLICATE_SET: return "SUBSCRIPTION_DUPLICATE_SET";
                case NnStatusCode.SUBSCRIPTION_RO_SET: return "SUBSCRIPTION_RO_SET";
                case NnStatusCode.SUBSCRIPTION_POS_OCCUPIED: return "SUBSCRIPTION_POS_OCCUPIED";
                
                case NnStatusCode.CATEGORY_ERROR: return "CATEGORY_ERROR";
                case NnStatusCode.CATEGORY_INVALID: return "CATEGORY_INVALID";
                
                case NnStatusCode.IPG_ERROR: return "IPG_ERROR";
                case NnStatusCode.IPG_INVALID: return "IPG_INVALID";
                
                case NnStatusCode.SET_ERROR: return "SET_ERROR";
                case NnStatusCode.SET_INVALID: return "SET_INVALID";
                
                case NnStatusCode.TAG_ERROR: return "TAG_ERROR";
                case NnStatusCode.TAG_INVALID: return "TAG_INVALID";
                
                case NnStatusCode.SERVER_ERROR: return "SERVER_ERROR";
                case NnStatusCode.SERVER_TIMEOUT: return "SERVER_TIMEOUT";
                
                case NnStatusCode.DATABASE_ERROR: return "DATABASE_ERROR";
                case NnStatusCode.DATABASE_TIMEOUT: return "DATABASE_TIMEOUT";
                case NnStatusCode.DATABASE_NEED_INDEX: return "DATABASE_NEED_INDEX";
                case NnStatusCode.DATABASE_READONLY: return "DATABASE_READONLY";
                default: return "MESSAGE_UNDEFINED";
            }
        } catch (NoSuchMessageException e) {
            return "MESSAGE_UNDEFINED";
        }
    }
    
    public static String getPlayerMsg(int status) {
    	return NnStatusMsg.assembleStrMsg(status, getPlayerMsgText(status));
    }
}
