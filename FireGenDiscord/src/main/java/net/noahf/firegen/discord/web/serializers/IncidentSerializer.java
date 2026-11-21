package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.utilities.Log;

import java.io.IOException;
import java.util.Map;

public class IncidentSerializer extends JsonSerializer<Incident> {

    @Override
    public void serialize(Incident incident, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeNumberField("id", incident.getId());
        gen.writeStringField("status", incident.getStatus().name());
        gen.writePOJOField("type", incident.getType());
        gen.writePOJOField("location", incident.getLocation());
        gen.writeNumberField("time", incident.getTime().getUnix());
        gen.writeStringField("publishStatus", incident.getPublished().name());

        gen.writeArrayFieldStart("units");
        for (UnitAssignment assignment : incident.getUnitAssignments().stream().sorted().toList()) {
            gen.writeObject(assignment);
        }
        gen.writeEndArray();

        gen.writeArrayFieldStart("log");
        for (IncidentLogEntry entry : incident.getLog().stream().sorted().toList().reversed()) {
            gen.writeObject(entry);
        }
        gen.writeEndArray();

        gen.writeArrayFieldStart("contributors");
        for (Contributor<?> contributor : incident.getContributors()) {
            gen.writeNumber(contributor.getId());
        }
        gen.writeEndArray();

        gen.writeArrayFieldStart("links");
        for (Map.Entry<String, String> entry : incident.getLinks().entrySet()) {
            gen.writeStartObject();

            gen.writeStringField("url", entry.getKey());
            gen.writeStringField("title", entry.getValue());

            gen.writeEndObject();
        }
        gen.writeEndArray();

        gen.writeNumberField("nextStale", (long)incident.getUnixNextStale());
        gen.writeNumberField("nextClosedDueToStale", (long)incident.getUnixNextClosedDueToStale());

        gen.writeEndObject();
    }
}
