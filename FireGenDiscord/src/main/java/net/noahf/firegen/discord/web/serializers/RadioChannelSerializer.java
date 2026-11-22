package net.noahf.firegen.discord.web.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.noahf.firegen.api.incidents.units.RadioChannel;

import java.io.IOException;

public class RadioChannelSerializer extends JsonSerializer<RadioChannel> {

    @Override
    public void serialize(RadioChannel channel, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeNumberField("talkgroup", channel.getTalkgroupId());
        gen.writeStringField("name", channel.getName());
        gen.writeStringField("alphaTag", channel.getAlphaTag());

        gen.writeEndObject();
    }

}
