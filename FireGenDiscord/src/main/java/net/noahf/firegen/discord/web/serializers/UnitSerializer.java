package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.incidents.units.Secondary;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.api.style.StatusStyle;

import java.io.IOException;

public class UnitSerializer extends JsonSerializer<Unit> {
    @Override
    public void serialize(Unit unit, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeNumberField("id", unit.getId());
        gen.writeStringField("short", unit.getShorthand());
        gen.writeStringField("long", unit.getLonghand());
        gen.writeStringField("type", unit.getType().toString());
        gen.writeNumberField("number", unit.getNumber());
        gen.writeStringField("formatted", unit.getFormatted());
        gen.writeStringField("parentAgency", unit.getAgency().getTitle());
        gen.writeNumberField("ordinal", unit.ordinal());

        gen.writeObjectFieldStart("currentStatus");
        gen.writeNumberField("lastUpdate", unit.getStatusLastUpdate());

        UnitAssignment current = unit.getCurrentAssignment();
        String status, secondary, assignment;
        StatusStyle style;
        if (current != null) {
            status = current.getLatestAssignment().getStatus().getName();
            assignment = current.getIncident().getFormattedId();
            style = current.getLatestAssignment().getStatus().getStyle();

            Secondary s = current.getLatestAssignment().getSecondary();
            secondary = s != null ? s.shortName() : null;
        } else {
            status = unit.getOperationalStatus().getName();
            style = unit.getOperationalStatus().getStyle();
            assignment = null;
            secondary = null;
        }

        gen.writeStringField("status", status);
        gen.writeStringField("assignment", assignment);
        gen.writeStringField("secondary", secondary);
        gen.writeObjectField("style", style);

        gen.writeEndObject();

        gen.writeEndObject();
    }
}
