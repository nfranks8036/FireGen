package net.noahf.firegen.discord.incidents.structure.units;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.AutofilledCharSequence;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.api.incidents.units.AssignmentStatusPurpose;
import net.noahf.firegen.api.incidents.units.Secondary;
import net.noahf.firegen.api.style.StatusBackground;
import net.noahf.firegen.api.style.StatusColor;
import net.noahf.firegen.api.style.StatusStyle;
import net.noahf.firegen.discord.web.serializers.AssignmentStatusSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@JsonSerialize(using = AssignmentStatusSerializer.class)
public class AssignmentStatusImpl implements AutofilledCharSequence, net.noahf.firegen.api.incidents.units.AssignmentStatus {

    public static final AssignmentStatus ADD_UNIT = new AssignmentStatusImpl(
            "ADDED", "ADD", StatusStyle.of(StatusColor.BLACK, StatusBackground.WHITE), null, Integer.MIN_VALUE, AssignmentStatusPurpose.UNIT_ATTACHED,
            new ArrayList<>()
    );

    public static final AssignmentStatus REMOVE_UNIT = new AssignmentStatusImpl(
            "REMOVED", "REM", StatusStyle.ofDefault(), null, Integer.MAX_VALUE, AssignmentStatusPurpose.UNIT_REMOVED,
            new ArrayList<>()
    );

    public AssignmentStatusImpl(String name, String shortName, StatusStyle style, Emoji emoji, int ordinal, @Nullable AssignmentStatusPurpose purpose, List<Secondary> secondaries) {
        this(name, shortName, style, ordinal, purpose, (emoji != null ? emoji.getFormatted() : null), secondaries);
    }

    private @Getter long id;

    private final @Getter String name;
    private final @Getter String shortName;

    private final @Getter StatusStyle style;
    private final @Getter @Accessors(fluent = true) int ordinal;
    private final @Getter @Nullable AssignmentStatusPurpose purpose;

    private final String emojiString;

    private transient Emoji emoji = null;

    private @Getter final List<Secondary> secondaries;

    public Emoji getEmoji() {
        if (emoji == null) {
            if (emojiString == null) return null;

            this.emoji = Emoji.fromFormatted(this.emojiString);
        }
        return this.emoji;
    }

    @Override
    @NotNull
    public String toString() {
        return this.name;
    }

}
