package net.noahf.firegen.discord.incidents.structure.units;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.noahf.firegen.api.incidents.units.RadioChannel;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Getter @Accessors(fluent = true)
@Entity
public class RadioChannelImpl implements RadioChannel {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
        return this.alphaTag;
    }
}
