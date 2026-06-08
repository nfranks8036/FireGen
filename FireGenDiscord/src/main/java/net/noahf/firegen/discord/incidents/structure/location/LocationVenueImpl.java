package net.noahf.firegen.discord.incidents.structure.location;

import jakarta.data.Order;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.api.utilities.IdGenerator;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(force = true)
@AllArgsConstructor
@Getter
@Entity
public class LocationVenueImpl implements LocationVenue {

    @Id
    private final long id = IdGenerator.generateVenueId(this);

    private final String name, displayName;

    @Override
    public @NotNull String toString() {
        if (this.name == null) return "None";
        return this.name;
    }

}