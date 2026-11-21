package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.incidents.units.UnitType;

import java.io.IOException;

public class UnitTypeSerializer extends JsonSerializer<UnitType> {
    @Override
    public void serialize(UnitType type, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("id", type.getId());
        gen.writeStringField("longName", type.getLonghand());
        gen.writeStringField("shortName", type.getShorthand());
        gen.writeStringField("formattedName", type.getFormatted());
        gen.writeStringField("description", type.getDescription());

        gen.writeEndObject();
    }
}
