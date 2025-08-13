package net.noahf.firewatch.common.data;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class UnitOperationStatus extends StructureObject {

    public static final String IN_SERVICE = "10-8";
    public static final String OUT_OF_SERVICE = "10-7";

    private final String unitOperationStatus;

    UnitOperationStatus(String unitOperationStatus) {
        this.unitOperationStatus = unitOperationStatus.replace("*", "").replace("!", "");
    }

    @Override public String name() { return unitOperationStatus; }
    @Override public String formatted() { return unitOperationStatus.replace("_", " "); }

}