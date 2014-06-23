package com.nncloudtv.exception;

public class NnApiException extends NnException {

    /**
     * NnApiException
     */
    private static final long serialVersionUID = 1L;
    
    public NnApiException(String msg) {
        super(msg);
    }
    
    public NnApiException() {
        super(null);
    }
}
