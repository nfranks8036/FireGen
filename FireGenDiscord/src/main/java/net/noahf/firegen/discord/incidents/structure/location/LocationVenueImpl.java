package net.noahf.firegen.discord.incidents.structure.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.api.utilities.IdGenerator;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public class LocationVenueImpl implements LocationVenue {

    private final long id = IdGenerator.generateVenueId(this);
    private final String name, displayName;

    @Override
    public @NotNull String toString() {
        return this.name;
    }

}