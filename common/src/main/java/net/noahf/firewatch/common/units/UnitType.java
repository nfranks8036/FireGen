package net.noahf.firewatch.common.units;

public enum UnitType {

    FIRE_ENGINE ("E", "Engine"),

    FIRE_LADDER ("L", "Ladder"),

    FIRE_TANKER ("T", "Tanker"),

    FIRE_UTILITY ("U", "Utility"),

    FIRE_RESPONSE("RESP", "Response"),

    FIRE_ATTACK("ATT", "Attack"),

    FIRE_BRUSH("BR", "Brush"),

    FIRE_HAZMAT("HAZM", "Hazmat"),

    FIRE_AIR("AIR", "Air"),

    FIRE_BATTALION_CHIEF (null, "Battalion Chief"),

    EMS_RESCUE ("R", "Rescue"),

    EMS_MEDIC ("M", "Medic"),

    EMS_SUPERVISOR (null, "Supervisor");

    private final String callsignPrefix;
    private final String callsignFull;

    UnitType(String callsignPrefix, String callsignFull) {
        this.callsignPrefix = callsignPrefix;
        this.callsignFull = callsignFull;
    }

    public String callsignPrefix() { return this.callsignPrefix; }
    public String callsignFull() { return this.callsignFull; }

}
