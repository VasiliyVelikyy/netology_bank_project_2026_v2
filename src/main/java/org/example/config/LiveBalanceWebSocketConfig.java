package org.example.config;

import lombok.RequiredArgsConstructor;
import org.example.service.websocket.LiveBalanceWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import static org.example.util.Constants.LIVE_BALANCE;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class LiveBalanceWebSocketConfig implements WebSocketConfigurer {
    private final LiveBalanceWebSocketHandler liveBalanceWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(liveBalanceWebSocketHandler, LIVE_BALANCE)
                .setAllowedOrigins("*"); // todo

    }
}
