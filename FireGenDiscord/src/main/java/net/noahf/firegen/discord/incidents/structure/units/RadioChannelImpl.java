package net.noahf.firegen.discord.incidents.structure.units;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.noahf.firegen.api.incidents.units.RadioChannel;
import net.noahf.firegen.api.utilities.IdGenerator;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
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
        return this.alphaTag;
    }
}
