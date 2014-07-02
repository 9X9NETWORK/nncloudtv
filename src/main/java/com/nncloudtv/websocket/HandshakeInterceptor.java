package com.nncloudtv.websocket;

import java.util.Map;
import java.util.logging.Logger;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

public class HandshakeInterceptor extends HttpSessionHandshakeInterceptor {
    
    protected static final Logger log = Logger.getLogger(HandshakeInterceptor.class.getName());
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Exception ex) {
        
        log.info("before handshake");
        // TODO Auto-generated method stub
        super.afterHandshake(request, response, wsHandler, ex);
    }
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> map) throws Exception {
        
        log.info("after handshake");
        // TODO Auto-generated method stub
        return super.beforeHandshake(request, response, wsHandler, map);
    }
}
