package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

public interface AssignmentEvent extends AutofilledCharSequence, Comparable<AssignmentEvent> {

    LocalDateTime getTimestamp();

    AssignmentStatus getStatus();

    Contributor<?> getContributor();

    @Nullable Secondary getSecondary();

    @Override
    default int compareTo(@NotNull AssignmentEvent o) {
        return this.getTimestamp().compareTo(o.getTimestamp());
    }
}
