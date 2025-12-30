package net.noahf.firegen.backend.database.structure;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;

import java.util.List;

@Entity
public class RadioChannel {

    @Id
    private ObjectId id;

    @Reference
    public List<Agency> agencies;

    public String radio;



}
