package net.noahf.firegen.discord.incidents.structure.units;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.noahf.firegen.api.incidents.units.RadioChannel;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Getter
public class RadioChannelImpl implements RadioChannel {

    private long id;

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
