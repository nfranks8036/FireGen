package net.noahf.firewatch.common.incidents;

public enum IncidentType {

    FIRE_SERVICE_CALL,

    FIRE_ALARM,

    STRUCTURE_FIRE,

    WORKING_FIRE,

    STANDBY,

    EMS_OMEGA,

    EMS_ALPHA,

    EMS_BRAVO,

    EMS_CHARLIE,

    EMS_DELTA,

    EMS_ECHO;

    @Override
    public String toString() {
        return this.name().replace("_", " ");
    }

}
