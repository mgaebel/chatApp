package ChatApp.domain;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TypingMessageDeserializer implements JsonDeserializer<TypingMessage> {

    @Override
    public TypingMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        TypingMessage message = new TypingMessage();
        JsonObject messageObject = jsonElement.getAsJsonObject();
        message.setSender( messageObject.getAsJsonPrimitive("sender").getAsString());
        JsonArray targetUsersArray = messageObject.getAsJsonArray("targetUsers");
        List<User> targetUsers = new ArrayList<User>();
        for( int i=0; i < targetUsersArray.size(); i++ ){
            User user = new User();
            user.setName( targetUsersArray.get(i).getAsJsonObject().getAsJsonPrimitive("name").getAsString());
            user.setId( targetUsersArray.get(i).getAsJsonObject().getAsJsonPrimitive("id").getAsLong());
            targetUsers.add(user);
        }
        message.setTargetUsers(targetUsers);

        return message;
    }
}
