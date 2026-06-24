package net.noahf.firegen.discord.database;

import jakarta.inject.Inject;
import lombok.Getter;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.discord.database.errors.GenerateSessionFailure;
import net.noahf.firegen.discord.incidents.structure.*;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.location.LocationVenueImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeTagImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeTagQualifierListImpl;
import net.noahf.firegen.discord.incidents.structure.units.*;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Manager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

@Getter
public class DatabaseManager extends Manager<DatabaseManager> {

    private final Configuration config;

    private SessionFactory factory;

    @Inject
    private IncidentDatabase database;

    public DatabaseManager() {
        super(DatabaseManager.class, "Database");

        if (2 > 1) {
            Log.warn("Database disabled. No persistent data will be stored.");
            this.config = null;
            return;
        }

        Log.info("-".repeat(20) + " [ DATABASE START ] " + "-".repeat(20));
        Log.info("Loading the database...");

        // config file located at /resources/hibernate.cfg.xml
        this.config = new Configuration();
        this.config.configure();

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
