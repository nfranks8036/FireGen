package net.noahf.firewatch.common.incidents;

import java.util.Locale;

public enum IncidentType {

    FIRE_ALARM (true, false),

    STRUCTURE_FIRE (true, true),

    WORKING_FIRE (true, true),

    MOTOR_VEHICLE_CRASH (true, true),

    EMS (false, true);

    private final boolean fire, ems;

    IncidentType(boolean fire, boolean ems) {
        this.fire = fire;
        this.ems = ems;
    }

    @Override
    public String toString() {
        return this.name().replace("_", " ");
    }

    public boolean isFire() { return this.fire; }
    public boolean isEMS() { return this.ems; }

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
