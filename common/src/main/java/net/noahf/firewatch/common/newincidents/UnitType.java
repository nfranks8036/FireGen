package net.noahf.firewatch.common.newincidents;

import net.noahf.firewatch.common.newincidents.lists.StructureObject;

public class UnitType implements StructureObject {

    private String name;
    private String callsign;
    private String abbreviation;

    @Override public String getName() { return this.name; }
    @Override public String getFormatted() { return this.name.replace("_", " "); }
    public String getCallsign() { return this.callsign; }
    public String getAbbreviation() { return this.abbreviation; }

}
