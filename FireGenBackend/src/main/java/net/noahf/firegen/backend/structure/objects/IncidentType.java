package net.noahf.firegen.backend.structure.objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.noahf.firegen.backend.structure.StructureManager;
import net.noahf.firegen.backend.structure.StructureObject;

import java.util.List;
import java.util.UUID;

public class IncidentType extends StructureObject {

    private final @Getter IncidentPrioritiesPreset prioritiesPreset;

    public IncidentType(StructureManager st, JsonObject obj) {
        super(obj.get("name").getAsString());

        JsonElement prioritiesField = obj.get("priorities");
        if (prioritiesField.isJsonArray()) {
            this.prioritiesPreset = new IncidentPrioritiesPreset("Custom-" + UUID.randomUUID(), prioritiesField.getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList());
        } else {
            try {
                this.prioritiesPreset = st.getPrioritiesPresets().from(prioritiesField.getAsString());
            } catch (UnsupportedOperationException exception) {
                throw new IllegalStateException("Must have some priorities set for IncidentType '" + this.getName() + "', found none: " + obj.toString());
            }
        }
    }

    public List<String> getPriorities() {
        return this.prioritiesPreset.getPriorities();
    }

    @Override
    public String toString() {
        return "IncidentType{name=" + this.getName() + ", priorities=" + this.getPriorities() + "}";
    }
}