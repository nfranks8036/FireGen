package net.noahf.firegen.discord.database;

import lombok.Getter;
import net.noahf.firegen.discord.database.errors.GenerateSessionFailure;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Manager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@Getter
public class DatabaseManager extends Manager<DatabaseManager> {

    private Configuration config;
    private Session session;

    public DatabaseManager() {
        super(DatabaseManager.class, "Database");

        if (2 > 1) {
            // disabled for the time being
            return;
        }

        Log.info("Loading the database...");

        // config file located at /resources/hibernate.cfg.xml
        this.config = new Configuration();
        this.config.configure();

        this.generateSession();
    }

    private void generateSession() {
        try (SessionFactory factory = this.config.buildSessionFactory()) {
            this.session = factory.openSession();
        } catch (Exception exception) {
            throw new GenerateSessionFailure(exception);
        }
    }

}
