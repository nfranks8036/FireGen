package net.noahf.firewatch.common;

import net.noahf.firewatch.common.incidents.Address;
import net.noahf.firewatch.common.incidents.Incident;
import net.noahf.firewatch.common.incidents.IncidentType;
import net.noahf.firewatch.common.units.Unit;

import java.util.ArrayList;
import java.util.List;

public class IncidentManager {

    public final List<Incident> active;

    public IncidentManager() {
        this.active = new ArrayList<>();

        Address address = new Address();
        address.streetAddress("120 W Jackson Ave");
        address.zipCode(24179);
        address.town("Vinton");
        address.state("VA");
        this.active.add(new Incident(
                System.currentTimeMillis(),
                IncidentType.FIRE_ALARM,
                address,
                new Unit[]{}));
    }

    public Incident getIncident(String incidentNumber) {
        for (Incident incident : this.active) {
            if (incident.getIncidentNumber().equalsIgnoreCase(incidentNumber)) {
                return incident;
            }
        }
        return null;
    }



}
