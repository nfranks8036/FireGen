package net.noahf.firegen.backend.database.structure.helper;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import jakarta.annotation.Nullable;
import net.noahf.firegen.backend.database.structure.Unit;
import net.noahf.firegen.backend.structure.objects.UnitAssignmentStatus;

import java.time.Instant;

@Entity
public class AssignmentEvent {

    public String status;
    public Instant timestamp;
    public String narrative;

    public AssignmentEvent() { super(); }
    public AssignmentEvent(Unit unit, UnitAssignmentStatus status, @Nullable String narrative) {
        this.status = status.name;
        this.timestamp = Instant.now();
        this.narrative = status.asNarrative(unit.getCallsign()) + (narrative != null ? ": " + narrative : "");
    }

    @Override
    public String toString() {
        return "{status=" + status +
                ", timestamp=" + timestamp +
                ", narrative='" + narrative + '\'' +
                '}';
    }
}