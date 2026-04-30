package net.noahf.firegen.discord.incidents.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public class AssignmentStatus implements AutofilledCharSequence {

    public static final AssignmentStatus HIDE_STATUS = new AssignmentStatus(
            "[HIDE STATUS]", "HID", null, Integer.MIN_VALUE + 1
    );

    public static final AssignmentStatus REMOVE_AGENCY = new AssignmentStatus(
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
