package net.noahf.firewatch.common.data.ems;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class EmsFieldDetails extends StructureObject {

    private String name;
    private String description;
    private String abbreviation;

    @Override public String name() { return this.name; }
    @Override public String formatted() { return this.name.replace("_", " "); }

    public String getDescription() { return this.description; }
    public String getAbbreviation() { return this.abbreviation; }
}