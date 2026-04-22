package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.incidents.Incident;

public interface Unit extends Identifiable {

    Agency getAgency();

    UnitType getUnitType();

    int getUnitNumber();

    String getUnitCallsign();

    UnitAssignment currentAssignment();

    UnitAssignment assign(Incident incident);

}
