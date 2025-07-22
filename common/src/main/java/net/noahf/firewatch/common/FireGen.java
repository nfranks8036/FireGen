package net.noahf.firewatch.common;

import net.noahf.firewatch.common.geolocation.GeoLocator;
import net.noahf.firewatch.common.incidents.IncidentManager;
import net.noahf.firewatch.common.units.UnitManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FireGen {

    private final IncidentManager incidentManager;
    private final GeoLocator geoLocator;
    private final UnitManager unitManager;

    public FireGen() {
        this.incidentManager = new IncidentManager();
        this.geoLocator = new GeoLocator();
        this.unitManager = new UnitManager();
    }

    public IncidentManager callManager() {
        return this.incidentManager;
    }

    public GeoLocator geoLocator() {
        return this.geoLocator;
    }

    public UnitManager unitManager() { return this.unitManager; }
}
