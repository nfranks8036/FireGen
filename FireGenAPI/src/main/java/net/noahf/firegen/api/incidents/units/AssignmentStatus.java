package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.style.StyledObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AssignmentStatus extends Status, StyledObject {

    int ordinal();

    @Nullable AssignmentStatusPurpose getPurpose();

    List<Secondary> getSecondaries();

}
