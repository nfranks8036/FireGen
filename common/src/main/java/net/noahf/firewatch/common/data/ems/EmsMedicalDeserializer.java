package net.noahf.firewatch.common.data.ems;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmsMedicalDeserializer implements JsonDeserializer<EmsMedical> {

    @Override
    public EmsMedical deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        Map<String, List<EmsFieldDetails>> dynamicFields = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (!key.contains("field:")) {
                continue;
            }
            key = key.substring("field:".length());

            Type listType = new TypeToken<List<EmsFieldDetails>>() {}.getType();
            List<EmsFieldDetails> fieldDetailsList = context.deserialize(value, listType);
            dynamicFields.put(key, fieldDetailsList);
        }

        return new EmsMedical(dynamicFields);
    }

}