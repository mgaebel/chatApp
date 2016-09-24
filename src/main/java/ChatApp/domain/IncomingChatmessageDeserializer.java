package ChatApp.domain;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael on 17/04/2015.
 */
public class IncomingChatMessageDeserializer implements JsonDeserializer<IncomingChatMessage> {

    @Override
    public IncomingChatMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        IncomingChatMessage message = new IncomingChatMessage();
        JsonObject messageObject = jsonElement.getAsJsonObject();
        message.setTextMessage(messageObject.getAsJsonPrimitive("textMessage").getAsString());
        message.setMessageTime(LocalDateTime.now());
        message.setSender( messageObject.getAsJsonPrimitive("sender").getAsString());
        message.setLabelColor( messageObject.getAsJsonPrimitive("labelColor").getAsString());
        JsonArray targetUsersArray = messageObject.getAsJsonArray("targetUsers");
        List<User> targetUsers = new ArrayList<User>();
        for( int i=0; i < targetUsersArray.size(); i++ ){
            User user = new User();
            user.setStatus( StatusEnum.valueOf(targetUsersArray.get(i).getAsJsonObject().getAsJsonPrimitive("status").getAsString()) );
            user.setName( targetUsersArray.get(i).getAsJsonObject().getAsJsonPrimitive("name").getAsString());
            user.setId( targetUsersArray.get(i).getAsJsonObject().getAsJsonPrimitive("id").getAsString());
            user.setLocalDateTime(LocalDateTime.now());
            targetUsers.add(user);
        }
        message.setTargetUsers(targetUsers);

        return message;
    }
}
