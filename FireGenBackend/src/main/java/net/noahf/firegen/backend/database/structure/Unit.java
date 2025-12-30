package net.noahf.firegen.backend.database.structure;

import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;

@Entity(value = "unit")
@Indexes(value = @Index(fields = @Field(value = "id", type = IndexType.DESC)))
public class Unit {

    @Id @Indexed
    private ObjectId id;

    @Reference
    public Agency agency = null;

    @Reference
    public UnitType unitType = null;

    public int unitNumber = 0;

    public void generateId() {
        this.id = new ObjectId();
    }

}
