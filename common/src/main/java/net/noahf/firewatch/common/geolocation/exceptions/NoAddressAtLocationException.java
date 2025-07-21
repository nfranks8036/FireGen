package net.noahf.firewatch.common.geolocation.exceptions;

public class NoAddressAtLocationException extends GeoLocatorException {

    public NoAddressAtLocationException(String query, String msg, Throwable cause) {
        super(query, msg, cause);
    }

    public NoAddressAtLocationException(String query, String msg) {
        super(query, msg);
    }
}
