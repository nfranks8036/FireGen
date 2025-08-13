package net.noahf.firewatch.common.incidents;

import net.noahf.firewatch.common.FireGen;
import net.noahf.firewatch.common.data.IncidentStatus;
import net.noahf.firewatch.common.utils.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncidentManager {

    private final Map<Identifier, Incident> incidents;

    public IncidentManager() {
        this.incidents = new HashMap<>();
    }

    public Incident generate() {
        Incident incident = new Incident();
        this.incidents.put(incident.identifier(), incident);
        return incident;
    }

    public Incident findById(Identifier id) {
        return this.incidents.get(id);
    }

    public int countAll() { return this.incidents.size(); }

    public List<Incident> findActive() {
        return this.incidents.values().stream()
                .filter(i -> !i.status().equals(FireGen.get().incidentStructure().incidentStatuses().marked(IncidentStatus.CLOSED_INCIDENT).one()))
                .toList();
    }

    public int countActive() { return this.findActive().size(); }

}
