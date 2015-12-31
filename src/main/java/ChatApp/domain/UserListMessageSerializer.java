package ChatApp.domain;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

/**
 * Created by Michael on 15/04/2015.
 */
public class UserListMessageSerializer implements JsonSerializer<UserListMessage> {
    @Override
    public JsonElement serialize(UserListMessage userListMessage, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("messageType",1);
        jsonObject.addProperty("newSessionAdded",userListMessage.getNewSessionAdded());
        JsonArray array = new JsonArray();
        for( User user : userListMessage.getUsers() ){
            JsonObject userObj = new JsonObject();
            userObj.addProperty("name", user.getName());
            userObj.addProperty("sessionTimestamp", user.getLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            userObj.addProperty("status", user.getStatus().name());
            userObj.addProperty("id", user.getId());
            array.add(userObj);
        }
        jsonObject.add( "users", array );
        return jsonObject;
    }
}
