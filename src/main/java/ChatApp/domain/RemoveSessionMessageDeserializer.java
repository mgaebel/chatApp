package ChatApp.domain;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;

public class RemoveSessionMessageDeserializer implements JsonDeserializer<RemoveSessionMessage>{
    @Override
    public RemoveSessionMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        RemoveSessionMessage removeSessionMessage = new RemoveSessionMessage();

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        removeSessionMessage.setUserName(jsonObject.getAsJsonPrimitive("userName").getAsString() );
        return removeSessionMessage;
    }
}
