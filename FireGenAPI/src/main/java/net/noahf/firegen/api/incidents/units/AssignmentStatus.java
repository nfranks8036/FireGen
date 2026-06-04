package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;

public interface AssignmentStatus extends AutofilledCharSequence {

    String getName();

    String getShortName();

    int ordinal();

}
