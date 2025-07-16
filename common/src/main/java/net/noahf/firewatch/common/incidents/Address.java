package net.noahf.firewatch.common.incidents;

public class Address {

    private String streetAddress;
    private int zipCode;

    private String town;
    private String state;

    public String streetAddress() {
        return this.streetAddress;
    }

    public void streetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public int zipCode() {
        return this.zipCode;
    }

    public void zipCode(int zipCode) {
        this.zipCode = zipCode;
    }

    public String town() {
        return this.town;
    }

    public void town(String town) {
        this.town = town;
    }

    public String state() {
        return this.state;
    }

    public void state(String state) {
        this.state = state;
    }


    public boolean searchFromZip() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return this.streetAddress() + ", " + this.town() + ", " + this.state() + " " + this.zipCode();
    }
}
