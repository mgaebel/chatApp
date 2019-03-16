package ChatApp.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class ChatMessageHandler {

    private Gson gson;

    public ChatMessageHandler(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter( OutgoingChatMessage.class, new OutgoingChatMessageSerializer());
        gson = gsonBuilder.create();
    }

    public void handleMessage( IncomingChatMessage chatMessage, Map<User,WebSocketSession> activeSessions ){
        OutgoingChatMessage outgoingChatMessage = new OutgoingChatMessage();
        outgoingChatMessage.setMessageTime(LocalDateTime.now());
        outgoingChatMessage.setSender( chatMessage.getSender() );
        outgoingChatMessage.setTextMessage( chatMessage.getTextMessage() );
        outgoingChatMessage.setLabelColor( chatMessage.getLabelColor() );
        outgoingChatMessage.setMessageType( 2 );

        StringBuilder sb = new StringBuilder(50);
        sb.append("Sent To:[");
        if(chatMessage.getTargetUsers().isEmpty()){
         sb.append("Everyone");
        } else {
            for (int i = 0; i < chatMessage.getTargetUsers().size(); i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(chatMessage.getTargetUsers().get(i).getName());
                outgoingChatMessage.setSender(chatMessage.getSender()+" @"+chatMessage.getTargetUsers().get(i).getName());
            }
        }
        sb.append(']');
        outgoingChatMessage.setSentTo( sb.toString() );

        String jsonMessage = gson.toJson( outgoingChatMessage );

        for( User user : activeSessions.keySet() ){
            if( chatMessage.getTargetUsers().isEmpty() ){
                try {
                    if( !user.getName().equals(chatMessage.getSender()) ) {
                        activeSessions.get(user).sendMessage(new TextMessage(jsonMessage));
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            } else {
                for (User targetUser : chatMessage.getTargetUsers()) {
                    if (user.equals(targetUser)) {
                        try {
                            activeSessions.get(user).sendMessage(new TextMessage(jsonMessage));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void handleTypingMessage(TypingMessage typingMessage, Map<User, WebSocketSession> activeSessions) {
        for( User user : activeSessions.keySet() ){
            if( typingMessage.getTargetUsers().isEmpty() ) {
                if (!typingMessage.getSender().equals(user.getName())) {
                    try {
                        activeSessions.get(user).sendMessage(new TextMessage("{\"messageType\":4,\"sender\":\"" + typingMessage.getSender() + "\"}"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                for (User targetUser : typingMessage.getTargetUsers()) {
                    if (user.equals(targetUser)) {
                        try {
                            activeSessions.get(user).sendMessage(new TextMessage("{\"messageType\":4,\"sender\":\"" + typingMessage.getSender() + "\"}"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}