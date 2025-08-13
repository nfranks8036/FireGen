package net.noahf.firewatch.common.data;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class AgencyType extends StructureObject {

    private final String agencyType;

    AgencyType(String agencyType) {
        this.agencyType = agencyType;
    }

    @Override public String getName() { return agencyType; }
    @Override public String getFormatted() { return agencyType.replace("_", " "); }

}
