package net.noahf.firegen.backend.structure.objects;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.Getter;
import net.noahf.firegen.backend.structure.StructureObject;

@Entity
public class RadioChannel extends StructureObject {

    public @Id String name;

    public RadioChannel() { super("DummyRadioChannel"); }
    public RadioChannel(String name) {
        super(name);
        this.name = super.getName();
    }
}
