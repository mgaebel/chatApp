package ChatApp.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableJpaRepositories(basePackages = {"ChatApp.repository"})
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatAppTextHandler chatAppTextHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatAppTextHandler, "/websocket").withSockJS();
    }

    @Bean
    public WebSocketHandler myHandler(ChatAppTextHandler chatAppTextHandler) {
        return chatAppTextHandler;
    }

}