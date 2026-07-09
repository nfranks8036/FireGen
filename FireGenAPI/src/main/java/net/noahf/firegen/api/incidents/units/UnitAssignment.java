package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;

import java.util.List;

public interface UnitAssignment extends AutofilledCharSequence, Comparable<UnitAssignment> {

    Incident getIncident();

    Unit getUnit();

    RadioChannel getRadioChannel();

    List<AssignmentEvent> getAssignments();

    AssignmentEvent getLatestAssignment();

    void assign(Contributor<?> contributor, AssignmentStatus newAssignment);

}
