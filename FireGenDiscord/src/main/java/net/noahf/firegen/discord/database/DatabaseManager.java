package net.noahf.firegen.discord.database;

import lombok.Getter;
import net.noahf.firegen.api.database.IncidentRepository;
import net.noahf.firegen.discord.database.errors.GenerateSessionFailure;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeTagQualifierListImpl;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Manager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@Getter
public class DatabaseManager extends Manager<DatabaseManager> {

    private Configuration config;
    private Session session;

    private IncidentRepository repository;

    public DatabaseManager() {
        super(DatabaseManager.class, "Database");

        Log.info("-".repeat(20) + " [ DATABASE START ] " + "-".repeat(20));
        Log.info("Loading the database...");

        // config file located at /resources/hibernate.cfg.xml
        this.config = new Configuration();
        this.config.configure();

        this.repository = new IncidentDatabase(this);

        this.generateSession();

        Log.info("-".repeat(20) + " [ DATABASE END ] " + "-".repeat(20));
    }

    private void generateSession() {
        try (SessionFactory factory = this.config.buildSessionFactory()) {
            this.session = factory.openSession();
        } catch (Exception exception) {
            throw new GenerateSessionFailure(exception);
        }
    }

}
