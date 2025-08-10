package net.noahf.firewatch.common.newincidents;

import net.noahf.firewatch.common.newincidents.objects.StructureObject;

public class AgencyType implements StructureObject {

    private final String agencyType;

    AgencyType(String agencyType) {
        this.agencyType = agencyType;
    }

    @Override public String getName() { return agencyType; }
    @Override public String getFormatted() { return agencyType.replace("_", " "); }

}
