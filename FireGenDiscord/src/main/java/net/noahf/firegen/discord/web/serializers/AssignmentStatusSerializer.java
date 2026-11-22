package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;

import java.io.IOException;

public class AssignmentStatusSerializer extends JsonSerializer<AssignmentStatus> {

    @Override
    public void serialize(AssignmentStatus status, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("name", status.getName());
        gen.writeStringField("short", status.getShortName());
        gen.writeNumberField("ordinal", status.ordinal());
        gen.writePOJOField("secondaries", status.getSecondaries());

        gen.writeEndObject();
    }

}
