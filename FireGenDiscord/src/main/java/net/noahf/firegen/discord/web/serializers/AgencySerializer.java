package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.incidents.units.Unit;

import java.io.IOException;

public class AgencySerializer extends JsonSerializer<Agency> {

    @Override
    public void serialize(Agency agency, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("title", agency.getTitle());
        gen.writeStringField("short", agency.getShorthand());
        gen.writeStringField("formatted", agency.getFormatted());
        gen.writeStringField("station", agency.getStation());
        gen.writeStringField("type", agency.getType().name());
        gen.writeNumberField("ordinal", agency.ordinal());

        gen.writeArrayFieldStart("units");
        for (Unit u : agency.getUnits()) {
            gen.writeStartObject();
            gen.writeNumberField("id", u.getId());
            gen.writeStringField("short", u.getShorthand());
            gen.writeStringField("long", u.getLonghand());
            gen.writeEndObject();
        }
        gen.writeEndArray();

        gen.writeEndObject();
    }

}
