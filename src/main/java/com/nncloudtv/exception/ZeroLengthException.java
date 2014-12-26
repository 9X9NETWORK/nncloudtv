package com.nncloudtv.exception;

public class ZeroLengthException extends Exception {
    
    /**
     * ZeroLengthException
     */
    private static final long serialVersionUID = 1L;
    
    public ZeroLengthException() {
        
        super();
    }
    
    public ZeroLengthException(String msg) {
        
        super(msg);
    }
    
}
