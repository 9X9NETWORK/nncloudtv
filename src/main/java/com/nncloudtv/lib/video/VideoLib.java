package com.nncloudtv.lib.video;

import java.io.InputStream;

public interface VideoLib {
    
    boolean isUrlMatched(String url);
    
    String getDirectVideoUrl(String url);
    
    InputStream getDirectVideoStream(String url);
    
}
