package net.noahf.firegen.backend.database.structure;

import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;

@Entity
@Indexes(value = @Index(fields = @Field(value = "id", type = IndexType.DESC)))
public class UnitType {

    @Id
    private ObjectId id;

    public String name;
    public String callsignFull;
    public String callsignAbbreviated;

}
