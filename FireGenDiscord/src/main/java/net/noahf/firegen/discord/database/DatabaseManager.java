package net.noahf.firegen.discord.database;

import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;
import jakarta.inject.Inject;
import lombok.Getter;
import net.noahf.firegen.discord.database.errors.GenerateSessionFailure;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentLogEntryImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentStatusEmoji;
import net.noahf.firegen.discord.incidents.structure.IncidentTimeImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.location.LocationVenueImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeTagImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeTagQualifierListImpl;
import net.noahf.firegen.discord.incidents.structure.units.*;
import net.noahf.firegen.discord.users.FireGenUser;
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

        // config file located at /resources/hibernate.cfg.xml
        Morphia.createDatastore("incidents", MapperOptions.builder().build())

        this.setUp();

        Log.info("-".repeat(20) + " [ DATABASE END ] " + "-".repeat(20));
    }

    private void setUp() {
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure()
                .applySetting("hibernate.hbm2ddl.auto", "update")
                .applySetting("hibernate.show_sql", true)
                .build();
        try {
            org.hibernate.boot.Metadata meta = new MetadataSources(registry)
                    .addAnnotatedClasses(
                            IncidentImpl.class, IncidentLogEntryImpl.class, IncidentStatusEmoji.class,
                            IncidentTimeImpl.class, IncidentLocationImpl.class, LocationVenueImpl.class,
                            IncidentTypeImpl.class, IncidentTypeTagImpl.class, IncidentTypeTagQualifierListImpl.class,
                            AssignmentStatusImpl.class, RadioChannelImpl.class, UnitAssignmentImpl.class,
                            UnitImpl.class, FireGenUser.class, AssignmentEventImpl.class
                    )
                    .buildMetadata();
            this.factory = meta.buildSessionFactory();
        } catch (Exception exception) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw new GenerateSessionFailure(exception);
        }
    }

    private Session generateSession() {
        return this.factory.openSession();
    }

}
