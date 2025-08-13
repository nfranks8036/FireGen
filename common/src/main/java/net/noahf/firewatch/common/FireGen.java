package net.noahf.firewatch.common;

import net.noahf.firewatch.common.data.IncidentStructure;
import net.noahf.firewatch.common.geolocation.GeoLocator;
import net.noahf.firewatch.common.incidents.IncidentManager;
import net.noahf.firewatch.common.loader.JsonImporter;
import net.noahf.firewatch.common.units.AgencyManager;

public class FireGen {

    private static FireGen instance = null;

    public static FireGen get() {
        if (FireGen.instance == null) {
            throw new RuntimeException("FireGen instance is null!");
        }
        return instance;
    }

    public static FireGen start(String municipality) {
        FireGen.instance = new FireGen(municipality);
        return FireGen.get();
    }





    private final GeoLocator geoLocator;
    private final IncidentStructure structure;
    private final AgencyManager agencyManager;
    private final IncidentManager incidentManager;

    private FireGen(String municipality) {
        FireGen.instance = this;

        JsonImporter importer = new JsonImporter(municipality);

        this.geoLocator = new GeoLocator();
        this.structure = importer.importedIncidentStructure();
        this.agencyManager = importer.importedAgencyManager();
        this.incidentManager = new IncidentManager();
    }

    public GeoLocator geoLocator() {
        return this.geoLocator;
    }

    public IncidentStructure incidentStructure() { return this.structure; }

    public AgencyManager agencyManager() { return this.agencyManager; }

    public IncidentManager incidentManager() { return this.incidentManager; }

}
