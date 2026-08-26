package net.noahf.firegen.discord.incidents.structure.units;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.*;
import net.noahf.firegen.api.utilities.IgnoreStringSelector;
import net.noahf.firegen.api.utilities.ToStringListStringSelector;
import net.noahf.firegen.discord.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor(force = true)
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
            this.assign(contributorWhoInitiatedEvent, event, null);
        }
    }

    private Long id;

    @Getter(onMethod_ = {@IgnoreStringSelector})
    private final Incident incident;

    @Getter(onMethod_ = {@IgnoreStringSelector})
    private final Unit unit;

    private @Setter RadioChannel radioChannel;

    private List<AssignmentEvent> assignments;

    @Override
    public AssignmentEvent getLatestAssignment() {
        return this.assignments.getLast();
    }

    public void assign(Contributor<?> contributor, AssignmentStatus newAssignment, @Nullable Secondary secondary) {
        this.assign(new AssignmentEventImpl(LocalDateTime.now(), newAssignment, contributor, secondary));
    }

    public void assign(AssignmentEvent newEvent) {
        ((UnitImpl)unit).addAssignment(this);
        Main.incidents.getAssignments().add(this);

        this.assignments.add(newEvent);
    }

    @Override
    @NotNull
    public String toString() {
        AssignmentEvent latest = this.getLatestAssignment();
        return unit.getShorthand() + ":" + latest.getStatus().getName()
                + (latest.getSecondary() != null ? ">" + latest.getSecondary().toString() : "")
                ;
    }

    @Override
    public int compareTo(@NotNull UnitAssignment o) {
        return Comparator
                .comparingInt((UnitAssignment ua) -> ua.getLatestAssignment().getStatus().ordinal())
                .thenComparingInt(ua -> ua.getUnit().ordinal())
                .compare(this, o);
    }
}
