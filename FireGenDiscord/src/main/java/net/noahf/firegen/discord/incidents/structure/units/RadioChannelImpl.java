package net.noahf.firegen.discord.incidents.structure.units;

import lombok.AllArgsConstructor;
import net.noahf.firegen.api.incidents.units.RadioChannel;
import org.jetbrains.annotations.NotNull;

public record RadioChannelImpl(String name, String alphaTag, int talkgroupId) implements RadioChannel {

    @Override
    public long getId() {
        return talkgroupId;
    }

    @Override
    @NotNull
    public String toString() {
        return this.alphaTag;
    }
}
