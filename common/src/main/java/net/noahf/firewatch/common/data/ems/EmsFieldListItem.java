package net.noahf.firewatch.common.data.ems;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class EmsFieldListItem extends StructureObject {

    private String name;
    private String description;
    private String abbreviation;

    @Override public String name() { return this.name; }
    @Override public String formatted() { return this.name.replace("_", " "); }

    public String description() {
        if (this.description == null) {
            this.description = this.name;
        }
        return this.description;
    }

    public String abbreviation() {
        if (this.abbreviation == null) {
            this.abbreviation = this.name.substring(0, 3);
        }
        return this.abbreviation;
    }

}