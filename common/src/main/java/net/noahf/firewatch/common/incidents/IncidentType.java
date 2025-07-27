package net.noahf.firewatch.common.incidents;

import java.util.Locale;

import static net.noahf.firewatch.common.incidents.IncidentPriority.*;

public enum IncidentType {

    FIRE_SERVICE_CALL (NON_EMERGENCY_RESPONSE, EMERGENCY_RESPONSE),

    FIRE_ALARM (EMERGENCY_RESPONSE),

    STRUCTURE_FIRE (EMERGENCY_RESPONSE),

    WORKING_FIRE (EMERGENCY_RESPONSE),

    MOTOR_VEHICLE_CRASH (MVC_INJURIES, MVC_NO_INJURIES),

    EMS (MEDICAL_CALL),

    STANDBY (STANDBY_EMS, STANDBY_FIRE, STANDBY_OTHER);

    private final IncidentPriority[] supportedPriorities;

    IncidentType(IncidentPriority... supportedPriorities) {
        this.supportedPriorities = supportedPriorities;
    }

    @Override
    public String toString() {
        return this.name().replace("_", " ");
    }

    public IncidentPriority[] supportedPriorityResponses() {
        return this.supportedPriorities;
    }

    public static IncidentType valueOfFormatted(String key) {
        return IncidentType.valueOf(key.replace(" ", "_").toUpperCase(Locale.ROOT));
    }

    public static String[] asFormattedStrings() {
        String[] incidents = new String[IncidentType.values().length];
        for (int i = 0; i < incidents.length; i++) {
            incidents[i] = IncidentType.values()[i].toString();
        }
        return incidents;
    }

}
