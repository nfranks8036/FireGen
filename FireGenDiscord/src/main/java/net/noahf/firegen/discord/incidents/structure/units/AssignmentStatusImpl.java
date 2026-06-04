package net.noahf.firegen.discord.incidents.structure.units;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.discord.utilities.ansi.AnsiColor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public class AssignmentStatusImpl implements AutofilledCharSequence, net.noahf.firegen.api.incidents.units.AssignmentStatus {

    public static final AssignmentStatus HIDE_STATUS = new AssignmentStatusImpl(
            "ADDED", "ADD", null, new AnsiColor[] {AnsiColor.BACKGROUND_WHITE, AnsiColor.BLACK}, Integer.MIN_VALUE
    );

    public static final AssignmentStatus REMOVE_UNIT = new AssignmentStatusImpl(
            "REMOVED", "REM", null, new AnsiColor[] {AnsiColor.BACKGROUND_BLACK, AnsiColor.WHITE}, Integer.MIN_VALUE + 1
    );

    private final String name;
    private final String shortName;
    private final Emoji emoji;
    private final AnsiColor[] ansiColor;
    private final @Accessors(fluent = true) int ordinal;

    @Override
    @NotNull
    public String toString() {
        return this.name;
    }

}
