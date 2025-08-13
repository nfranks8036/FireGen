package net.noahf.firewatch.common.incidents;

import net.noahf.firewatch.common.FireGen;
import net.noahf.firewatch.common.data.CallerType;
import net.noahf.firewatch.common.data.IncidentPriority;
import net.noahf.firewatch.common.data.IncidentStatus;
import net.noahf.firewatch.common.data.IncidentType;
import net.noahf.firewatch.common.geolocation.IncidentAddress;
import net.noahf.firewatch.common.narrative.IncidentNarrative;
import net.noahf.firewatch.common.units.Unit;
import net.noahf.firewatch.common.units.UnitAssignment;
import net.noahf.firewatch.common.utils.Identifier;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class Incident {

    private Identifier id;
    private IncidentStatus status;
    private IncidentType type;
    private IncidentPriority priority;
    private IncidentAddress address;
    private IncidentNarrative narrative;
    private CallerType callerType;
    private Instant created, closed;
    private Set<UnitAssignment> assignments;

    public Incident() {
        this.id = Identifier.generate(0);
        this.status = FireGen.get().incidentStructure().incidentStatuses().marked(IncidentStatus.NEW_INCIDENT).one();
        this.type = null;
        this.priority = null;
        this.address = IncidentAddress.blankAddress();
        this.narrative = new IncidentNarrative();
        this.created = Instant.now();
        this.closed = null;
        this.assignments = new HashSet<>();
    }

    public void assignUnit(Unit unit, boolean primary) {
        if (assignments.stream().anyMatch(ua -> ua.unit().equals(unit))) {
            throw new IllegalStateException("Unit already assigned to another incident.");
        }

        UnitAssignment assignment = new UnitAssignment(this, unit, primary);
        this.assignments.add(assignment);
        unit.assignment(assignment);
    }

    public Identifier identifier() { return this.id; }

    public IncidentStatus status() { return this.status; }
    public void status(IncidentStatus newStatus) { this.status = newStatus; }

    public IncidentType type() { return this.type; }
    public void type(IncidentType newType) {
        this.type = newType;
        if (!newType.getIncidentPriorities().contains(this.priority)) {
            this.priority(newType.getIncidentPriorities().asCollection().stream().findFirst().orElseThrow());
        }
    }

    public IncidentPriority priority() { return this.priority; }
    public void priority(IncidentPriority newPriority) { this.priority = newPriority; }

    public IncidentAddress address() { return this.address; }

    public IncidentNarrative narrative() { return this.narrative; }

    public CallerType callerType() { return this.callerType; }
    public void callerType(CallerType newCallerType) { this.callerType = newCallerType; }

    public Instant created() { return this.created; }
    public Instant closed() { return this.closed; }

}
