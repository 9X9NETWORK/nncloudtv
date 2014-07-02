package com.nncloudtv.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        
        registry.addHandler(new NnSocketHandler(), "/websocket")
                .addInterceptors(new HandshakeInterceptor())
                .setHandshakeHandler(handshakeHandler());
    }
    
    public DefaultHandshakeHandler handshakeHandler() {
        
        return new DefaultHandshakeHandler(new JettyRequestUpgradeStrategy());
    }
}
