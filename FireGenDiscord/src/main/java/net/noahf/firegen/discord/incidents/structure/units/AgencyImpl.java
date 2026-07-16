package net.noahf.firegen.discord.incidents.structure.units;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.incidents.units.AgencyType;
import net.noahf.firegen.api.incidents.units.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AgencyImpl implements Agency {

    private final String title;
    private final String shorthand;
    private final String formatted;
    private final String station;
    private final AgencyType type;
    private final @Nullable transient Emoji emoji;
    private final @Accessors(fluent = true) int ordinal;
    private final List<Unit> units;
    private final int startUnitOrdinal;

}
