package net.noahf.firegen.discord.incidents.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public class AssignmentStatus implements AutofilledCharSequence {

    public static final AssignmentStatus HIDE_STATUS = new AssignmentStatus(
            "[HIDE STATUS]", "HID", null
    );

    public static final AssignmentStatus REMOVE_AGENCY = new AssignmentStatus(
            "[REMOVE]", "REM", null
    );

    private final String name;
    private final String shortName;
    private final Emoji emoji;

    @Override
    @NotNull
    public String toString() {
        return this.name;
    }
}
