package net.noahf.firewatch.common.data;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class UnitOperationStatus extends StructureObject {

    private final String unitOperationStatus;

    UnitOperationStatus(String unitOperationStatus) {
        this.unitOperationStatus = unitOperationStatus;
    }

    @Override public String getName() { return unitOperationStatus; }
    @Override public String getFormatted() { return unitOperationStatus.replace("_", " "); }

}