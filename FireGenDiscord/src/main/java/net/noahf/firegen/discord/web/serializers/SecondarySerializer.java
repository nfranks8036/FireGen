package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.incidents.units.Secondary;

import java.io.IOException;

public class SecondarySerializer extends JsonSerializer<Secondary> {

    @Override
    public void serialize(Secondary secondary, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("long", secondary.longName());
        gen.writeStringField("short", secondary.shortName());

        gen.writeEndObject();
    }

}
