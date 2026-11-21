package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.incidents.units.OperationalStatus;

import java.io.IOException;

public class OperationalStatusSerializer extends JsonSerializer<OperationalStatus> {

    @Override
    public void serialize(OperationalStatus status, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("name", status.getName());
        gen.writeStringField("short", status.getShortName());
        gen.writeObjectField("purpose", status.getPurpose());
        gen.writeNumberField("ordinal", status.ordinal());

        gen.writeEndObject();
    }

}
