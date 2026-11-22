package net.noahf.firegen.discord.incidents.structure.units;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.IdGenerator;
import net.noahf.firegen.api.incidents.units.*;
import net.noahf.firegen.api.style.StatusStyle;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.config.files.ConfigOperationalStatuses;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Time;
import net.noahf.firegen.discord.web.serializers.UnitSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;

@RequiredArgsConstructor @NoArgsConstructor(force = true)
@Getter
@JsonSerialize(using = UnitSerializer.class)
@EqualsAndHashCode(of = {"id", "shorthand", "number"})
public class UnitImpl implements Unit {

    private final Long id = -1L;

    private final String shorthand;
    private final String longhand;
    private final @Getter(value = AccessLevel.NONE) String formatted;
    private @Nullable UnitAssignment currentAssignment = null;
    private final Set<UnitAssignment> previousAssignments = new HashSet<>();
    private OperationalStatus operationalStatus = new OperationalStatus() {
        @Override public StatusStyle getStyle() { return StatusStyle.ofDefault(); }
        @Override public int ordinal() { return -1; }
        @Override public @Nullable OperationalStatusPurpose getPurpose() { return null; }
        @Override public String getName() { return "NO STATUS"; }
        @Override public String getShortName() { return "NULL"; }
    };
    private long statusLastUpdate;
    private final transient Emoji emoji;
    private final Agency agency;
    private final UnitType type;
    private final int number;
    private final @Accessors(fluent = true) int ordinal;
    private final @Nullable Object additional;

    private final boolean isPlaceholder;
    private final transient @Getter SelectOption selectOption;

    @Override
    @NotNull
    public String toString() {
        return (this.shorthand != null ? this.shorthand : "[Unit " + id + "]");
    }

    public String toStringJava() {
        return new StringJoiner(", ", UnitImpl.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("type=" + type)
                .add("number=" + number)
                .add("assignment=" + currentAssignment)
                .add("status=" + operationalStatus)
                .add("shorthand='" + shorthand + "'")
                .add("longhand='" + longhand + "'")
                .add("formatted='" + formatted + "'")
                .add("emoji=" + emoji)
                .add("agency=" + agency)
                .add("ordinal=" + ordinal)
                .add("isPlaceholder=" + isPlaceholder)
                .add("selectOption=" + selectOption)
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

    public void assign(UnitAssignment assignment) {
        this.currentAssignment = assignment;

        OperationalStatusPurpose purpose = OperationalStatusPurpose.UNIT_ATTACHED_TO_INCIDENT;
        if (this.currentAssignment == null) {
            purpose = OperationalStatusPurpose.UNIT_AVAILABLE_FOR_CALLS;
        }

        this.previousAssignments.add(assignment);

        this.operationalStatus = Main.config.get(ConfigOperationalStatuses.class).purposeIs(purpose);
        this.statusLastUpdate = Time.getUnix();
    }

    @Override
    public void setOperationalStatus(OperationalStatus status) {
        this.operationalStatus = status;
        this.statusLastUpdate = Time.getUnix();
    }

    public String getFormattedStatus(AssignmentEvent assignment) {
        return getFormattedStatus(assignment.getStatus(), assignment.getSecondary());
    }

    public String getFormattedStatus(AssignmentStatus status, @Nullable Secondary secondary) {
        String returned = this.getFormatted();

        AssignmentStatusImpl statusImpl = (AssignmentStatusImpl) status;
        if (statusImpl != null && statusImpl.getEmoji() != null && !statusImpl.equals(AssignmentStatusImpl.ADD_UNIT)) {
            returned = (this.emoji != null ? emoji.getFormatted() + " " : "") +
                    statusImpl.getEmoji().getFormatted() + " " +
                    (this.formatted != null ? this.formatted.strip() : "(?)");
        }

        SecondaryImpl secondaryImpl = (SecondaryImpl) secondary;
        if (secondaryImpl != null) {
            returned = returned + " (" +
                    (secondaryImpl.emoji() != null ? secondaryImpl.emoji().getFormatted() + " " : "")
                    + "*" + secondaryImpl.shortName() + "*)";
        }

        return returned;
    }

    @Override
    public int compareTo(@NonNull Unit other) {
        return Comparator
                .comparingLong(Unit::getStatusLastUpdate)
                .reversed()
                .thenComparing((a, b) -> {
                    if (a.getType() != null && a.getType().getId().startsWith("$")) {
                        // reverse agencies so they're in order of ordinal (they go from smallest -> biggest)
                        return Integer.compare(b.ordinal(), a.ordinal());
                    }
                    return Integer.compare(a.ordinal(), b.ordinal());
                })
                .compare(this, other);
    }
}
