package net.noahf.firegen.discord.config.files;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.api.incidents.types.IncidentTypeTag;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.config.MultiObjectConfiguration;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeTagImpl;
import net.noahf.firegen.discord.utilities.Log;

import java.util.ArrayList;
import java.util.List;

import static net.noahf.firegen.discord.utilities.JsonUtilities.asStr;

public class ConfigIncidentTypes extends MultiObjectConfiguration<IncidentType> {

    public ConfigIncidentTypes(FireGenVariables vars) {
        super(vars, IncidentType.class, vars.municipality() + "/" + vars.incidentTypesFile());
    }

    @Override
    public void importObject(JsonElement e) {
        final String NEW_INCIDENT = "NEW_INCIDENT";

        JsonObject object = e.getAsJsonObject();

        List<IncidentTypeTagImpl> tags = new ArrayList<>();
        IncidentTypeTagImpl newTag = null;
        for (JsonElement element : object.getAsJsonArray("tags").asList()) {
            IncidentTypeTagImpl tag = new IncidentTypeTagImpl(element.getAsJsonObject());
            tags.add(tag);
            if (tag.tagName.equalsIgnoreCase(NEW_INCIDENT)) {
                newTag = tag;
            }
        }
        if (newTag == null) {
            throw new IllegalStateException("Expected an incident tag with name '" + NEW_INCIDENT + "', found none.");
        }

        for (JsonElement element : object.getAsJsonArray("types").asList()) {
            JsonObject obj = element.getAsJsonObject();
            String name = asStr(obj, "name");
            String tagStr = asStr(obj, "tag");
            IncidentTypeTag tag = tags.stream().filter(itt -> itt.tagName.equalsIgnoreCase(tagStr)).findFirst().orElse(null);
            if (tag == null) {
                throw new IllegalStateException("Expected type '" + name + "' to have an associated 'tag'");
            }
            List<IncidentType> types = new ArrayList<>();
            if (tagStr.equalsIgnoreCase(NEW_INCIDENT)) {
                this.getVars().defaultType(new IncidentTypeImpl(name, tag, 0));
                types.add(this.getVars().defaultType());
            } else if (tag.getQualifiers() == null) {
                types.add(new IncidentTypeImpl(name, tag, 0));
            } else {
                List<String> stringTags = tag.findTypeOptions(name);
                for (int i = 0; i < stringTags.size(); i++) {
                    types.add(new IncidentTypeImpl(name, tag, i));
                }
            }

            this.addAll(types);
        }

        if (this.getVars().defaultType() == null) {
            throw new IllegalStateException("Expected an incident type to be tagged '" + NEW_INCIDENT + "', found none.");
        }

        Log.info("Imported " + this.count() + " incident types.");
    }

    /**
     * Requests a list of all allowed {@link IncidentTypeImpl IncidentTypes} as {@link String strings} that are
     * autocomplete-ready.
     * @return the list of incident types which are autocomplete ready (can be fed into Discord's autocomplete). Please
     *         note: this <b>DOES NOT</b> limit the amount of results, you will receive the full list!
     */
    public List<String> getAutocompleteIncidentTypes() {
        return this.get().stream().map(IncidentType::getSelectedName)
                .toList();
    }
}
