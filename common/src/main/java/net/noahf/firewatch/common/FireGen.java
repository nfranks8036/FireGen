package net.noahf.firewatch.common;

import net.noahf.firewatch.common.agency.AgencyManager;
import net.noahf.firewatch.common.geolocation.GeoLocator;
import net.noahf.firewatch.common.incidents.IncidentManager;
import net.noahf.firewatch.common.newincidents.IncidentStructure;

public class FireGen {

    private final IncidentStructure incidentStructure;
    private final IncidentManager incidentManager;
    private final GeoLocator geoLocator;
    private final AgencyManager agencyManager;

    public FireGen(String incidentStructureLocation) {
        this.incidentStructure = IncidentStructure.create(incidentStructureLocation);
        this.incidentManager = new IncidentManager();
        this.geoLocator = new GeoLocator();
        this.agencyManager = new AgencyManager();
    }

    public IncidentStructure incidentStructure() { return this.incidentStructure; }

    public IncidentManager incidentManager() {
        return this.incidentManager;
    }

    public GeoLocator geoLocator() {
        return this.geoLocator;
    }

    public AgencyManager agencyManager() { return this.agencyManager; }



}
