package net.noahf.firegen.api.utilities;

import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.incidents.IncidentType;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.incidents.units.Unit;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class IdGenerator<T extends Identifiable> {

    public static long generateIncidentId(Incident incident) {
        return id(incident, 8);
    }

    public static long generateContributorId(Contributor contributor) { return id(contributor, 7); }

    public static long generateTypeId(IncidentType type) { return id(type, 6); }

    public static long generateNarrativeId(IncidentLogEntry entry) {
        return id(entry, 5);
    }

    public static long generateAgencyId(Agency agency) {
        return id(agency, 4);
    }

    public static long generateUnitId(Unit unit) {
        return id(unit, 3);
    }

    public static long generateVenueId(LocationVenue venue) { return id(venue, 2); }



    private static final Map<Identifiable, Long> generatedIds = new HashMap<>();

    public static int getGeneratedIdsAmount() {
        return generatedIds.size();
    }

    private static <T extends Identifiable> long id(T object, int length) {
        Long id = generatedIds.get(object);
        if (id != null) {
            return id;
        }

        long origin = (long) Math.pow(10, length - 1);
        long bound = (long) Math.pow(10, length) - 1;

        long totalAllowed = bound - origin;
        long totalForType = 0;
        for (Identifiable i : generatedIds.keySet()) {
            if (i.getClass().equals(object.getClass())) {
                totalForType++;
            }
        }
        if (totalForType >= totalAllowed) {
            throw new IllegalStateException("Cannot generate a new ID for " + object.toString() +
                    ", ran out for type " + object.getClass().getCanonicalName() + " " +
                    "(" + totalForType + " >= " + totalAllowed + ", requested length of " + length + ")"
            );
        }

        long newId = new Random(object.hashCode()).nextLong(origin, bound);

        if (generatedIds.containsValue(newId)) {
            // try again because clearly this value is already taken (rare but possible)
            return id(object, length);
        }

        generatedIds.put(object, newId);
        return newId;
    }

}
