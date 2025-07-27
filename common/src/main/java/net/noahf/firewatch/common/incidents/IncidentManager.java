package net.noahf.firewatch.common.incidents;

import net.noahf.firewatch.common.FireGen;
import net.noahf.firewatch.common.geolocation.GeoAddress;
import net.noahf.firewatch.common.geolocation.IncidentAddress;
import net.noahf.firewatch.common.geolocation.State;
import net.noahf.firewatch.common.units.Unit;

import java.util.ArrayList;
import java.util.List;

public class IncidentManager {

    public final List<Incident> active;

    public IncidentManager() {
        this.active = new ArrayList<>();
    }

    public Incident getIncident(String incidentNumber) {
        for (Incident incident : this.active) {
            if (incident.getIncidentNumber().equalsIgnoreCase(incidentNumber)) {
                return incident;
            }
        }
        return null;
    }

    public Incident post(Incident incident) {
        this.active.add(incident);
        return incident;
    }



}
