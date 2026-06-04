package net.noahf.firegen.discord.incidents.structure.units;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public class AssignmentStatusImpl implements AutofilledCharSequence, net.noahf.firegen.api.incidents.units.AssignmentStatus {

    public static final AssignmentStatus HIDE_STATUS = new AssignmentStatusImpl(
            "[HIDE STATUS]", "HID", null, Integer.MIN_VALUE + 1
    );

    public static final AssignmentStatus REMOVE_UNIT = new AssignmentStatusImpl(
            "[REMOVE]", "REM", null, Integer.MIN_VALUE
    );

    private final String name;
    private final String shortName;
    private final Emoji emoji;
    private final @Accessors(fluent = true) int ordinal;

    @Override
    @NotNull
    public String toString() {
        return this.name;
    }

}
