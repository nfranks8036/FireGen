package net.noahf.firewatch.common.geolocation;

import java.util.StringJoiner;

public abstract class Address {

    private String commonName;
    private String houseNumbers;
    private String streetName;
    private String city;
    private State state;
    private int zip;

    public Address(String houseNumbers, String streetName, String city, State state, int zip) {
        this.commonName = null;
        this.houseNumbers = houseNumbers;
        this.streetName = streetName;
        this.city = city;
        this.state = state;
        this.zip = zip;
    }

    public String commonName() { return this.commonName; }
    public String houseNumbers() { return this.houseNumbers; }
    public String streetName() { return this.streetName; }
    public String city() { return this.city; }
    public State state() { return this.state; }
    public int zip() { return this.zip; }

    protected void commonName(String newCommonName) { this.commonName = newCommonName; }
    protected void houseNumbers(String newHouseNumbers) { this.houseNumbers = newHouseNumbers; }
    protected void streetName(String newStreetName) { this.streetName = newStreetName; }
    protected void city(String newCity) { this.city = newCity; }
    protected void state(State newState) { this.state = newState; }
    protected void zip(int newZip) { this.zip = newZip; }

    public void copyFrom(Address address) {
        this.commonName = copyOrElse(address.commonName, this.commonName);
        this.houseNumbers = copyOrElse(address.houseNumbers, this.houseNumbers);
        this.streetName = copyOrElse(address.streetName, this.streetName);
        this.city = copyOrElse(address.city, this.city);
        this.state = copyOrElse(address.state, this.state);
        this.zip = copyOrElse(address.zip, this.zip);
    }

    private <T> T copyOrElse(T main, T other) {
        if (main == null) {
            return other;
        }
        return main;
    }

    @Override
    public String toString() {
        return Address.formString(commonName, houseNumbers, streetName, city, state, zip);
    }

    public static String formString(String commonName, String houseNumbers, String streetName, String city, State state, int zip) {
        StringJoiner joiner = new StringJoiner(", ");
        if (commonName != null)
            joiner.add(commonName);

        StringJoiner streetAddress = new StringJoiner(" ");
        if (houseNumbers != null)
            streetAddress.add(houseNumbers);
        if (streetName != null)
            streetAddress.add(streetName);
        if (streetAddress.length() != 0)
            joiner.add(streetAddress.toString());

        if (city != null)
            joiner.add(city);
        if (state != null)
            joiner.add(state.abbreviate());

        String stringVersion = joiner.toString();
        if (zip != 0)
            stringVersion = stringVersion + " " + zip;

        return stringVersion;
    }

}
