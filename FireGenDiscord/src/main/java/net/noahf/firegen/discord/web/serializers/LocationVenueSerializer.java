package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.incidents.location.LocationVenue;

import java.io.IOException;

public class LocationVenueSerializer extends JsonSerializer<LocationVenue> {

    @Override
    public void serialize(LocationVenue venue, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("name", venue.getName());
        gen.writeStringField("display", venue.getDisplayName());

        gen.writeEndObject();
    }

}
