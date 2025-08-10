package net.noahf.firewatch.common;

import net.noahf.firewatch.common.agency.AgencyManager;
import net.noahf.firewatch.common.geolocation.GeoLocator;
import net.noahf.firewatch.common.incidents.IncidentManager;
import net.noahf.firewatch.common.newincidents.IncidentStructure;

public class FireGen {

    private final IncidentStructure incidentStructure;
    private final AgencyManager agencyManager;
    private final IncidentManager incidentManager;
    private final GeoLocator geoLocator;

    public FireGen(String municipality) {
        this.incidentStructure = IncidentStructure.create(municipality);
        this.agencyManager = new AgencyManager();
        this.incidentManager = new IncidentManager();
        this.geoLocator = new GeoLocator();
    }

    public IncidentStructure incidentStructure() { return this.incidentStructure; }

    public AgencyManager agencyManager() { return this.agencyManager; }

    public IncidentManager incidentManager() {
        return this.incidentManager;
    }

    public GeoLocator geoLocator() {
        return this.geoLocator;
    }



}
