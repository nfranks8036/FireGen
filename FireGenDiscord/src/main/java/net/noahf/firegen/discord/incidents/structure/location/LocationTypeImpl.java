package net.noahf.firegen.discord.incidents.structure.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.noahf.firegen.api.incidents.location.LocationField;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public class LocationTypeImpl implements net.noahf.firegen.api.incidents.location.LocationType {

    private final String prefix;
    private final String id;
    private final String title;
    private final String shortName;
    private final String description;
    private final String defaultDataDelimiter;
    private final boolean fallback;
    private final LocationField[] fields;

    @Override
    public @NotNull String toString() {
        return this.title;
    }

    @Override
    public String describe() {
        return "**" + this.title + ":** " + this.description;
    }
}
