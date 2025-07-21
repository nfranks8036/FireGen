package net.noahf.firewatch.common;

import net.noahf.firewatch.common.geolocation.GeoLocator;
import net.noahf.firewatch.common.incidents.IncidentManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FireGen {

    private final IncidentManager incidentManager;
    private final GeoLocator geoLocator;

    public FireGen() {
        this.incidentManager = new IncidentManager();
        this.geoLocator = new GeoLocator();

    }

    public IncidentManager callManager() {
        return this.incidentManager;
    }

    public GeoLocator geoLocator() {
        return this.geoLocator;
    }

}
