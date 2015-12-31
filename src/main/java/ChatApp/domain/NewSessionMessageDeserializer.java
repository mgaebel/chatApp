package ChatApp.domain;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Date;

/**
 * Created by Michael on 14/04/2015.
 */
public class NewSessionMessageDeserializer implements JsonDeserializer<NewSessionMessage>{
    @Override
    public NewSessionMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        NewSessionMessage newSessionMessage = new NewSessionMessage();

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String name = jsonObject.getAsJsonPrimitive("name").getAsString();
        LocalDate date = LocalDate.now();
        newSessionMessage.setName( name );
        newSessionMessage.setSessionDate( date );

        return newSessionMessage;
    }
}
