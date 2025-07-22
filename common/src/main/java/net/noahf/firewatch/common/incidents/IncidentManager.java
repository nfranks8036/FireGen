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

            IncidentAddress address = IncidentAddress.address("310", "Alumni Mall", "Blacksburg", State.VIRGINIA, 24061);
        this.active.add(new Incident(
                System.currentTimeMillis(),
                IncidentType.FIRE_ALARM,
                IncidentPriority.EMERGENCY_RESPONSE,
                CallerType.ALARM_COMPANY,
                address
        ));

        address = IncidentAddress.address("190", "W Campus Dr", "Blacksburg", State.VIRGINIA, 24061);
        this.active.add(new Incident(
                System.currentTimeMillis() - (1000 * 50),
                IncidentType.EMS,
                IncidentPriority.EMS_BRAVO,
                CallerType.INDIVIDUAL,
                address
        ));
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
