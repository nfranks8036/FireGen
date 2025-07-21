package net.noahf.firewatch.common.geolocation.exceptions;

public class NoDataGivenException extends GeoLocatorException{
    public NoDataGivenException(String query, String msg) {
        super(query, msg);
    }

    public NoDataGivenException(String query, String msg, Throwable cause) {
        super(query, msg, cause);
    }
}
