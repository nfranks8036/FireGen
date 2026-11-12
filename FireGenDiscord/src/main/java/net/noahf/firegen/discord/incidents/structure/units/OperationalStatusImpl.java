package net.noahf.firegen.discord.incidents.structure.units;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.units.OperationalStatus;
import net.noahf.firegen.api.incidents.units.OperationalStatusPurpose;
import net.noahf.firegen.discord.utilities.ansi.AnsiColor;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@Getter
@AllArgsConstructor
public class OperationalStatusImpl implements OperationalStatus {

    private String name;

    private String shortName;

    private @Accessors(fluent = true) int ordinal;

    private @Nullable OperationalStatusPurpose purpose;

    private AnsiColor ansiColor;

    private String emojiString;
    private Emoji emoji;

    @Override
    public @NonNull String toString() {
        return this.name;
    }

    public Emoji getEmoji() {
        if (emoji == null) {
            if (emojiString == null) return null;

            this.emoji = Emoji.fromFormatted(this.emojiString);
        }
        return this.emoji;
    }

}
