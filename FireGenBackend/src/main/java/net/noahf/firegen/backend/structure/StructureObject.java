package net.noahf.firegen.backend.structure;

import lombok.Getter;

public abstract class StructureObject {

    private final String name;

    public StructureObject(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
