package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.incidents.types.IncidentType;

import java.io.IOException;

public class IncidentTypeSerializer extends JsonSerializer<IncidentType> {

    @Override
    public void serialize(IncidentType type, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("name", type.getSelectedName());
        gen.writeStringField("nameGeneric", type.getType());
        try {
            gen.writeStringField("selectedQualifier", type.getStringQualifierChoice());
        } catch (ArrayIndexOutOfBoundsException ignored) {
            gen.writeStringField("selectedQualifier", "None");
        }
        gen.writeStringField("tag", type.getTag().getTagName());

        gen.writeEndObject();
    }

}
