package net.noahf.firegen.discord.incidents.structure.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.components.Component;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
@AllArgsConstructor
@Getter
public class LocationVenueImpl implements LocationVenue {

    private final long id;
    private final @Accessors(fluent = true) String name, displayName;

    @Override
    public String toString() {
        return this.name;
    }

}