package net.noahf.firegen.discord.incidents.structure.units;

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
import java.util.StringJoiner;

@EqualsAndHashCode(of = "ordinal")
@RequiredArgsConstructor @NoArgsConstructor(force = true)
@Getter
public class UnitImpl implements Unit {

    private Long id = -1L;

    private final String shorthand;
    private final String longhand;
    private final @Getter(value = AccessLevel.NONE) String formatted;
    private final transient Emoji emoji;
    private final Agency agency;
    private final @Accessors(fluent = true) int ordinal;

    private final boolean isPlaceholder;
    private final transient @Getter SelectOption selectOption;

    private transient @Getter final Set<UnitAssignment> assignments = new LinkedHashSet<>();

    @Override
    @NotNull
    public String toString() {
        return (this.shorthand != null ? this.shorthand : "[Unit " + id + "]");
    }

    public String toStringJava() {
        return new StringJoiner(", ", UnitImpl.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("shorthand='" + shorthand + "'")
                .add("longhand='" + longhand + "'")
                .add("formatted='" + formatted + "'")
                .add("emoji=" + emoji)
                .add("agency=" + agency)
                .add("ordinal=" + ordinal)
                .add("isPlaceholder=" + isPlaceholder)
                .add("selectOption=" + selectOption)
                .add("assignments=" + assignments)
                .toString();
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

        SecondaryImpl secondary = (SecondaryImpl) assignment.getSecondary();
        if (secondary != null) {
            returned = returned + " (" +
                    (secondary.getEmoji() != null ? secondary.getEmoji().getFormatted() + " " : "")
                    + "*" + secondary.getShortName() + "*)";
        }

        return returned;
    }
}
