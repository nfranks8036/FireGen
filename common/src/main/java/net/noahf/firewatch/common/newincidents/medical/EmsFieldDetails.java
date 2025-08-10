package net.noahf.firewatch.common.newincidents.medical;

import net.noahf.firewatch.common.newincidents.objects.StructureObject;

public class EmsFieldDetails implements StructureObject {

    private String name;
    private String description;
    private String abbreviation;

    @Override public String getName() { return this.name; }
    @Override public String getFormatted() { return this.name.replace("_", " "); }

    public String getDescription() { return this.description; }
    public String getAbbreviation() { return this.abbreviation; }
}
