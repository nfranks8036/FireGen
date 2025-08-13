package net.noahf.firewatch.common.units;

import net.noahf.firewatch.common.data.UnitAssignmentStatus;
import net.noahf.firewatch.common.incidents.Incident;
import net.noahf.firewatch.common.utils.IdGenerator;
import org.jetbrains.annotations.Nullable;

import java.sql.Array;
import java.time.Instant;
import java.util.*;

public class UnitAssignment {

    private int id;
    private Incident incident;
    private Unit unit;
    private boolean primary;
    private Set<UnitTimings> timings;
    private Stack<AssignmentEvent> history;

    public UnitAssignment(Incident incident, Unit unit, boolean primary) {
        this.id = IdGenerator.generate();
        this.incident = incident;
        this.unit = unit;
        this.primary = primary;
        this.timings = new HashSet<>();
        this.history = new Stack<>();
    }

    public void updateStatus(UnitAssignmentStatus status, @Nullable String narrative) {
        this.history.push(new AssignmentEvent(status, Instant.now(), narrative));
    }

    public UnitTimings timings(UnitAssignmentStatus status) {
        return this.timings.stream()
                .filter(ut -> ut.assignmentStatus().equals(status))
                .findFirst()
                .orElseGet(() -> {
                    UnitTimings timing = new UnitTimings(Instant.now(), status);
                    this.timings.add(timing);
                    return timing;
                });
    }

    public AssignmentEvent lastAssignment() { return this.history.peek(); }
    public List<AssignmentEvent> assignmentHistory() { return this.history; }

}
