package ChatApp.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler(), "/websocket").withSockJS();
    }

    @Bean
    public WebSocketHandler myHandler() {
        return new ChatAppTextHandler();
    }

}



/*
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;



@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    //@Override
    //public void configureMessageBroker(MessageBrokerRegistry config) {
        //config.enableSimpleBroker("/topic");
        //config.setApplicationDestinationPrefixes("/websocket");
    //}

    //@Override
    //public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/chat").withSockJS();
  //  }



}
*/