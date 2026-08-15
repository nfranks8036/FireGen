package net.noahf.firegen.discord.database;

import lombok.Getter;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Manager;

@Getter
public class DatabaseManager extends Manager<DatabaseManager> {

    private IncidentDatabase database;

    public DatabaseManager() {
        super(DatabaseManager.class, "Database");

        if (2 > 1) {
            Log.warn("Database disabled. No persistent data will be stored.");
            return;
        }

        Log.info("-".repeat(20) + " [ DATABASE START ] " + "-".repeat(20));
        Log.info("Loading the database...");

        Log.info("-".repeat(20) + " [ DATABASE END ] " + "-".repeat(20));
    }

}
