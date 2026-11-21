package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.utilities.Time;

import java.io.IOException;
import java.util.Objects;

public class UnitAssignmentSerializer  extends JsonSerializer<UnitAssignment> {

    @Override
    public void serialize(UnitAssignment assignment, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeNumberField("unit", assignment.getUnit().getId());
        gen.writeStringField("title", assignment.getUnit().getLonghand());
        gen.writeStringField("channel", assignment.getRadioChannel().getAlphaTag());

        gen.writeArrayFieldStart("assignments");
        for (AssignmentEvent event : assignment.getAssignments().stream().filter(Objects::nonNull).sorted().toList()) {
            gen.writeStartObject();

            gen.writeNumberField("timestamp", Time.getUnix(event.getTimestamp()));
            gen.writeStringField("status", event.getStatus().getName());
            gen.writeNumberField("contributor", event.getContributor().getId());

            if (event.getSecondary() != null) {
                gen.writeStringField("secondary", event.getSecondary().longName());
            }

            gen.writeEndObject();
        }
        gen.writeEndArray();

        gen.writeEndObject();
    }
}
