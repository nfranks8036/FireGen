package net.noahf.firegen.discord.incidents.structure;

import jakarta.persistence.*;
import lombok.Getter;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;

@Getter
public class IncidentStatusEmoji implements AutofilledCharSequence {

    protected IncidentStatusEmoji() {
        // required for JPA Hibernate
    }

    public IncidentStatusEmoji(String name, String leftEmoji, String rightEmoji) {
        this.status = IncidentStatus.valueOf(name);
        this.leftEmoji = leftEmoji;
        this.rightEmoji = rightEmoji;
    }

    private IncidentStatus status;
    private String leftEmoji;
    private String rightEmoji;

    public String getEmojisFormattedCombined() {
        return this.leftEmoji + this.rightEmoji;
    }

    @Override
    @NotNull
    public String toString() {
        return this.status.name();
    }
}
