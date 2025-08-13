package net.noahf.firewatch.common;

import net.noahf.firewatch.common.data.IncidentStructure;
import net.noahf.firewatch.common.data.loader.JsonImporter;
import net.noahf.firewatch.common.geolocation.GeoLocator;

public class FireGen {

    private final GeoLocator geoLocator;
    private final IncidentStructure structure;

    public FireGen(String municipality) {
        JsonImporter importer = new JsonImporter(municipality);

        this.geoLocator = new GeoLocator();
        this.structure = importer.importedIncidentStructure();
    }

    public GeoLocator geoLocator() {
        return this.geoLocator;
    }

    public IncidentStructure incidentStructure() { return this.structure; }

}
