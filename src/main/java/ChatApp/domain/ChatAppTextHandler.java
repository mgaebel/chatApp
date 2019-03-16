package ChatApp.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatAppTextHandler extends TextWebSocketHandler {

    private Gson gson;
    @Autowired
    private SessionMessageHandler sessionMessageHandler;
    @Autowired
    private ChatMessageHandler chatMessageHandler;

    public ChatAppTextHandler(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        registerTypeAdapters(gsonBuilder);
        gson = gsonBuilder.create();
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {

        try {
            Integer messageType = getMessageType(message.getPayload());

            switch (messageType) {
                case 0: {
                    sessionMessageHandler.handleNewSessionMessage(gson.fromJson(getMessageContent(message.getPayload()), NewSessionMessage.class), session);
                    break;
                }
                case 1: {
                    sessionMessageHandler.handleRemoveSessionMessage(gson.fromJson(getMessageContent(message.getPayload()), RemoveSessionMessage.class), session);
                    break;
                }
                case 2: {
                    chatMessageHandler.handleMessage(gson.fromJson(getMessageContent(message.getPayload()), IncomingChatMessage.class),sessionMessageHandler.getActiveSessions());
                    break;
                }
                case 3: {
                    sessionMessageHandler.handleHearteatMessage(session);
                    break;
                }
                case 4: {
                    chatMessageHandler.handleTypingMessage(gson.fromJson(getMessageContent(message.getPayload()), TypingMessage.class),sessionMessageHandler.getActiveSessions());
                    break;
                }
                case 6: {
                    sessionMessageHandler.handleSettingsSave( gson.fromJson(getMessageContent(message.getPayload()), UserSettings.class), session);
                    break;
                }
                default: {
                    System.out.println(message.getPayload());
                }
            }
        } catch( IllegalStateException t ){
            t.printStackTrace();
            //TODO remove user from active list
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    private Integer getMessageType(String payload) {
        assert( payload.contains("|"));
        String type = payload.substring(0, payload.indexOf( "|" ) );
        return Integer.valueOf( type );
    }

    private String getMessageContent( String payload ){
        assert( payload.contains("|") );
        String content = payload.substring( payload.indexOf( "|" ) + 1 );
        return content;
    }

    private void registerTypeAdapters(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapter( NewSessionMessage.class, new NewSessionMessageDeserializer() );
        gsonBuilder.registerTypeAdapter( RemoveSessionMessage.class, new RemoveSessionMessageDeserializer() );
        gsonBuilder.registerTypeAdapter( IncomingChatMessage.class, new IncomingChatMessageDeserializer() );
    }
}
