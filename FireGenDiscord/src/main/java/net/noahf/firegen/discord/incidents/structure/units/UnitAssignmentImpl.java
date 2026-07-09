package net.noahf.firegen.discord.incidents.structure.units;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.*;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor(force = true)
@Getter @EqualsAndHashCode(of = {"incident", "unit"})
@Entity @Table(name = "assignments")
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(
            targetEntity = IncidentImpl.class, cascade = CascadeType.ALL
    )
    private final Incident incident;

    @OneToOne(
            targetEntity = UnitImpl.class, cascade = CascadeType.ALL
    )
    private final Unit unit;

    @OneToOne(
            targetEntity = RadioChannelImpl.class, cascade = CascadeType.ALL
    )
    private @Setter RadioChannel radioChannel;

    @OneToMany(
            targetEntity = AssignmentEventImpl.class, cascade = CascadeType.ALL
    )
    private List<AssignmentEvent> assignments;

    @Override
    public AssignmentEvent getLatestAssignment() {
        return this.assignments.getLast();
    }

    public void assign(Contributor<?> contributor, AssignmentStatus newAssignment) {
        this.assign(new AssignmentEventImpl(LocalDateTime.now(), newAssignment, contributor));
    }

    public void assign(AssignmentEvent newEvent) {
        ((UnitImpl)unit).addAssignment(this);
        Main.incidents.getAssignments().add(this);

        this.assignments.add(newEvent);
    }

    @Override
    @NotNull
    public String toString() {
        return unit.getFormatted() + " (" + this.getLatestAssignment().getStatus().getName() + ")";
    }

    @Override
    public int compareTo(@NotNull UnitAssignment o) {
        return Comparator
                .comparingInt((UnitAssignment ua) -> ua.getLatestAssignment().getStatus().ordinal())
                .thenComparingInt(ua -> ua.getUnit().ordinal())
                .compare(this, o);
    }
}
