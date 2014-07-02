package com.nncloudtv.websocket;

import java.util.logging.Logger;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class NnSocketHandler extends TextWebSocketHandler {
    
    protected static final Logger log = Logger.getLogger(NnSocketHandler.class.getName());
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        
        log.info(message.getPayload());
        // TODO Auto-generated method stub
        super.handleTextMessage(session, message);
        TextMessage returnMessage = new TextMessage(message.getPayload()+" received at server");
        session.sendMessage(returnMessage);
    }
    
}
