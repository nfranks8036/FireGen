package net.noahf.firewatch.common.data;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class CallerType extends StructureObject {

    private final String callerType;

    CallerType(String callerType) {
        this.callerType = callerType;
    }

    @Override public String name() { return callerType; }
    @Override public String formatted() { return callerType.replace("_", " "); }

}