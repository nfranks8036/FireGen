package net.noahf.firegen.api.incidents.units;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface UnitStatus extends Status {

    int ordinal();

    @Nullable UnitStatusPurpose getPurpose();

    @Nullable Set<UnitAssignment> getAssignments();

}
