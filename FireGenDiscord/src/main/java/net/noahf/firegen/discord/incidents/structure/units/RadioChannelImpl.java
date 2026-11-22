package net.noahf.firegen.discord.incidents.structure.units;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.noahf.firegen.api.incidents.units.RadioChannel;
import net.noahf.firegen.discord.web.serializers.RadioChannelSerializer;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Getter
@JsonSerialize(using = RadioChannelSerializer.class)
public class RadioChannelImpl implements RadioChannel {

    private final String name;

    private final String alphaTag;

    private final int talkgroupId;

    @Override
    public long getId() {
        return talkgroupId;
    }

    @Override
    @NotNull
    public String toString() {
        return this.alphaTag != null ? this.alphaTag : "TG" + talkgroupId;
    }
}
