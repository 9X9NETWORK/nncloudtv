package com.nncloudtv.lib;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannelPref;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.NnChannelPrefManager;
import com.nncloudtv.web.api.ApiContext;

public class NnStringUtil {
    
    public static final String UTF8 = "UTF-8";
    public static final String ASCII = "US-ASCII";
    
    protected static final Logger log = Logger.getLogger(NnStringUtil.class.getName());    
    public static final int MAX_JDO_STRING_LENGTH = 255;
    
    private static NnChannelPrefManager channelPrefMngr = new NnChannelPrefManager();
    
    public static void setChannelPrefMngr(NnChannelPrefManager mngr) {
        channelPrefMngr = mngr;
    }
    
    public static boolean stringToBool(String s) {
      if (s.equals("1"))
        return true;
      if (s.equals("0"))
        return false;
      throw new IllegalArgumentException(s +" is not a bool");
    }
    
    public static String urlencode(String text) {
        
        return urlencode(text, UTF8);
    }
    
    public static String urlencode(String text, String charset) {
        
        if (text == null)
            return null;
        String str = "";
        try {
            str = URLEncoder.encode(text, charset).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }
    
    public static String capitalize(String str) {
        if (str == null) {return null;}
        str = str.toLowerCase();
        String firstLetter = str.substring(0,1);
        String remainder   = str.substring(1);
        String capitalized = firstLetter.toUpperCase() + remainder.toLowerCase();
        return capitalized;
    }

    /**
     * To to truncate an UTF-8 string to fit proper bytes
     *
     * source: http://stackoverflow.com/questions/119328/how-do-i-truncate-a-java-string-to-fit-in-a-given-number-of-bytes-once-utf-8-enc
     * @throws UnsupportedEncodingException 
     */
    public static String truncateUTF8(String str, int maxBytes) {
        if (str == null) { return null; }
        Charset utf8 = Charset.forName(UTF8);
        int totalBytes = str.getBytes(utf8).length;
        if (totalBytes <= maxBytes) { return str; }
        for (int i = 0, b = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            
            // ranges from http://en.wikipedia.org/wiki/UTF-8
            int skip = 0;
            int more;
            if (c <= 0x007f) {
                more = 1;
            }
            else if (c <= 0x07FF) {
                more = 2;
            } else if (c <= 0xd7ff) {
                more = 3;
            } else if (c <= 0xDFFF) {
                // surrogate area, consume next char as well
                more = 4;
                skip = 1;
            } else {
                more = 3;
            }
            
            if (b + more > maxBytes) {
                String truncStr = str.substring(0, i);
                int truncBytes = truncStr.getBytes(utf8).length;
                log.info("truncate string length " + str.length() + " (" + totalBytes + " bytes) --> " + i + " (" + truncBytes + " bytes)");
                return truncStr;
            }
            b += more;
            i += skip;
        }
        return str;
    }
    
    public static String getDateString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm a");
        String result = dateFormat.format(date);
        System.out.println("getUpdateDate() : UDate = " + result);
        return result;
    }
    
    public static String getDelimitedStr(String[] ori) {
        StringBuilder result = new StringBuilder();
        String delimiter = "\t";
        for (String str : ori) {
            if (str != null) {
                str = str.replaceAll("\\t", " ");
            }
        }
        if (ori.length > 0) {
            result.append(ori[0]);
            for (int i = 1; i < ori.length; i++) {
               result.append(delimiter);
               result.append(ori[i]);
            }
        }
        return result.toString();
    }
    
    public static String escapedQuote(String str) {
        
        return "'" + str.replaceAll("'", "''")
                         .replaceAll("\"", "\\\"")
                         .replaceAll("\\?", "")
                         .replaceAll("\\\\", "\\\\\\\\") + "'";
    }
    
    public static String bytesToHex(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    
    public static String revertHtml(String str) {
        if (str == null) return null;
        return str.replaceAll("&quot;", "\"")
                  .replaceAll("&gt;",   ">")
                  .replaceAll("&lt;",   "<")
                  .replaceAll("&amp;",  "&");
    }
    
    public static String htmlSafeAndTruncated(String str) {
        return htmlSafeAndTruncated(str, MAX_JDO_STRING_LENGTH);
    }
    
    public static String htmlSafeAndTruncated(String str, int length) {
        
        if (str == null || str.length() == 0) {
            return str;
        }
        
        for (int i = length; i > 0; i--) {
            String truncated = truncateUTF8(str, i);
            String htmlSafe = htmlSafeChars(truncated);
            Integer bytelen = htmlSafe.getBytes(Charset.forName(UTF8)).length;
            if (bytelen <= length) {
                log.info("truncated length = " + bytelen + " bytes (limit is " + length + ")");
                return htmlSafe;
            }
        }
        return null;
    }
    
    /**
     * Translate string to html safe characters
     * 
     * @param str
     * @return
     */
    public static String htmlSafeChars(String str) {
    
        if (str == null) {
            return null;
        }
        
        return str.replaceAll("\n", " ")
                   .replaceAll("\t", " ")
                   .replaceAll("&",  "&amp;")
                   .replaceAll("<",  "&lt;")
                   .replaceAll(">",  "&gt;")
                   .replaceAll("\"", "&quot;")
                   .replaceAll("\\|", " "); // not for htmlSafe but for player parsing
    }
    
    public static String validateLangCode(String lang) {
        
        if (lang == null) {
            return null;
        }
        
        lang = lang.toLowerCase();
        
        if (lang.equals(LangTable.LANG_ZH)) {
            return LangTable.LANG_ZH;
        } else if (lang.equals(LangTable.LANG_EN)) {
            return LangTable.LANG_EN;
        } else if (lang.equals(LangTable.OTHER)) {
            return LangTable.OTHER;
        } else {
            return null;
        }
    }
    
    public static String seqToStr(int seq) {
        
        return String.format("%08d", seq);
    }
    
    public static String getSharingUrl(boolean flipr,
            ApiContext context, String channelIdStr, String programIdStr) {
        
        String schema = "http";
        if (flipr) {
            schema = "flipr";
            if (context != null) {
                Mso mso = context.getMso();
                schema += MsoManager.isNNMso(mso) ? "" : "-" + mso.getName();
            }
        }
        
        String domain = MsoConfigManager.getServerDomain();
        if (context != null) {
            domain = context.getAppDomain();
        }
        
        return schema + "://" + domain
                + "/view/p" + channelIdStr + "/"
                + (programIdStr == null ? "" : programIdStr);
    }
    
    public static String getSharingUrl(boolean flipr, String mso, Long channelId, Long episodeId) {
        
        if (mso == null) {
            
            NnChannelPref pref = channelPrefMngr.getBrand(channelId);
            mso = pref.getValue();
        }
        
        String schema = "http";
        if (flipr) {
            schema = "flipr";
            if (mso != null && mso != Mso.NAME_9X9) {
                schema += "-" + mso;
            }
        }
        
        String domain = MsoConfigManager.getServerDomain();
        
        if (mso != null && !mso.equals(Mso.NAME_9X9)) {
            
            domain = mso + "." + domain.replaceAll("^www\\.", "");
        }
        
        return schema + "://" + domain + "/view/p" + channelId
                   + "/" + (episodeId == null ? "" : "e" + episodeId);
    }
}
