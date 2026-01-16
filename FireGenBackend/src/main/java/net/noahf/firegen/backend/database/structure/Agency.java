package net.noahf.firegen.backend.database.structure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import lombok.Getter;
import lombok.Setter;
import net.noahf.firegen.backend.Main;
import net.noahf.firegen.backend.database.structure.helper.AgencyType;
import net.noahf.firegen.backend.structure.StructureList;
import net.noahf.firegen.backend.structure.StructureManager;
import net.noahf.firegen.backend.structure.StructureObject;

import java.util.ArrayList;
import java.util.List;

@Entity(value = "agency")
@Indexes(value = @Index(fields = @Field(value = "incidentNumber", type = IndexType.DESC)))
@Getter @Setter
public class Agency extends StructureObject {

    public @Id @Getter String abbreviation;

    public @Getter int station;

    public String name;

    public @Getter String simplified;

    public @Getter net.noahf.firegen.backend.database.structure.helper.AgencyType type;

    public @Getter @Reference List<Unit> units;

    public Agency() { super("DummyAgency"); } // required for morphia
    public Agency(StructureManager st, JsonObject obj) {
        super(obj.get("name").getAsString());
        this.name = super.getName();
        this.station = obj.get("station").getAsInt();
        this.simplified = obj.get("simplified").getAsString();
        this.abbreviation = obj.get("abbreviation").getAsString();
        this.type = AgencyType.valueOf(obj.get("agency_type").getAsString());

        List<JsonElement> unitObjs = obj.getAsJsonArray("units").asList();
        this.units = new ArrayList<>();
        for (JsonElement unitEle : unitObjs) {
            JsonObject unitObj = unitEle.getAsJsonObject();
            Unit unit = new Unit(
                    this,
                    st.getUnitTypes().from(unitObj.get("unit_type").getAsString()),
                    unitObj.get("number").getAsInt()
            );
            this.units.add(unit);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }
}
