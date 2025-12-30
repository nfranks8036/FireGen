package net.noahf.firegen.backend.database;

import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.config.MorphiaConfig;
import dev.morphia.config.MorphiaConfigHelper;
import jakarta.annotation.Nullable;
import net.noahf.firegen.backend.utils.Log;

import java.sql.*;
import java.util.StringJoiner;

public class DatabaseInitializer {

    public static final String DEFAULT_URL = "localhost:27017";

    final MorphiaConfig config;
    final Datastore datastore;

    public DatabaseInitializer(@Nullable String user, @Nullable String pass) {
        Log.debug("Initializing database with: [user=" + user + ", password=" + (pass != null ? "****" : "null") + "]");

        this.config = MorphiaConfig.load().database("firegendb").storeNulls(true).storeEmpties(true);
        this.datastore = Morphia.createDatastore(MongoClients.create(), this.config);

        String configStr = MorphiaConfigHelper.dumpConfigurationFile(this.config, true);
        StringJoiner config = new StringJoiner(", ", "MorphiaConfig[", "]");
        for (String str : configStr.split("\n")) {
            if (str.startsWith("#"))
                continue;
            config.add(str);
        }
        Log.debug("Database environment: [url='" + DEFAULT_URL + "', config=" + config.toString() + "]");

        Log.debug("Database loaded.");
    }

    public DatabaseManager database() {
        return new DatabaseManager(this);
    }

}
