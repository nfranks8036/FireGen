package net.noahf.firegen.discord.incidents.structure.units;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.api.utilities.IdGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

@EqualsAndHashCode(of = "ordinal")
@RequiredArgsConstructor @NoArgsConstructor(force = true)
@Getter
@Entity @Table(name = "units")
public class UnitImpl implements Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = -1L;

    private final String shorthand;
    private final String longhand;
    private final @Getter(value = AccessLevel.NONE) String formatted;
    private final transient Emoji emoji;
    private final @Enumerated Agency agency;
    private final @Accessors(fluent = true) int ordinal;

    private final boolean isPlaceholder;
    private final transient @Getter SelectOption selectOption;

    private transient @Getter final Set<UnitAssignment> assignments = new LinkedHashSet<>();

    @Override
    @NotNull
    public String toString() {
        return (this.shorthand != null ? this.shorthand : "[Unit " + id + "]");
    }

    @Override
    public long getId() {
        return IdGenerator.generateUnitId(this);
    }

    @Override
    public String getFormatted() {
        return (this.emoji != null ? emoji.getFormatted() + " " : "") +
                this.formatted;
    }

    void addAssignment(UnitAssignment a) {
        this.assignments.add(a);
    }

    public String getFormattedStatus(AssignmentEvent assignment) {
        String returned = this.getFormatted();

        AssignmentStatusImpl status = (AssignmentStatusImpl) assignment.getStatus();
        if (status != null && status.getEmoji() != null && !status.equals(AssignmentStatusImpl.ADD_UNIT)) {
            returned = (this.emoji != null ? emoji.getFormatted() + " " : "") +
                    status.getEmoji().getFormatted() + " " +
                    this.formatted;
        }

        if (assignment.getSecondary() != null) {
            returned = returned + " (*" + assignment.getSecondary().getShortName() + "*)";
        }

        return returned;
    }
}
