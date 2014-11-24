package com.nncloudtv.lib;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

public class NnLogUtil {
    
    protected final static Logger log = Logger.getLogger(NnLogUtil.class.getName());
    
    public static void logFinalize(String className) {
        
        System.out.println(String.format("[finalize] %s", className));
    }
    
    public static void logException(Exception e) {
        
        if (e == null) {
            return;
        }
        
        String detail = "";
        StackTraceElement[] elements = e.getStackTrace();
        for (StackTraceElement elm:elements ) {
            detail = detail + elm.toString() + "\n";
        }
        log.severe("exception:" + e.toString());
        log.severe("exception stacktrace:\n" + detail);
    }
    
    public static void logThrowable(Throwable t) {
        String detail = "";
        StackTraceElement[] elements = t.getStackTrace();
        for (StackTraceElement elm:elements ) {
            detail = detail + elm.toString() + "\n";
        }
        log.severe("exception:" + t.toString());
        log.severe("exception stacktrace:\n" + detail);
    }
    
    public static void logUrl(HttpServletRequest req) {
        String url = req.getRequestURL().toString();
        String queryStr = req.getQueryString();
        if (queryStr != null && !queryStr.equals("null"))
            queryStr = "?" + (queryStr.length() > 150 ? queryStr.substring(0, 147) + "..." : queryStr);
        else 
            queryStr = "";
        url +=  queryStr;
        NnNetUtil.log.info(url);
    }
    
}
