package net.noahf.firewatch.common.data.objects;

public abstract class StructureObject {

    public abstract String name();
    public abstract String formatted();

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof StructureObject other)) return false;
        return this.name().equalsIgnoreCase(other.name());
    }

}