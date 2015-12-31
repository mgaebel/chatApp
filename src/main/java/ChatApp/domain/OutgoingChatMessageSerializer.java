package ChatApp.domain;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

/**
 * Created by Michael on 15/04/2015.
 */
public class OutgoingChatMessageSerializer implements JsonSerializer<OutgoingChatMessage>{
    @Override
    public JsonElement serialize(OutgoingChatMessage outgoingChatMessage, Type type, JsonSerializationContext jsonSerializationContext) {

        JsonObject chatMessage = new JsonObject();
        chatMessage.addProperty( "messageDateTime", outgoingChatMessage.getMessageTime().format(DateTimeFormatter.ISO_DATE_TIME) );
        chatMessage.addProperty( "textMessage", outgoingChatMessage.getTextMessage() );
        chatMessage.addProperty( "sender", outgoingChatMessage.getSender() );
        chatMessage.addProperty( "messageType", outgoingChatMessage.getMessageType() );
        chatMessage.addProperty( "sentTo", outgoingChatMessage.getSentTo() );

        return chatMessage;
    }
}
