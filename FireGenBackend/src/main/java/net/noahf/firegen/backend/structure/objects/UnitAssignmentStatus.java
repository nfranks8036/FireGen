package net.noahf.firegen.backend.structure.objects;

import dev.morphia.annotations.Entity;
import lombok.Getter;
import net.noahf.firegen.backend.structure.StructureObject;

import java.util.function.Function;

@Entity
public class UnitAssignmentStatus extends StructureObject {

    public String name;
    public @Getter String narrative;

    public UnitAssignmentStatus(String name, String narrative) {
        super(name);
        this.name = super.getName();
        this.narrative = narrative;
    }

    public String asNarrative(String unit) {
        return this.narrative.replace("{0}", unit);
    }

}
