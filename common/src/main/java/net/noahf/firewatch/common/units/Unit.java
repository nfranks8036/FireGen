package net.noahf.firewatch.common.units;

import net.noahf.firewatch.common.data.UnitAssignmentStatus;
import net.noahf.firewatch.common.data.UnitOperationStatus;
import net.noahf.firewatch.common.data.UnitType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Unit {

    private int id;
    private UnitType type;
    private String callsign;
    private UnitOperationStatus operationStatus;
    private @Nullable UnitAssignment assignmentStatus;

}
