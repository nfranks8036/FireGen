package net.noahf.firewatch.common.data.objects;

import net.noahf.firewatch.common.data.UnitAssignmentStatus;

public abstract class StructureObject {

    public abstract String getName();
    public abstract String getFormatted();

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof StructureObject other)) return false;
        return this.getName().equalsIgnoreCase(other.getName());
    }

}