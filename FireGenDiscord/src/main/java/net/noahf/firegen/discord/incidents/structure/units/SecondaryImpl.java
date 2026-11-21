package net.noahf.firegen.discord.incidents.structure.units;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.units.Secondary;
import net.noahf.firegen.discord.web.serializers.SecondarySerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonSerialize(using = SecondarySerializer.class)
public record SecondaryImpl(String longName, String shortName, @Nullable Emoji emoji) implements Secondary {

    @Override
    @NotNull
    public String toString() {
        return this.longName();
    }
}
