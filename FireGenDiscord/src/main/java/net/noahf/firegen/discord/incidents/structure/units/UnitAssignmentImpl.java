package net.noahf.firegen.discord.incidents.structure.units;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.*;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.discord.Main;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Getter @EqualsAndHashCode(of = {"incident", "unit"})
public class UnitAssignmentImpl implements UnitAssignment {

    public UnitAssignmentImpl(Incident incident,
                              Unit unit,
                              Contributor<?> contributorWhoInitiatedEvent,
                              AssignmentStatus... assignments
    ) {
        this.incident = incident;
        this.unit = unit;
        this.assignments = new LinkedList<>();

        for (AssignmentStatus event : assignments) {
            this.assign(contributorWhoInitiatedEvent, event);
        }
    }

    private final Incident incident;
    private final Unit unit;

    private @Setter RadioChannel radioChannel;
    private final List<AssignmentEvent> assignments;

    @Override
    public AssignmentEvent getLatestAssignment() {
        return this.assignments.getLast();
    }

    public void assign(Contributor<?> contributor, AssignmentStatus newAssignment) {
        this.assign(new AssignmentEvent(LocalDateTime.now(), newAssignment, contributor));
    }

    public void assign(AssignmentEvent newEvent) {
        ((UnitImpl)unit).addAssignment(this);
        Main.incidents.getAssignments().add(this);

        this.assignments.add(newEvent);
    }

    @Override
    @NotNull
    public String toString() {
        return unit.getFormatted() + " (" + this.getLatestAssignment().status().getName() + ")";
    }
}
