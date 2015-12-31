package ChatApp.domain;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by Michael on 11/25/2015.
 */
public class SettingsRequestMessageDeserializer implements JsonDeserializer<SettingsRequestMessage> {
    @Override
    public SettingsRequestMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        SettingsRequestMessage settingsRequestMessage = new SettingsRequestMessage();

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String name = jsonObject.getAsJsonPrimitive("name").getAsString();
        settingsRequestMessage.setName( name );

        return settingsRequestMessage;

    }
}
