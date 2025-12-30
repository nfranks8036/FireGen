package net.noahf.firegen.backend.database.structure;


import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import net.noahf.firegen.backend.database.structure.helper.AgencyType;
import org.bson.types.ObjectId;

@Entity(value = "agency")
@Indexes(value = @Index(fields = @Field(value = "name", type = IndexType.TEXT)))
public class Agency {

    @Id
    private ObjectId id;

    @Indexed
    public String name = "NEW_AGENCY";

    public String simplified = "";

    public String abbreviation = "";

    public AgencyType agencyType = AgencyType.OTHER;


}
