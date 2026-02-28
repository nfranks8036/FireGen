package net.noahf.firegen.backend.structure.objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.noahf.firegen.backend.structure.StructureManager;
import net.noahf.firegen.backend.structure.StructureObject;

import java.util.List;

public class IncidentType extends StructureObject {

    private final @Getter IncidentTypeTag tag;

    public IncidentType(StructureManager st, JsonObject obj) {
        super(obj.get("name").getAsString());

        JsonElement tagsField = obj.get("tags");

        try {
            this.tag = st.getTagsPresets().from(tagsField.getAsJsonArray().get(0).getAsString());
        } catch (UnsupportedOperationException exception) {
            throw new IllegalStateException("Must have a tag set for IncidentType '" + this.getName() + "', found none: " + obj.toString());
        }
    }

    @Override
    public String toString() {
        return "IncidentType{name=" + this.getName() + ", tag=" + this.getTag().toString() + "}";
    }
}