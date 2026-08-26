package net.noahf.firegen.discord.config.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.noahf.firegen.api.incidents.units.UnitType;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.config.MultiObjectConfiguration;
import net.noahf.firegen.discord.incidents.structure.units.UnitTypeImpl;

import static net.noahf.firegen.discord.utilities.JsonUtilities.asStr;

public class ConfigUnitTypes extends MultiObjectConfiguration<UnitType> {

    public ConfigUnitTypes(FireGenVariables vars) {
        super(vars, UnitType.class, vars.municipality() + "/" + vars.unitTypesFile());
    }

    @Override
    public void importObject(JsonElement element) {

        JsonArray array = element.getAsJsonArray();
        for (JsonElement item : array.asList()) {
            JsonObject root = item.getAsJsonObject();

            String id = asStr(root, "id");
            String shorthand = asStr(root, "short", true);
            String longhand = asStr(root, "long", true);
            String formatted = asStr(root, "format", true);
            String description = asStr(root, "description");

            UnitType type = new UnitTypeImpl(
                    id, shorthand, longhand, formatted, description
            );
            this.add(type);
        }

        log("Imported unit types " + String.join(", ", this.get()));
    }

    public UnitType fromId(String id) {
        for (UnitType element : this.get()) {
            if (element.getId().equalsIgnoreCase(id)) {
                return element;
            }
        }
        return null;
    }
}
