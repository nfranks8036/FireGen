package net.noahf.firewatch.common.incidents.medical;

import net.noahf.firewatch.common.incidents.IncidentPriority;

public enum MedicalPriority {

    OMEGA,
    ALPHA,
    BRAVO,
    CHARLIE,
    DELTA,
    ECHO;

    @Override
    public String toString() {
        return this.name();
    }

    public String toLetter() {
        return this.name().substring(0, 1);
    }
}
