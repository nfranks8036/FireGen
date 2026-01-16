package net.noahf.firegen.backend.structure.objects;

import lombok.Getter;
import net.noahf.firegen.backend.structure.StructureObject;

import java.util.List;

public class IncidentPrioritiesPreset extends StructureObject {

    private final @Getter List<String> priorities;

    public IncidentPrioritiesPreset(String name, List<String> priorities) {
        super(name);
        this.priorities = priorities;
    }

}
