package com.nncloudtv.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig extends WebSocketConfigurationSupport {
    
    @Override
    protected void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        
        registry.addHandler(new NnSocketHandler(), "/websocket")
                .addInterceptors(new HandshakeInterceptor());
    }
}
