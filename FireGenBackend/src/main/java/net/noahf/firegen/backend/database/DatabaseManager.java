package net.noahf.firegen.backend.database;

import dev.morphia.Datastore;
import dev.morphia.config.MorphiaConfig;

public class DatabaseManager {

    private final Datastore datastore;
    private final MorphiaConfig config;

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

}
