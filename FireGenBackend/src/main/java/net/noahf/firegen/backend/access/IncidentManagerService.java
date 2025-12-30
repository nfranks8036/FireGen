package net.noahf.firegen.backend.access;

import dev.morphia.InsertOneOptions;
import dev.morphia.InsertOptions;
import dev.morphia.UpdateOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filter;
import net.noahf.firegen.backend.Main;
import net.noahf.firegen.backend.database.structure.Incident;
import net.noahf.firegen.backend.database.structure.helper.IncidentStatus;
import net.noahf.firegen.backend.utils.Identifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static dev.morphia.query.filters.Filters.*;

@Service
public class IncidentManagerService {

    public List<Incident> getActiveIncidents() {
        return Main.db.datastore().find(Incident.class)
                .filter(
                        or(
                                eq("incidentStatus", IncidentStatus.NEW.name()),
                                eq("incidentStatus", IncidentStatus.RESOLVED.name()),
                                eq("incidentStatus", IncidentStatus.IN_PROGRESS.name())
                        )
                )
                .stream().toList();
    }


    private Query<Incident> queryIncident(String idString) {
        Identifier id = Identifier.from(idString);
        return Main.db.datastore().find(Incident.class).filter(
                and(
                        eq("incidentYear", id.year()), eq("incidentNumber", id.id()))
        );
    }

    public Incident getIncidentById(String id) {
        return this.queryIncident(id)
                .first();
    }

    public Incident updateIncident(String idString, Incident incident) {
        Identifier id = Identifier.from(idString);
        incident.setIdentifier(id);
        return Main.db.datastore().save(incident);
    }

    public boolean createIncident(Incident incident) {
        Main.db.datastore().insert(incident);
        return true;
    }



}
