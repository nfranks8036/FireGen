package net.noahf.firewatch.common.data.units;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class UnitType extends StructureObject {

    private String name;
    private String callsign;
    private String abbreviation;

    @Override public String name() { return this.name; }
    @Override public String formatted() { return this.name.replace("_", " "); }
    public String callsign() { return this.callsign; }
    public String abbreviation() { return this.abbreviation; }

}
