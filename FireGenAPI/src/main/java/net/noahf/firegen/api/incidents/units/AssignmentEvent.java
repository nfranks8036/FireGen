package net.noahf.firegen.api.incidents.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
public class AssignmentEvent implements AutofilledCharSequence {

    private final LocalDateTime timestamp;
    private final AssignmentStatus status;
    private final Contributor<?> contributor;

    @Override
    @NotNull
    public String toString() {
        return "[time=" + timestamp.toString() + ", status=" + status.toString() + ", contributor=" + contributor.toString() + "]";
    }
}
