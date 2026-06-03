package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.incidents.Incident;

import java.util.List;

public interface UnitAssignment {

    Incident getIncident();

    Unit getUnit();

    RadioChannel getRadioChannel();

    List<AssignmentEvent> getAssignments();

    AssignmentEvent getLatestAssignment();


}
