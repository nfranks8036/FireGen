package net.noahf.firewatch.common;

import net.noahf.firewatch.common.agency.AgencyManager;
import net.noahf.firewatch.common.geolocation.GeoLocator;
import net.noahf.firewatch.common.incidents.IncidentManager;

public class FireGen {

    private final IncidentManager incidentManager;
    private final GeoLocator geoLocator;
    private final AgencyManager agencyManager;

    public FireGen() {
        this.incidentManager = new IncidentManager();
        this.geoLocator = new GeoLocator();
        this.agencyManager = new AgencyManager();
    }

    public IncidentManager incidentManager() {
        return this.incidentManager;
    }

    public GeoLocator geoLocator() {
        return this.geoLocator;
    }

    public AgencyManager agencyManager() { return this.agencyManager; }



}
