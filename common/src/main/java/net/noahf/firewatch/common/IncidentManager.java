package net.noahf.firewatch.common;

import net.noahf.firewatch.common.incidents.*;
import net.noahf.firewatch.common.incidents.location.Address;
import net.noahf.firewatch.common.incidents.location.State;
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
        address.state(State.VIRGINIA);
        this.active.add(new Incident(
                System.currentTimeMillis(),
                IncidentType.FIRE_ALARM,
                IncidentPriority.EMERGENCY_RESPONSE,
                CallerType.ALARM_COMPANY,
                address,
                new Unit[]{}));

        address = new Address();
        address.streetAddress("4111 Blue View Dr");
        address.zipCode(24012);
        address.town("Roanoke County");
        address.state(State.VIRGINIA);
        this.active.add(new Incident(
                System.currentTimeMillis() - (1000 * 50),
                IncidentType.EMS,
                IncidentPriority.EMS_BRAVO,
                CallerType.INDIVIDUAL,
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
