package net.noahf.firegen.discord.incidents.structure.location;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.location.LocationType;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LocationPreset extends IncidentLocationImpl {

    public LocationPreset(IncidentManager manager, String key, JsonObject object) {
        super(new ArrayList<>(List.of(key)));
        try {
            JsonElement locationElement  = object.get("location");

            String[] entireAddress;
            if (locationElement != null) {
                entireAddress = this.splitAddress(locationElement.getAsString());
            } else {
                entireAddress = this.splitAddress(key);
            }

            String numerics = entireAddress[0];
            String street = entireAddress[1];

            JsonElement unitElement = object.get("unit");
            if (unitElement != null) {
                street = street + " " + unitElement.getAsString();
            }

            JsonElement venueElement = object.get("venue");
            LocationVenue venue = null;
            if (venueElement != null) {
                venue = manager.getVenueBy(venueElement.getAsString());
            }

            JsonElement commonNameElement = object.get("common_name");
            String commonName = null;
            if (commonNameElement != null) {
                commonName = commonNameElement.getAsString();
            }

            this.setData(List.of(numerics, street));
            this.setType(LocationType.ADDRESS);
            this.setCommonName(commonName);
            this.setVenue(venue);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "An error occurred importing \"" + object.toString() + "\" (key=\"" + key + "\"): " + exception,
                    exception
            );
        }
    }

    private String[] splitAddress(String address) {
        if (address == null) return new String[]{"", ""};

        address = address.trim();

        int i = address.indexOf(' ');
        if (i == -1) return new String[]{"", address};

        return new String[] {
                address.substring(0, i),
                address.substring(i + 1).trim()
        };
    }
}
