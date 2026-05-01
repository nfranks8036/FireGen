package net.noahf.firegen.discord.incidents.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.status.IncidentStatusAttributes;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class IncidentStatusImpl implements IncidentStatus {

    private String name;
    private String shortName;
    private Emoji leftEmoji;
    private Emoji rightEmoji;
    private IncidentStatusAttributes attributes;

    public String getEmojisFormattedCombined() {
        return this.leftEmoji.getFormatted() + this.rightEmoji.getFormatted();
    }

    @Override
    @NotNull
    public String toString() {
        return this.name;
    }
}
