package net.noahf.firewatch.common.data;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class AgencyType extends StructureObject {

    private final String agencyType;

    AgencyType(String agencyType) {
        this.agencyType = agencyType.replace("*", "");
    }

    @Override public String name() { return agencyType; }
    @Override public String formatted() { return agencyType.replace("_", " "); }

}
