package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.incidents.units.Unit;

import java.io.IOException;

public class UnitSerializer extends JsonSerializer<Unit> {
    @Override
    public void serialize(Unit unit, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("type", unit.getType().toString());
        gen.writeNumberField("number", unit.getNumber() != Integer.MIN_VALUE ? unit.getNumber() : -999999);
        gen.writeStringField("short", unit.getShorthand());
        gen.writeStringField("long", unit.getLonghand());
        gen.writeStringField("formatted", unit.getFormatted());

        gen.writeEndObject();
    }
}
