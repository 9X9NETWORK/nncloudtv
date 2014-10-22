package com.nncloudtv.lib.stream;

import java.io.InputStream;

public interface StreamLib {
    
    boolean isUrlMatched(String url);
    
    String getDirectVideoUrl(String url);
    
    InputStream getDirectVideoStream(String url);
    
}
