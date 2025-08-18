package net.noahf.firewatch.common.data.units;

import net.noahf.firewatch.common.data.objects.ListMark;
import net.noahf.firewatch.common.data.objects.StructureObject;

public class UnitAssignmentStatus extends UnitStatus {

    public static final String ASSIGNED = "ASSIGNED";
    public static final String AVAILABLE = "AVAILABLE";

    private final String unitAssignmentStatus;

    public UnitAssignmentStatus(String unitAssignmentStatus) {
        this.unitAssignmentStatus = ListMark.removeKeys(unitAssignmentStatus, "*", "!");
    }

    @Override public String name() { return unitAssignmentStatus; }
    @Override public String formatted() { return unitAssignmentStatus.replace("_", " "); }

    @Override public UnitStatusType statusType() { return UnitStatusType.ASSIGNMENT; }
}