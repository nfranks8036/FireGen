package net.noahf.firegen.discord.incidents.structure.units;

import lombok.Getter;
import lombok.Setter;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.api.incidents.units.Secondary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.StringJoiner;

@Getter
public class AssignmentEventImpl implements AssignmentEvent {

    public AssignmentEventImpl(LocalDateTime timestamp, AssignmentStatus status, Contributor<?> contributor, @Nullable Secondary secondary) {
        this.timestamp = timestamp;
        this.status = status;
        this.contributor = contributor;
        this.secondary = secondary;
    }

    private long id;

    private final LocalDateTime timestamp;

    private final AssignmentStatus status;

    private final Contributor<?> contributor;

    private @Nullable @Setter Secondary secondary;

    @Override
    @NotNull
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "[", "]")
                .add("time=" + timestamp.toString())
                .add("status=" + status.toString())
                .add("contributor=" + contributor.toString());
        if (secondary != null) {
            joiner.add("secondary=" + secondary);
        }
        return joiner.toString();
    }
}
