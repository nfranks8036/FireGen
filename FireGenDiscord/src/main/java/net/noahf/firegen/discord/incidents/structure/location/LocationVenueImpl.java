package net.noahf.firegen.discord.incidents.structure.location;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.api.utilities.IdGenerator;
import net.noahf.firegen.discord.web.serializers.LocationVenueSerializer;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(force = true)
@AllArgsConstructor
@Getter
@JsonSerialize(using = LocationVenueSerializer.class)
public class LocationVenueImpl implements LocationVenue {

    private final long id = IdGenerator.generateVenueId(this);

    private final String name, displayName;

    @Override
    public @NotNull String toString() {
        if (this.name == null) return "None";
        return this.name;
    }

}