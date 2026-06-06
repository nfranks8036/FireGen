package net.noahf.firegen.discord.incidents.structure.units;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.units.AgencyType;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.api.utilities.IdGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@EqualsAndHashCode(of = "ordinal")
@AllArgsConstructor @NoArgsConstructor
@Getter
@Entity @Table(name = "units")
public class UnitImpl implements Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shorthand;
    private String longhand;
    private @Getter(value = AccessLevel.NONE) String formatted;
    private transient Emoji emoji;
    private @Enumerated AgencyType agencyType;
    private @Accessors(fluent = true) int ordinal;

    private transient @Getter SelectOption selectOption;

    private transient @Getter final Set<UnitAssignment> assignments = new LinkedHashSet<>();

    @Override
    @NotNull
    public String toString() {
        return this.shorthand;
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

    public String getFormattedStatus(AssignmentStatusImpl status) {
        if (status != null && status.getEmoji() != null && !status.equals(AssignmentStatusImpl.HIDE_STATUS)) {
            return (this.emoji != null ? emoji.getFormatted() + " " : "") +
                    status.getEmoji().getFormatted() + " " +
                    this.formatted;
        }
        return this.getFormatted();
    }
}
