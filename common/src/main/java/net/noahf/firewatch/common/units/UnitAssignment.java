package net.noahf.firewatch.common.units;

import net.noahf.firewatch.common.FireGen;
import net.noahf.firewatch.common.data.UnitAssignmentStatus;
import net.noahf.firewatch.common.incidents.Incident;
import net.noahf.firewatch.common.utils.Identifier;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;

public class UnitAssignment {

    private Identifier id;
    private Incident incident;
    private Unit unit;
    private boolean primary;
    private Instant assigned, cleared;
    private Stack<AssignmentEvent> history;

    public UnitAssignment(Incident incident, Unit unit, boolean primary) {
        this.id = Identifier.generate(11);
        this.incident = incident;
        this.unit = unit;
        this.primary = primary;
        this.history = new Stack<>();
        this.assigned = Instant.now();
        this.cleared = null;

        this.updateStatus(FireGen.get().incidentStructure().unitAssignmentStatuses().marked(UnitAssignmentStatus.ASSIGNED).one(), null);
    }

    public void updateStatus(UnitAssignmentStatus status, @Nullable String narrative) {
        this.history.push(new AssignmentEvent(status, Instant.now(), narrative));
    }

    public AssignmentEvent lastAssignment() { return this.history.peek(); }
    public List<AssignmentEvent> assignmentHistory() { return this.history; }

    public Incident incident() { return this.incident; }
    public Unit unit() { return this.unit; }

    public void clear() {
        this.cleared = Instant.now();
        this.updateStatus(FireGen.get().incidentStructure().unitAssignmentStatuses().marked(UnitAssignmentStatus.AVAILABLE).one(), null);
        this.unit.assignment(null);
    }

}
