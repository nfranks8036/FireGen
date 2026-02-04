package net.noahf.firegen.backend.database.structure;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Reference;
import lombok.Builder;
import net.noahf.firegen.backend.database.structure.helper.AssignmentEvent;
import net.noahf.firegen.backend.structure.objects.RadioChannel;

import java.time.Instant;
import java.util.List;
import java.util.Stack;

@Entity
public class UnitAssignment {

    public String incident;

    public String unit;

    public boolean primary;

    public String radioChannel;

    public List<AssignmentEvent> events;

    public UnitAssignment() { super(); }
    public UnitAssignment(Unit unit, Incident incident, boolean primary, String radioChannel) {
        this.incident = incident.fullId;
        this.unit = unit.getCallsign();
        this.primary = primary;
        this.radioChannel = radioChannel;
        this.events = new Stack<>();
    }

}
