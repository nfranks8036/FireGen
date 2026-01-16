package net.noahf.firegen.backend.database.structure;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Reference;
import lombok.Builder;
import net.noahf.firegen.backend.database.structure.helper.AssignmentEvent;
import net.noahf.firegen.backend.structure.objects.RadioChannel;

import java.time.Instant;
import java.util.Stack;

@Entity
public class UnitAssignment {

    @Reference
    public Unit unit;

    @Reference
    public Incident incident;

    public boolean primary;

    public RadioChannel operate;

    public Stack<AssignmentEvent> events;

    public UnitAssignment(Unit unit, Incident incident, boolean primary, RadioChannel operate) {
        this.unit = unit;
        this.incident = incident;
        this.primary = primary;
        this.operate = operate;
        this.events = new Stack<>();
    }

}
