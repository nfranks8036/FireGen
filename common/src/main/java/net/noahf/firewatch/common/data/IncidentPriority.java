package net.noahf.firewatch.common.data;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class IncidentPriority implements StructureObject {

    private String name;

    @Override public String getName() { return this.name; }
    @Override public String getFormatted() { return this.name.replace("_", " "); }

    void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof IncidentPriority other)) return false;

        return this.name.equalsIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        return this.getName().replace("_", " ");
    }
}
