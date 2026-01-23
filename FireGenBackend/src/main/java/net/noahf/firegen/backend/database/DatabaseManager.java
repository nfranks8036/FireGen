package net.noahf.firegen.backend.database;

import dev.morphia.Datastore;
import dev.morphia.config.MorphiaConfig;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Sort;
import net.noahf.firegen.backend.database.structure.Incident;

public class DatabaseManager {

    private final Datastore datastore;
    private final MorphiaConfig config;

    private long refreshing = -1;
    private int incidentCount = -1;

    DatabaseManager(DatabaseInitializer init) {
        this.datastore = init.datastore;
        this.config = init.config;
    }

    public Datastore datastore() {
        return this.datastore;
    }

    public MorphiaConfig config() {
        return this.config;
    }

    public int nextIncident() {
        System.out.println("Finding next incident...");

        if (this.incidentCount == -1) {
            this.refreshIncidentCount();
        }

        this.incidentCount = this.incidentCount + 1;
        System.out.println("Next incident number should be " + this.incidentCount);
        return this.incidentCount;
    }

    private int refreshIncidentCount() {
        if (System.currentTimeMillis() - this.refreshing <= 20_000) {
            System.out.println("Returning -1 because program attempted to call method WHILE refreshing!");
            return -1;
        }

        this.refreshing = System.currentTimeMillis();
        System.out.println("[!] Program must refresh incident numbers...");
        System.out.println("[!] Refreshing...");
        Incident highestIncidentNumber = this.datastore.find(
                        Incident.class,
                        new FindOptions().sort(Sort.descending("incidentNumber")).limit(1)
                )
                .first();
        System.out.println("[!] Found most recent incident: " + highestIncidentNumber);

        this.incidentCount = 0;
        if (highestIncidentNumber != null) {
            this.incidentCount = (int) highestIncidentNumber.incidentNumber;
            System.out.println("[!] Current count is: " + this.incidentCount);
        }
        this.refreshing = -1;
        return this.incidentCount;
    }

    public int countIncidents() {
        return this.incidentCount == -1 ? this.refreshIncidentCount() : this.incidentCount;
    }

}
