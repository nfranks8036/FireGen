package net.noahf.firewatch.common.geolocation;

public class IncidentAddress extends Address {

    public static IncidentAddress address(String houseNumbers, String streetName, String city, State state, int zip) {
        return new IncidentAddress(houseNumbers, streetName, city, state, zip);
    }

    private IncidentAddress(String houseNumbers, String streetName, String city, State state, int zip) {
        super(houseNumbers, streetName, city, state, zip);
    }

    @Override
    public void commonName(String newCommonName) { super.commonName(newCommonName); }

    @Override
    public void houseNumbers(String newHouseNumbers) { super.houseNumbers(newHouseNumbers); }

    @Override
    public void streetName(String newStreetName) {
        super.streetName(newStreetName);
    }

    @Override
    public void city(String newCity) {
        super.city(newCity);
    }

    @Override
    public void state(State newState) {
        super.state(newState);
    }

    @Override
    public void zip(int newZip) {
        super.zip(newZip);
    }

    public GeoAddress geoAddress(GeoLocator geoLocator) {
        return geoLocator.find(this.commonName(), this.houseNumbers(), this.streetName(), this.city(), this.state(), this.zip());
    }

}
