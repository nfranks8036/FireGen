package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.incidents.location.IncidentLocation;

import java.io.IOException;

public class IncidentLocationSerializer extends JsonSerializer<IncidentLocation> {

    @Override
    public void serialize(IncidentLocation location, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("format", location.format());

        gen.writeStringField("type", location.getType().getId());
        gen.writeStringField("commonName", location.getCommonName());
        gen.writeStringField("venue", (location.getVenue() != null ? location.getVenue().getName() : null));

        gen.writeArrayFieldStart("data");
        for (String data : location.getData()) {
            gen.writeString(data);
        }
        gen.writeEndArray();


        gen.writeEndObject();
    }

}
