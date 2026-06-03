package net.noahf.firegen.api.database;

import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;

import java.util.List;

public interface UnitRepository {

    Unit saveUnit(Unit unit);

    UnitAssignment saveAssignment(UnitAssignment assignment);

    List<UnitAssignment> findByIncident(Incident incident);

    List<UnitAssignment> findByUnit(Unit unit);

}
