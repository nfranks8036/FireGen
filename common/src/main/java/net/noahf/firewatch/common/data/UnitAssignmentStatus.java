package net.noahf.firewatch.common.data;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class UnitAssignmentStatus extends StructureObject {

    public static final String ASSIGNED = "ASSIGNED";
    public static final String AVAILABLE = "AVAILABLE";

    private final String unitAssignmentStatus;

    UnitAssignmentStatus(String unitAssignmentStatus) {
        this.unitAssignmentStatus = unitAssignmentStatus;
    }

    @Override public String name() { return unitAssignmentStatus; }
    @Override public String formatted() { return unitAssignmentStatus.replace("_", " "); }

}