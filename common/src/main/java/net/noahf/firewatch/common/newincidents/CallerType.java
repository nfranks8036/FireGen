package net.noahf.firewatch.common.newincidents;

import net.noahf.firewatch.common.newincidents.objects.StructureObject;

public class CallerType implements StructureObject {

    private final String callerType;

    CallerType(String callerType) {
        this.callerType = callerType;
    }

    @Override public String getName() { return callerType; }
    @Override public String getFormatted() { return callerType.replace("_", " "); }

}
