package com.nncloudtv.lib.stream;

import java.io.InputStream;

/**
 * TODO: document is needed
 * 
 * @author louis
 *
 */
public interface StreamLib {
    
    boolean isUrlMatched(String urlStr);
    
    //String checkHealth(String urlStr);
    
    String normalizeUrl(String urlStr);
    
    String getDirectVideoUrl(String urlStr);
    
    String getHtml5DirectVideoUrl(String urlStr);
    
    InputStream getDirectVideoStream(String urlStr);
    
}
