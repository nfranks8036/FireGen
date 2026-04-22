package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.incidents.Incident;

public interface UnitAssignment {

    Incident getIncident();

    Unit getUnit();

    boolean isPrimary();

    RadioChannel getRadioChannel();





}
