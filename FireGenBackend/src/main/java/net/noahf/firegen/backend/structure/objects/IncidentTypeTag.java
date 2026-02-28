package net.noahf.firegen.backend.structure.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.noahf.firegen.backend.structure.StructureObject;

import javax.annotation.Nullable;
import java.util.List;

public class IncidentTypeTag extends StructureObject {


    @AllArgsConstructor
    public static class Qualifier {
        private @Getter boolean required;
        private @Getter boolean unique;
        private @Getter String syntax;
        private @Getter List<String> qualifiers;
    }

    private final @Getter List<String> priorities;
    private final @Getter @Nullable Qualifier qualifier;

    public IncidentTypeTag(String name, List<String> priorities, @Nullable Qualifier qualifier) {
        super(name);
        this.priorities = priorities;
        this.qualifier = qualifier;
    }

}
