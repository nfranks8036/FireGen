package net.noahf.firewatch.common.data;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class UnitAssignmentStatus implements StructureObject {

    private final String unitAssignmentStatus;

    UnitAssignmentStatus(String unitAssignmentStatus) {
        this.unitAssignmentStatus = unitAssignmentStatus;
    }

    @Override public String getName() { return unitAssignmentStatus; }
    @Override public String getFormatted() { return unitAssignmentStatus.replace("_", " "); }

}