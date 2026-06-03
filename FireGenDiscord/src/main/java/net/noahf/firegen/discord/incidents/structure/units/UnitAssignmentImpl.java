package net.noahf.firegen.discord.incidents.structure.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.RadioChannel;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class UnitAssignmentImpl implements UnitAssignment {

    private final Incident incident;
    private final Unit unit;

    private @Setter RadioChannel radioChannel;
    private List<AssignmentEvent> assignments;

    @Override
    public AssignmentEvent getLatestAssignment() {
        return this.assignments.getLast();
    }

    @Override
    @NotNull
    public String toString() {
        return unit.getFormatted() + " (" + this.getLatestAssignment().getName() + ")";
    }

}
