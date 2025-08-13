package net.noahf.firewatch.common.units;

import net.noahf.firewatch.common.incidents.Incident;
import net.noahf.firewatch.common.utils.IdGenerator;

import java.util.List;

public class UnitAssignment {

    private int id;
    private Incident incident;
    private Unit unit;
    private boolean primary;
    private List<UnitTimings> timings;
    private List<AssignmentEvent> history;

    public UnitAssignment(Incident incident, Unit unit, boolean primary) {
        this.id = IdGenerator.generate();
        
    }

}
