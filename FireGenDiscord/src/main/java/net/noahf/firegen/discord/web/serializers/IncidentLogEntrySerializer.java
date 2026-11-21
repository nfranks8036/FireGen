package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.utilities.Time;

import java.io.IOException;

public class IncidentLogEntrySerializer extends JsonSerializer<IncidentLogEntry> {

    @Override
    public void serialize(IncidentLogEntry entry, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeNumberField("time", Time.getUnix(((IncidentLogEntryImpl)entry).getCustomTimeOrDefault()));
        gen.writeNumberField("trueTime", Time.getUnix(entry.getTime()));
        gen.writeNumberField("contributor", entry.getUser().getId());
        gen.writeStringField("type", entry.getType().name());
        gen.writeStringField("text", entry.getEntry());

        gen.writeEndObject();
    }

}
