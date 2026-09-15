package net.noahf.firegen.discord.incidents.structure.location;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.noahf.firegen.api.incidents.location.LocationType;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.config.files.ConfigVenues;
import net.noahf.firegen.discord.utilities.JsonUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class LocationPreset extends IncidentLocationImpl {

    public LocationPreset(String key, JsonObject object, ConfigVenues venues) {
        super(new ArrayList<>(List.of(key)));
        try {
            JsonElement locationElement  = JsonUtilities.element(object, "location");

            String[] entireAddress;
            if (locationElement != null) {
                entireAddress = this.splitAddress(locationElement.getAsString());
            } else {
                entireAddress = this.splitAddress(key);
            }

            String numerics = entireAddress[0];
            String street = entireAddress[1];

            JsonElement unitElement = JsonUtilities.element(object, "unit", true);
            if (unitElement != null) {
                street = street + " " + unitElement.getAsString();
            }

            JsonElement venueElement = JsonUtilities.element(object, "venue", true);
            LocationVenue venue = null;
            if (venueElement != null) {
                venue = venues.fromName(venueElement.getAsString());
            }

            JsonElement commonNameElement = JsonUtilities.element(object, "common_name", true);
            String commonName = null;
            if (commonNameElement != null) {
                commonName = commonNameElement.getAsString();
            }

            if (!numerics.isEmpty()) {
                this.setType(LocationType.ADDRESS);
                this.setData(List.of(numerics, street));
                this.setCommonName(commonName);
            } else {
                this.setType(LocationType.MILE_MARKER);
                this.setData(Stream.of(commonName, street).filter(Objects::nonNull).toList());
            }

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

        String first = address.substring(0, i);
        String second = address.substring(i + 1).trim();
        try {
            Integer.parseInt(first);
        } catch (NumberFormatException ignored) {
            return new String[]{"", address};
        }

        return new String[] {first, second};
    }
}
