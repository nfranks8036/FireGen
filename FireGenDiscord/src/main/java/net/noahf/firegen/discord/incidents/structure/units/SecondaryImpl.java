package net.noahf.firegen.discord.incidents.structure.units;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.units.Secondary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor @Getter
public class SecondaryImpl implements Secondary {

    private final String longName;

    private final String shortName;

    private final @Nullable Emoji emoji;

    @Override
    @NotNull
    public String toString() {
        return this.getLongName();
    }
}
