package vn.cineshow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker //bat tinh nang web socket server
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) { //STOMP: Simple Text Oriented Messaging Protocol
        registry.addEndpoint("/ws")              // endpoint de client connect
                .setAllowedOriginPatterns("http://localhost:3000", "http://localhost:3001")   // allows CORS
                .withSockJS();                   // fallback SockJS- tuỳ chọn dự phòng cho  các trình duyệt không hỗ trợ websocket

        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*")
                .setAllowedOrigins("https://websocketking.com", "http://localhost:3000", "*");

    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); // noi client subscribe
        registry.setApplicationDestinationPrefixes("/app"); // noi client gửi message
    }

}
