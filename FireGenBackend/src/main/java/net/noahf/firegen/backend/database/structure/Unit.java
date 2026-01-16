package net.noahf.firegen.backend.database.structure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import lombok.Getter;
import lombok.Setter;
import net.noahf.firegen.backend.structure.objects.UnitType;
import org.bson.types.ObjectId;

@Entity(value = "unit")
@Indexes(value = @Index(fields = @Field(value = "id", type = IndexType.DESC)))
@Getter @Setter
public class Unit {

    @Id
    public String id;

    @Reference @JsonIgnore
    public Agency agency;

    public UnitType unitType;

    public int unitNumber;

    public Unit() { super(); } // required for Morphia
    public Unit(Agency agency, UnitType type, int number) {
        this.id = agency.getAbbreviation() + ":" + (type.getAbbreviation() != null ? type.getAbbreviation() : agency.getAbbreviation()) + number;
        this.agency = agency;
        this.unitType = type;
        this.unitNumber = number;
    }

    public String getCallsign() {
        return this.unitType.getAbbreviation() + this.getUnitNumber();
    }

    @Override
    public String toString() {
        return this.id;
    }
}
