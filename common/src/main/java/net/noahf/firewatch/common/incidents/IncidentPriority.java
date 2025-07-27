package net.noahf.firewatch.common.incidents;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Predicate;

public enum IncidentPriority {

    EMERGENCY_RESPONSE,

    NON_EMERGENCY_RESPONSE,

    MVC_NO_INJURIES,

    MVC_INJURIES,

    MEDICAL_CALL,

    STANDBY_OTHER,

    STANDBY_EMS,

    STANDBY_FIRE;

    @Override
    public String toString() {
        return this.name().replace("_", " ");
    }

    public static String[] asFormattedStrings() {
        String[] incidents = new String[IncidentPriority.values().length];
        for (int i = 0; i < incidents.length; i++) {
            incidents[i] = IncidentPriority.values()[i].toString();
        }
        return incidents;
    }

    public static IncidentPriority valueOfFormatted(String key) {
        return IncidentPriority.valueOf(key.replace(" ", "_").toUpperCase(Locale.ROOT));
    }

    public static String[] asFormattedStringsFilter(Predicate<IncidentPriority> filter) {
        return Arrays.stream(IncidentPriority.values())
                .filter(filter)
                .map(IncidentPriority::toString)
                .toArray(String[]::new);
    }

}
