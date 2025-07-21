package net.noahf.firewatch.common.geolocation.exceptions;

import net.noahf.firewatch.common.geolocation.GeoLocator;

public class GeoLocatorException extends IllegalStateException {

    public GeoLocatorException(String query, String msg) {
        super(msg + ": [" + query + "]");
    }

    public GeoLocatorException(String query, String msg, Throwable cause) {
        super(msg + ": [" + query + "]", cause);
    }

}
