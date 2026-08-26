package net.noahf.firegen.discord.incidents.structure.units;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.noahf.firegen.api.incidents.units.UnitType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.StringJoiner;

@Getter
@AllArgsConstructor
public class UnitTypeImpl implements UnitType {

    public static final UnitType CUSTOM = new UnitTypeImpl(
            "$CUSTOM", null, null, null, "No description provided."
    );
    public static final UnitType AGENCY = new UnitTypeImpl(
            "$AGENCY", null, null, null, "An agency placeholder. No description provided."
    );

    private final String id;
    private final @Nullable String shorthand;
    private final @Nullable String longhand;
    private final @Nullable String formatted;
    private final String description;

    @Override
    public @NonNull String toString() {
        return this.getId();
    }
}
