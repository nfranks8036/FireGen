package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public record AssignmentEvent(
        LocalDateTime timestamp, AssignmentStatus status, Contributor<?> contributor
) implements AutofilledCharSequence {

    @Override
    @NotNull
    public String toString() {
        return "[time=" + timestamp.toString() + ", status=" + status.toString() + ", contributor=" + contributor.toString() + "]";
    }
}
