package net.noahf.firegen.backend.database.structure.helper;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import jakarta.annotation.Nullable;
import net.noahf.firegen.backend.database.structure.Incident;
import net.noahf.firegen.backend.database.structure.Unit;
import net.noahf.firegen.backend.structure.objects.RadioChannel;
import net.noahf.firegen.backend.structure.objects.UnitAssignmentStatus;

import java.time.Instant;

@Entity
public class AssignmentEvent {

    public @Id String unitId;
    public UnitAssignmentStatus status;
    public Instant timestamp;
    public String narrative;

    public AssignmentEvent(Unit unit, UnitAssignmentStatus status, @Nullable String narrative) {
        this.unitId = unit.id;
        this.status = status;
        this.timestamp = Instant.now();
        this.narrative = status.asNarrative(unit.id) + (narrative != null ? ": " + narrative : "");
    }

    @Override
    public String toString() {
        return "{status=" + status +
                ", timestamp=" + timestamp +
                ", narrative='" + narrative + '\'' +
                '}';
    }
}