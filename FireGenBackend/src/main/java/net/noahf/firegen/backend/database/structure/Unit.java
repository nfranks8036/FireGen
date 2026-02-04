package net.noahf.firegen.backend.database.structure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import net.noahf.firegen.backend.structure.objects.UnitType;

@Entity(value = "unit")
@Indexes(value = @Index(fields = @Field(value = "id", type = IndexType.DESC)))
@Getter @Setter
public class Unit {

    @Id
    public String unitId;

    @Reference @JsonIgnore
    public Agency agency;

    public UnitType unitType;

    public int unitNumber;

    public UnitAssignment assignment;

    public Unit() { super(); } // required for Morphia
    public Unit(Agency agency, UnitType type, int number) {
        this.unitId = agency.getAbbreviation() + ":" + (type.getAbbreviation() != null ? type.getAbbreviation() : agency.getAbbreviation()) + number;
        this.agency = agency;
        this.unitType = type;
        this.unitNumber = number;
        this.assignment = null;
    }

    public String getCallsign() {
        return this.unitType.getAbbreviation() + this.getUnitNumber();
    }

    @Override
    public String toString() {
        return this.unitId;
    }
}
