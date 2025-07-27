package net.noahf.firewatch.common.geolocation.exceptions;

public class NoDataReturnedException extends GeoLocatorException{
    public NoDataReturnedException(String query, String msg) {
        super(query, msg);
    }

    public NoDataReturnedException(String query, String msg, Throwable cause) {
        super(query, msg, cause);
    }
}
