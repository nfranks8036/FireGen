package net.noahf.firegen.discord.incidents.structure.units;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.discord.utilities.ansi.AnsiColor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@RequiredArgsConstructor
@Entity
public class AssignmentStatusImpl implements AutofilledCharSequence, net.noahf.firegen.api.incidents.units.AssignmentStatus {

    public static final AssignmentStatus ADD_UNIT = new AssignmentStatusImpl(
            "ADDED", "ADD", null, new AnsiColor[] {AnsiColor.BACKGROUND_WHITE, AnsiColor.BLACK}, Integer.MIN_VALUE
    );

    public static final AssignmentStatus REMOVE_UNIT = new AssignmentStatusImpl(
            "REMOVED", "REM", null, new AnsiColor[] {AnsiColor.BACKGROUND_BLACK, AnsiColor.WHITE}, Integer.MAX_VALUE
    );

    public AssignmentStatusImpl(String name, String shortName, Emoji emoji, AnsiColor[] ansiColor, int ordinal) {
        this(name, shortName,
                (emoji != null ? emoji.getFormatted() : null),
                Arrays.stream(ansiColor).map(Enum::name).toArray(String[]::new),
                ordinal
        );
    }

    private @Getter @Id @GeneratedValue(strategy = GenerationType.AUTO) long id;

    private final @Getter String name;
    private final @Getter String shortName;

    private final String emojiString;
    private final String[] ansiColorStrings;

    private final @Getter @Accessors(fluent = true) int ordinal;

    private transient Emoji emoji = null;
    private transient AnsiColor[] ansiColor = null;

    public Emoji getEmoji() {
        if (emoji == null) {
            if (emojiString == null) return null;

            this.emoji = Emoji.fromFormatted(this.emojiString);
        }
        return this.emoji;
    }

    public AnsiColor[] getAnsiColor() {
        if (ansiColor == null) {
            if (ansiColorStrings == null) return null;

            this.ansiColor = new AnsiColor[ansiColorStrings.length];
            for (int i = 0; i < this.ansiColor.length; i++) {
                this.ansiColor[i] = AnsiColor.valueOf(this.ansiColorStrings[i]);
            }
        }
        return this.ansiColor;
    }

    @Override
    @NotNull
    public String toString() {
        return this.name;
    }

}
