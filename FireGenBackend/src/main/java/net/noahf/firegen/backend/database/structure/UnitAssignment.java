package net.noahf.firegen.backend.database.structure;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Reference;
import net.noahf.firegen.backend.database.structure.helper.AssignmentEvent;

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

    public Instant assigned;
    public Instant cleared;

    public Stack<AssignmentEvent> events;

}
