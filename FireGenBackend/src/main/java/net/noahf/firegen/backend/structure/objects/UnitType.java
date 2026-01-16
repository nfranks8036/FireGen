package net.noahf.firegen.backend.structure.objects;

import com.google.gson.JsonElement;
import dev.morphia.annotations.Entity;
import jakarta.annotation.Nullable;
import lombok.Getter;
import net.noahf.firegen.backend.structure.StructureObject;

@Entity
public class UnitType extends StructureObject {

    private @Getter String callsign;
    private @Getter @Nullable String abbreviation;

    public UnitType() { super("DummyUnitType"); } // required for Morphia
    public UnitType(JsonElement name, JsonElement callsign, JsonElement abbreviation) {
        super(name.getAsString());
        this.callsign = callsign.getAsString();
        this.abbreviation = abbreviation.isJsonNull() ?  null : abbreviation.getAsString();
    }

    @Override
    public String toString() {
        return "UnitType{" +
                "name='" + getName() + "\'" +
                ", callsign='" + callsign + '\'' +
                ", abbreviation='" + abbreviation + '\'' +
                '}';
    }
}
