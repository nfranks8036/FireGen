package net.noahf.firewatch.common.data.units;

import net.noahf.firewatch.common.data.objects.ListMark;
import net.noahf.firewatch.common.data.objects.StructureObject;

public class UnitOperationStatus extends UnitStatus {

    public static final String IN_SERVICE = "10-8";
    public static final String OUT_OF_SERVICE = "10-7";

    private final String unitOperationStatus;

    public UnitOperationStatus(String unitOperationStatus) {
        this.unitOperationStatus = ListMark.removeKeys(unitOperationStatus, "*", "!");
    }

    @Override public String name() { return unitOperationStatus; }
    @Override public String formatted() { return unitOperationStatus.replace("_", " "); }

    @Override public UnitStatusType statusType() { return UnitStatusType.OPERATION; }

}