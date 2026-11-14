package net.noahf.firegen.api.incidents.units;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AssignmentStatus extends Status {

    int ordinal();

    @Nullable AssignmentStatusPurpose getPurpose();

    List<Secondary> getSecondaries();

}
