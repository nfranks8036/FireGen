package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.discord.incidents.structure.IncidentStatusEmoji;

import java.io.IOException;

public class IncidentStatusSerializer extends JsonSerializer<IncidentStatusEmoji> {

    @Override
    public void serialize(IncidentStatusEmoji statusEmoji, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(statusEmoji.getStatus().name());
    }

}
