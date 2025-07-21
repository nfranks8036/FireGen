package net.noahf.firewatch.common.geolocation;

import java.util.StringJoiner;

public class GeoAddress extends Address{

    private final Coordinates coords;

    public GeoAddress(String commonName, String houseNumbers, String streetAddress, String city, State state, int zip, Coordinates coords) {
        super(houseNumbers, streetAddress, city, state, zip);
        this.commonName(commonName);
        this.coords = coords;
    }

    public Coordinates coords() {
        return this.coords;
    }
}
