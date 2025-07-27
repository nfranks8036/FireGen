package net.noahf.firewatch.common.geolocation.exceptions;


public class NoDataProvidedException extends GeoLocatorException {
    public NoDataProvidedException(String query, String msg) {
        super(query, msg);
    }

    public NoDataProvidedException(String query, String msg, Throwable cause) {
        super(query, msg, cause);
    }
}
