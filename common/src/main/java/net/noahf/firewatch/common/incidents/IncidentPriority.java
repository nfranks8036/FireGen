package net.noahf.firewatch.common.incidents;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

public enum IncidentPriority {

    STANDBY (true, true),

    SERVICE_CALL (true, true),

    EMERGENCY_RESPONSE (true, false),

    NON_EMERGENCY_RESPONSE (true, false),

    EMS_OMEGA (false, true),

    EMS_ALPHA (false, true),

    EMS_BRAVO (false, true),

    EMS_CHARLIE (false, true),

    EMS_DELTA (false, true),

    EMS_ECHO (false, true);

    final boolean fire, ems;

    IncidentPriority(boolean fire, boolean ems) {
        this.fire = fire;
        this.ems = ems;
    }

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

}
