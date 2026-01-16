package net.noahf.firegen.backend.access;

import dev.morphia.query.Query;
import net.noahf.firegen.backend.Main;
import net.noahf.firegen.backend.database.structure.Incident;
import net.noahf.firegen.backend.database.structure.IncidentLogEntry;
import net.noahf.firegen.backend.database.structure.helper.IncidentLogType;
import net.noahf.firegen.backend.database.structure.helper.IncidentStatus;
import net.noahf.firegen.backend.utils.Identifier;
import org.springframework.stereotype.Service;

import java.util.List;

import static dev.morphia.query.filters.Filters.*;

@Service
public class IncidentManagerService {

    public List<Incident> getActiveIncidents() {
        return Main.db.datastore().find(Incident.class)
                .filter(
                        or(
                                eq("incidentStatus", IncidentStatus.PENDING.name()),
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
        incident.log.add(IncidentLogEntry.of(IncidentLogType.INCIDENT_CREATED, "Created incident with ID " + incident.getFullId()));
        Main.db.datastore().insert(incident);
        return true;
    }



}
