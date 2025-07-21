package net.noahf.firewatch.common.geolocation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.noahf.firewatch.common.geolocation.exceptions.GeoLocatorException;
import net.noahf.firewatch.common.geolocation.exceptions.NoAddressAtLocationException;
import net.noahf.firewatch.common.geolocation.exceptions.NoDataGivenException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Supplier;

public class GeoLocator {

    private final Map<String, GeoAddress> knownAddresses;

    public GeoLocator() {
        this.knownAddresses = new HashMap<>();
    }

    public GeoAddress find(String commonName, String houseNumbers, String streetAddress, String town, State state, int zipCode) {
        return this.find(Address.formString(commonName, houseNumbers, streetAddress, town, state, zipCode));
    }

    public GeoAddress find(String query) {
        if (knownAddresses.containsKey(query.toLowerCase())) {
            return knownAddresses.get(query.toLowerCase());
        }

        try {
            String params = "?addressdetails=1&format=jsonv2&limit=1&q=" + URLEncoder.encode(query, Charset.defaultCharset());
            URI uri = URI.create("https://nominatim.openstreetmap.org/search" + params);
            BufferedReader reader = new BufferedReader(new InputStreamReader(uri.toURL().openStream()));
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            if (array.isEmpty()) {
                throw new NoDataGivenException(query, "No items in array from " + uri.toString());
            }
            JsonObject object = array.get(0).getAsJsonObject();

            Coordinates coords = new Coordinates(
                    object.get("lat").getAsDouble(),
                    object.get("lon").getAsDouble()
            );

            String name = this.tryOrNull(() -> object.get("name").getAsString());

            JsonObject jsonAddress = this.tryOrNull(() -> object.get("address").getAsJsonObject());
            if (jsonAddress == null) {
                throw new NoAddressAtLocationException(query, "No address found at given input");
            }

            String houseNumber = this.tryOrNull(() -> jsonAddress.get("house_number").getAsString());
            String road = this.tryOrNull(() -> jsonAddress.get("road").getAsString());
            String city = this.tryOrNull(() -> jsonAddress.get("city").getAsString());
            State state = this.tryOrNull(() -> State.valueOf(jsonAddress.get("state").getAsString().toUpperCase(Locale.ROOT)));
            int zipCode = this.tryOrElse(() -> jsonAddress.get("postcode").getAsInt(), 0);

            reader.close();

            GeoAddress address = new GeoAddress(name, houseNumber, road, city, state, zipCode, coords);
            this.knownAddresses.put(query.toLowerCase(Locale.ROOT), address);
            return address;
        } catch (IOException io) {
            throw new GeoLocatorException(query, "Failed to search for address from API, is it down? Found input", io);
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
            throw exception;
        }
    }

    private <T> T tryOrNull(Supplier<T> obj) { return this.tryOrElse(obj, null); }
    private <T> T tryOrElse(Supplier<T> obj, T other) {
        try {
            return obj.get();
        } catch (Exception exception) {
            return other;
        }
    }

}
