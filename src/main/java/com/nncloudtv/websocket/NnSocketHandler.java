package com.nncloudtv.websocket;

import java.util.logging.Logger;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class NnSocketHandler extends TextWebSocketHandler {
    
    protected static final Logger log = Logger.getLogger(NnSocketHandler.class.getName());
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
        
        log.info(msg.getPayload());
        // TODO Auto-generated method stub
        super.handleTextMessage(session, msg);
    }
    
}
