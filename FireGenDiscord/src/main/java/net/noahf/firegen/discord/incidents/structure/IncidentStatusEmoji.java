package net.noahf.firegen.discord.incidents.structure;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import net.noahf.firegen.api.AutofilledCharSequence;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.discord.web.serializers.IncidentStatusSerializer;
import org.jetbrains.annotations.NotNull;

@Getter
@JsonSerialize(using = IncidentStatusSerializer.class)
public class IncidentStatusEmoji implements AutofilledCharSequence {

    public IncidentStatusEmoji(String name, String leftEmoji, String rightEmoji) {
        this.status = IncidentStatus.valueOf(name);
        this.leftEmoji = leftEmoji;
        this.rightEmoji = rightEmoji;
    }

    private final IncidentStatus status;
    private final String leftEmoji;
    private final String rightEmoji;

    public String getEmojisFormattedCombined() {
        return this.leftEmoji + this.rightEmoji;
    }

    @Override
    @NotNull
    public String toString() {
        return this.status.name();
    }
}
